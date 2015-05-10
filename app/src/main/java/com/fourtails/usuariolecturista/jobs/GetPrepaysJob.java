package com.fourtails.usuariolecturista.jobs;

import android.text.format.Time;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesGetPrepays;
import com.appspot.ocr_backend.backend.model.MessagesGetPrepaysResponse;
import com.appspot.ocr_backend.backend.model.MessagesPrepay;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.model.ChartPrepay;
import com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;

/**
 * GetPrepaysJob async job
 */
public class GetPrepaysJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String accountNumber;

    public GetPrepaysJob(String accountNumber) {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-prepay"));
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

        MessagesGetPrepays messagesGetPrepays = new MessagesGetPrepays();
        messagesGetPrepays.setAccountNumber(accountNumber);

        MessagesGetPrepaysResponse response = service.prepay().get(messagesGetPrepays).execute();

        if (response.getOk()) {
            List<MessagesPrepay> prepayArray = response.getPrepays();
            erasePrepayDataFromLocalDB();
            populateDBWithPrepay(prepayArray);
            Logger.json(response.toPrettyString());
            responseOk = true;
            // Go back to main activity and call the rest of the backend tasks
            MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.PREPAY, BackendObjectsEvent.Status.NORMAL));

        } else {
            responseOk = false;
            if (response.getError().contains("No Prepay events found under specified criteria")) {
                Logger.d(response.getError());
                erasePrepayDataFromLocalDB();
                retry = false; // we don't want to retry because there is not going to be new bills right away
                MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.PREPAY, BackendObjectsEvent.Status.NOT_FOUND));
            } else {
                Logger.e(response.getError());
            }
        }

    }

    /**
     * Database erase
     * Erases the db so we don't have to check if the reading already exists and don't put duplicates * erases all the bills so we don't have to compare
     */
    private void erasePrepayDataFromLocalDB() {
        List<ChartPrepay> tempList = new Select().from(ChartPrepay.class).execute();
        if (tempList != null && tempList.size() > 0) {
            ActiveAndroid.beginTransaction();
            try {
                new Delete().from(ChartPrepay.class).execute();
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
     * @param prepayArray from the backend
     */
    private void populateDBWithPrepay(List<MessagesPrepay> prepayArray) {
        Time time = new Time();

        ActiveAndroid.beginTransaction();
        try {
            for (MessagesPrepay prepays : prepayArray) {
                long timeInMillis = prepays.getCreationDate().getValue();
                time.set(timeInMillis);

                ChartPrepay chartPrepay = new ChartPrepay(
                        time.monthDay,
                        time.month,
                        time.year,
                        timeInMillis,
                        prepays.getAmount(),
                        prepays.getBalance(),
                        prepays.getPrepay(),
                        prepays.getUrlsafeKey(),
                        prepays.getAccountNumber());
                chartPrepay.save();
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
        Logger.d("GetPrepaysJob canceled");
        MainActivity.bus.post(new BackendObjectsEvent(BackendObjectsEvent.Type.PREPAY, BackendObjectsEvent.Status.ERROR));

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
