package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesAssignMeterToUser;
import com.appspot.ocr_backend.backend.model.MessagesAssignMeterToUserResponse;
import com.fourtails.usuariolecturista.MeterRegistrationActivity;
import com.fourtails.usuariolecturista.ottoEventBus.AssignMeterToUserBackendEvent;
import com.fourtails.usuariolecturista.ottoEventBus.AssignMeterToUserBackendEvent.Type;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;


/**
 * AssignMeterToUserBackendJob async job
 */
public class AssignMeterToUserBackendJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String mAccountNumber;
    String mEmailAsUserIdFromActivity;

    public AssignMeterToUserBackendJob(String mAccountNumber, String mEmailAsUserIdFromActivity) {
        super(new Params(Priority.MID).requireNetwork().groupBy("assign-meter-to-user"));
        this.mAccountNumber = mAccountNumber;
        this.mEmailAsUserIdFromActivity = mEmailAsUserIdFromActivity;
    }

    @Override
    public void onAdded() {
        Logger.d("AssignMeterToUserBackendJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();

        MessagesAssignMeterToUser messagesAssignMeterToUser = new MessagesAssignMeterToUser();
        messagesAssignMeterToUser.setAccountNumber(mAccountNumber);
        messagesAssignMeterToUser.setEmail(mEmailAsUserIdFromActivity);

        MessagesAssignMeterToUserResponse response = service.meter().assignToUser(messagesAssignMeterToUser).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            MeterRegistrationActivity.bus.post(new AssignMeterToUserBackendEvent(Type.COMPLETED, 1));
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("AssignMeterToUserBackendJob canceled");
        MeterRegistrationActivity.bus.post(new AssignMeterToUserBackendEvent(Type.COMPLETED, 99));
        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
