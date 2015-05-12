package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesNewBill;
import com.appspot.ocr_backend.backend.model.MessagesNewBillResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.ottoEvents.CreateNewBillEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEvents.CreateNewBillEvent.Type;


/**
 * CreateNewBillJob async job
 */
public class CreateNewBillJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    String accountNumber;

    public CreateNewBillJob(String accountNumber) {
        super(new Params(Priority.MID).requireNetwork().groupBy("create-bill"));
        this.accountNumber = accountNumber;
    }

    @Override
    public void onAdded() {
        Logger.d("CreateNewBillJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();

        MessagesNewBill messagesNewBill = new MessagesNewBill();
        messagesNewBill.setAccountNumber(accountNumber);

        MessagesNewBillResponse response = service.bill().backendNew(messagesNewBill).execute();

        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            responseOk = true;
            // Go back to main activity and call the rest of the backend tasks
            MainActivity.bus.post(new CreateNewBillEvent(Type.COMPLETED, 1));
        } else {
            Logger.e(response.getError());
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("CreateNewBillJob canceled");
        MainActivity.bus.post(new CreateNewBillEvent(Type.COMPLETED, 2));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
