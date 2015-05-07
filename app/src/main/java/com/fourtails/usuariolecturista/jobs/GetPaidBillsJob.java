package com.fourtails.usuariolecturista.jobs;

import android.text.format.Time;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesBill;
import com.appspot.ocr_backend.backend.model.MessagesGetBills;
import com.appspot.ocr_backend.backend.model.MessagesGetBillsResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.model.ChartBill;
import com.fourtails.usuariolecturista.model.ChartReading;
import com.fourtails.usuariolecturista.ottoEventBus.BackendObjectsEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;

/**
 * Get paid bills async job
 */
public class GetPaidBillsJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String accountNumber;

    public GetPaidBillsJob(String accountNumber) {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-bills"));
        this.accountNumber = accountNumber;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();

        MessagesGetBills messagesGetBills = new MessagesGetBills();
        messagesGetBills.setAccountNumber(accountNumber);
        messagesGetBills.setStatus("Paid");

        MessagesGetBillsResponse response = service.bill().get(messagesGetBills).execute();

        if (response.getOk()) {
            List<MessagesBill> billsArray = response.getBills();
            eraseBillsDataFromLocalDB();
            populateDBWithBills(billsArray);
            Logger.json(response.toPrettyString());
            responseOk = true;
            // Go back to main activity and call the rest of the backend tasks
            MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.PAID_BILL, BackendObjectsEvent.Status.NORMAL));

        } else {
            responseOk = false;
            if (response.getError().contains("No Bills found under specified criteria")) {
                eraseBillsDataFromLocalDB();
                retry = false; // we don't want to retry because there is not going to be new bills right away
                MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.PAID_BILL, BackendObjectsEvent.Status.NOT_FOUND));
            }
        }

    }

    /**
     * Database erase
     * Erases the db so we don't have to check if the reading already exists and don't put duplicates * erases all the bills so we don't have to compare
     */
    private void eraseBillsDataFromLocalDB() {
        List<ChartReading> tempList = new Select().from(ChartBill.class).execute();
        if (tempList != null && tempList.size() > 0) {
            ActiveAndroid.beginTransaction();
            try {
                new Delete().from(ChartBill.class).execute();
                ActiveAndroid.setTransactionSuccessful();
            } catch (Exception e) {
                Logger.e(e, "error deleting existing db");
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    /**
     * Database save
     * This will attempt to save the bills from the backend to the
     *
     * @param billsArray from the backend
     */
    private void populateDBWithBills(List<MessagesBill> billsArray) {
        Time time = new Time();

        ActiveAndroid.beginTransaction();
        try {
            for (MessagesBill bills : billsArray) {
                long timeInMillis = bills.getCreationDate().getValue();
                time.set(timeInMillis);

                ChartBill chartBill = new ChartBill(
                        time.monthDay,
                        time.month,
                        time.year,
                        timeInMillis,
                        bills.getAmount(),
                        bills.getBalance(),
                        bills.getUrlsafeKey(),
                        bills.getAccountNumber(),
                        bills.getStatus());
                chartBill.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(e, "there was an error saving to the database, most likely the data doesn't have" +
                    "the needed fields from the database or they are null");
        } finally {
            ActiveAndroid.endTransaction();
        }

    }


    @Override
    protected void onCancel() {
        Logger.d("PaidBillsJob canceled");
        MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.PAID_BILL, BackendObjectsEvent.Status.ERROR));

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
