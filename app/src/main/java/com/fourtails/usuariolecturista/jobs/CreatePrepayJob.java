package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesNewPrepay;
import com.appspot.ocr_backend.backend.model.MessagesNewPrepayResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.ottoEvents.CreatePrepayJobEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEvents.CreatePrepayJobEvent.Type;

/**
 * CreatePrepayJob async job
 */
public class CreatePrepayJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String accountNumber;
    long mM3toPrepay;

    public CreatePrepayJob(String accountNumber, long mM3toPrepay) {
        super(new Params(Priority.MID).requireNetwork().groupBy("create-prepay"));
        this.accountNumber = accountNumber;
        this.mM3toPrepay = mM3toPrepay;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();

        MessagesNewPrepay messagesNewPrepay = new MessagesNewPrepay();
        messagesNewPrepay.setM3ToPrepay(mM3toPrepay);
        messagesNewPrepay.setAccountNumber(accountNumber);

        MessagesNewPrepayResponse response = service.prepay().backendNew(messagesNewPrepay).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            responseOk = true;
            // Go back to main activity and call the rest of the backend tasks
            MainActivity.bus.post(new CreatePrepayJobEvent(Type.COMPLETED, 1));
        } else {
            Logger.e(response.getError());
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("CreatePrepayJob canceled");
        MainActivity.bus.post(new CreatePrepayJobEvent(Type.COMPLETED, 99));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
