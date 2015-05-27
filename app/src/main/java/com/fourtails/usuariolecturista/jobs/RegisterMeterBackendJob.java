package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesCreateMeter;
import com.appspot.ocr_backend.backend.model.MessagesCreateMeterResponse;
import com.fourtails.usuariolecturista.MeterRegistrationActivity;
import com.fourtails.usuariolecturista.ottoEvents.RegisterMeterBackendEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEvents.RegisterMeterBackendEvent.Type;


/**
 * RegisterMeterBackendJob async job
 */
public class RegisterMeterBackendJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String mAccountNumber;

    public RegisterMeterBackendJob(String mAccountNumber) {
        super(new Params(Priority.MID).requireNetwork().groupBy("register-meter-backend"));
        this.mAccountNumber = mAccountNumber;
    }

    @Override
    public void onAdded() {
        Logger.d("RegisterMeterBackendJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();

        MessagesCreateMeter messagesCreateMeter = new MessagesCreateMeter();
        messagesCreateMeter.setAccountNumber(mAccountNumber);

        MessagesCreateMeterResponse response = service.meter().create(messagesCreateMeter).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            MeterRegistrationActivity.bus.post(new RegisterMeterBackendEvent(Type.COMPLETED, 1));
        } else if (response.getError().contains("Meter account number already in platform")) {
            MeterRegistrationActivity.bus.post(new RegisterMeterBackendEvent(Type.COMPLETED, 2));
            Logger.e(response.getError());
        } else {
            Logger.e(response.getError());
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("RegisterMeterBackendJob canceled");
        MeterRegistrationActivity.bus.post(new RegisterMeterBackendEvent(Type.COMPLETED, 99));
        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
