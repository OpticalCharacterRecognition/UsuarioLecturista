package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesNewImageForProcessing;
import com.appspot.ocr_backend.backend.model.MessagesNewImageForProcessingResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.ottoEventBus.RegisterImageNameEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEventBus.RegisterImageNameEvent.Type;

/**
 * register image in backend async job
 */
public class RegisterImageNameJob extends Job {
    boolean responseOk = false;
    boolean retry = true;

    String mAccountNumber;
    String mImageName;

    public RegisterImageNameJob(String accountNumber, String imageName) {
        super(new Params(Priority.MID).requireNetwork().groupBy("register-image-name"));
        mAccountNumber = accountNumber;
        mImageName = imageName;
    }

    @Override
    public void onAdded() {
        Logger.i("Initiating Image upload");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();


        MessagesNewImageForProcessing messagesNewImageForProcessing = new MessagesNewImageForProcessing();
        messagesNewImageForProcessing.setAccountNumber(mAccountNumber);
        messagesNewImageForProcessing.setImageName(mImageName);

        MessagesNewImageForProcessingResponse response = service.reading().newImageForProcessing(messagesNewImageForProcessing).execute();
        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            responseOk = true;
            MainActivity.bus.post(new RegisterImageNameEvent(Type.COMPLETED, 1));
        }
    }

    @Override
    protected void onCancel() {
        Logger.e("BACKEND, Bad-registerImageNameOnBackend");
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
