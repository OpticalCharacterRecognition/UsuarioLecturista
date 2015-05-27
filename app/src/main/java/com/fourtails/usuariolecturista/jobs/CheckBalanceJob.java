package com.fourtails.usuariolecturista.jobs;

import android.os.Bundle;

import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesGetMeters;
import com.appspot.ocr_backend.backend.model.MessagesGetMetersResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.ottoEvents.CheckBalanceEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


/**
 * CheckBalanceJob async job
 */
public class CheckBalanceJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String emailAsUserIdFromActivity;
    Bundle savedInstanceState;
    boolean mIsFirstTime;


    public CheckBalanceJob(String emailAsUserIdFromActivity, Bundle savedInstanceState, boolean isFirstTime) {
        super(new Params(Priority.HIGH).requireNetwork().groupBy("check-balance"));
        this.emailAsUserIdFromActivity = emailAsUserIdFromActivity;
        this.savedInstanceState = savedInstanceState;
        this.mIsFirstTime = isFirstTime;
    }

    @Override
    public void onAdded() {
        Logger.d("CheckBalanceJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        Meter meter = checkForSavedMeter();

        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();

        MessagesGetMeters messagesGetMeters = new MessagesGetMeters();
        messagesGetMeters.setUser(emailAsUserIdFromActivity);

        MessagesGetMetersResponse response = service.meter().getAllAssignedToUser(messagesGetMeters).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            meter.balance = response.getMeters().get(0).getBalance();
            meter.save();
            MainActivity.bus.post(new CheckBalanceEvent(CheckBalanceEvent.Type.COMPLETED, 1, meter.balance, savedInstanceState, mIsFirstTime));
        } else {
            Logger.e(response.getError());
        }
    }

    public static Meter checkForSavedMeter() {
        return new Select().from(Meter.class).executeSingle();
    }

    @Override
    protected void onCancel() {
        Logger.d("CheckBalanceJob canceled");
        MainActivity.bus.post(new CheckBalanceEvent(CheckBalanceEvent.Type.COMPLETED, 99, 99999999, null, mIsFirstTime));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
