package com.fourtails.usuariolecturista.jobs;

import android.text.format.Time;

import com.activeandroid.ActiveAndroid;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesBill;
import com.appspot.ocr_backend.backend.model.MessagesGetBills;
import com.appspot.ocr_backend.backend.model.MessagesGetBillsResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.model.ChartBill;
import com.fourtails.usuariolecturista.ottoEventBus.BackendObjectsEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;

/**
 * Get unpaid bills async job
 */
public class GetUnPaidBillsJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String accountNumber;

    public GetUnPaidBillsJob(String accountNumber) {
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
        messagesGetBills.setStatus("Unpaid");

        MessagesGetBillsResponse response = service.bill().get(messagesGetBills).execute();

        if (response.getOk()) {
            List<MessagesBill> billsArray = response.getBills();
            populateDBWithBills(billsArray);
            Logger.json(response.toPrettyString());
            responseOk = true;
            // Go back to main activity and call the rest of the backend tasks
            MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.UNPAID_BILL, BackendObjectsEvent.Status.NORMAL));
        } else {
            responseOk = false;
            if (response.getError().contains("No Bills found under specified criteria")) {
                retry = false;
                MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.UNPAID_BILL, BackendObjectsEvent.Status.NOT_FOUND));
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
        Logger.d("UnPaidBillsJob canceled");
        MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.UNPAID_BILL, BackendObjectsEvent.Status.ERROR));

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
