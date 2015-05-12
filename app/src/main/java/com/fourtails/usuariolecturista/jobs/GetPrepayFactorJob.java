package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesGetPrepayFactor;
import com.appspot.ocr_backend.backend.model.MessagesGetPrepayFactorResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.ottoEvents.GetPrepayFactorEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import static com.fourtails.usuariolecturista.ottoEvents.GetPrepayFactorEvent.Type.COMPLETED;

/**
 * GetPrepayFactorJob async job
 */
public class GetPrepayFactorJob extends Job {
    boolean responseOk = false;
    boolean retry = true;
    long mCubicMeters;

    public GetPrepayFactorJob(long mCubicMeters) {
        super(new Params(Priority.MID).requireNetwork().groupBy("GetPrepayFactorJob"));
        this.mCubicMeters = mCubicMeters;
    }

    @Override
    public void onAdded() {
        Logger.d("GetPrepayFactorJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();

        MessagesGetPrepayFactor messagesGetPrepayFactor = new MessagesGetPrepayFactor();
        messagesGetPrepayFactor.setM3ToPrepay(mCubicMeters);

        MessagesGetPrepayFactorResponse response = service.prepay().factor(messagesGetPrepayFactor).execute();
        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            MainActivity.bus.post(new GetPrepayFactorEvent(COMPLETED, 1, response.getFactor()));
            responseOk = true;
        } else {
            Logger.e(response.getError());
        }

    }

    @Override
    protected void onCancel() {
        Logger.d("GetPrepayFactorJob canceled");
        MainActivity.bus.post(new GetPrepayFactorEvent(COMPLETED, 1, 0));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
