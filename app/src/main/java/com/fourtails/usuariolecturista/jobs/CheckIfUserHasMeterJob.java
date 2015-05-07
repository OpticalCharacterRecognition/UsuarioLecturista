package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesGetMeters;
import com.appspot.ocr_backend.backend.model.MessagesGetMetersResponse;
import com.fourtails.usuariolecturista.MeterRegistrationActivity;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.ottoEventBus.CheckIfUserHasMeterEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEventBus.CheckIfUserHasMeterEvent.Type;


/**
 * RegisterUserJob async job
 */
public class CheckIfUserHasMeterJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    private String emailAsUserIdFromActivity;
    boolean meterExists = false;

    public CheckIfUserHasMeterJob(String emailAsUserIdFromActivity) {
        super(new Params(Priority.MID).requireNetwork().groupBy("check-if-user-has-meter"));
        this.emailAsUserIdFromActivity = emailAsUserIdFromActivity;
    }

    @Override
    public void onAdded() {
        Logger.d("CheckIfUserHasMeterJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
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
            Meter meter = new Meter(
                    response.getMeters().get(0).getAccountNumber(),
                    response.getMeters().get(0).getBalance(),
                    response.getMeters().get(0).getModel(),
                    response.getMeters().get(0).getUrlsafeKey()
            );
            meter.save();
            meterExists = true;
            MeterRegistrationActivity.bus.post(new CheckIfUserHasMeterEvent(Type.COMPLETED, 1, meterExists));
        } else {
            if (response.getError().contains("No Meters found under specified criteria")) {
                meterExists = false;
                retry = false;
                MeterRegistrationActivity.bus.post(new CheckIfUserHasMeterEvent(Type.COMPLETED, 1, meterExists));
            }
            if (response.getError().contains("User does not exist")) {
                meterExists = false;
                retry = false;
                MeterRegistrationActivity.bus.post(new CheckIfUserHasMeterEvent(Type.COMPLETED, 1, meterExists));
            }
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("CheckIfUserHasMeterJob canceled");
        MeterRegistrationActivity.bus.post(new CheckIfUserHasMeterEvent(Type.COMPLETED, 99, meterExists));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
