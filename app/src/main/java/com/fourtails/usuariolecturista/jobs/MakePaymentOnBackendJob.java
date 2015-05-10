package com.fourtails.usuariolecturista.jobs;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesPayBill;
import com.appspot.ocr_backend.backend.model.MessagesPayBillResponse;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.fragments.BillsFragment;
import com.fourtails.usuariolecturista.model.ChartBill;
import com.fourtails.usuariolecturista.ottoEvents.MakePaymentOnBackendEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.List;

import static com.fourtails.usuariolecturista.ottoEvents.MakePaymentOnBackendEvent.Type;

/**
 * MakePaymentOnBackendJob async job
 */
public class MakePaymentOnBackendJob extends Job {
    boolean responseOk = false;
    boolean retry = true;

    public MakePaymentOnBackendJob() {
        super(new Params(Priority.MID).requireNetwork().groupBy("MakePaymentOnBackendJob"));
    }

    @Override
    public void onAdded() {
        Logger.d("MakePaymentOnBackendJob initiated");
    }

    @Override
    public void onRun() throws Throwable {
        List<ChartBill> bills = BillsFragment.getBillsForThisMonthRange(1);
        ChartBill bill = bills.get(BillsFragment.selectedBillIndex);
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();

        MessagesPayBill messagesPayBill = new MessagesPayBill();
        messagesPayBill.setBillKey(bill.urlSafeKey);

        MessagesPayBillResponse response = service.bill().pay(messagesPayBill).execute();
        if (response.getOk()) {
            Logger.json(response.toPrettyString());
            MainActivity.bus.post(new MakePaymentOnBackendEvent(Type.COMPLETED, 1));
            responseOk = true;
        } else {
            Logger.e(response.getError());
        }

    }

    @Override
    protected void onCancel() {
        Logger.d("MakePaymentOnBackendJob canceled");
        MainActivity.bus.post(new MakePaymentOnBackendEvent(Type.COMPLETED, 99));

        responseOk = false;
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
