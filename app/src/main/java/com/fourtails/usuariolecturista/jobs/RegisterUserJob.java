package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesCreateUser;
import com.appspot.ocr_backend.backend.model.MessagesCreateUserResponse;
import com.fourtails.usuariolecturista.ServiceChooserActivity;
import com.fourtails.usuariolecturista.ottoEvents.RegisterUserEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.parse.ParseInstallation;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEvents.RegisterUserEvent.Type;


/**
 * RegisterUserJob async job
 */
public class RegisterUserJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    private String mAccountType;
    private long mAge;
    private String mEmail;
    private String mName;
    private String mInstallationId;

    public RegisterUserJob(String mAccountType, long mAge, String mEmail, String mName, String mInstallationId) {
        super(new Params(Priority.MID).requireNetwork().groupBy("register-user"));
        this.mAccountType = mAccountType;
        this.mAge = mAge;
        this.mEmail = mEmail;
        this.mName = mName;
        this.mInstallationId = mInstallationId;
    }

    @Override
    public void onAdded() {
        Logger.d("RegisterUserJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null);
        Backend service = builder.build();

        MessagesCreateUser messagesCreateUser = new MessagesCreateUser();
        messagesCreateUser.setAccountType(mAccountType);
        messagesCreateUser.setAge(mAge);
        messagesCreateUser.setEmail(mEmail);
        messagesCreateUser.setName(mName);
        messagesCreateUser.setInstallationId(mInstallationId);
        ParseInstallation.getCurrentInstallation().getInstallationId();

        MessagesCreateUserResponse response = service.user().create(messagesCreateUser).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            ServiceChooserActivity.bus.post(new RegisterUserEvent(Type.COMPLETED, 1));
            responseOk = true;
        } else {
            if (response.getError().contains("User email already in platform")) {
                Logger.d(response.getError());
                ServiceChooserActivity.bus.post(new RegisterUserEvent(Type.COMPLETED, 1));
                retry = false;
            } else {
                Logger.e(response.getError());
            }
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("RegisterUserJob canceled");
        ServiceChooserActivity.bus.post(new RegisterUserEvent(Type.COMPLETED, 99));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
