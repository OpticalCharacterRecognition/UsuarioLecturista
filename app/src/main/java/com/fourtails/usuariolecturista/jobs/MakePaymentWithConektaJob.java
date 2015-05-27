package com.fourtails.usuariolecturista.jobs;

import android.app.Activity;

import com.conekta.Charge;
import com.conekta.Token;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.conekta.ConektaAndroid;
import com.fourtails.usuariolecturista.conekta.ConektaCallback;
import com.fourtails.usuariolecturista.ottoEvents.MakePaymentWithConektaEvent;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.json.JSONException;
import org.json.JSONObject;

import static com.fourtails.usuariolecturista.ottoEvents.MakePaymentWithConektaEvent.Type;

/**
 * MakePaymentWithConektaJob
 */
public class MakePaymentWithConektaJob extends Job {
    boolean responseOk = false;
    boolean retry = true;

    Activity mActivity;
    int mPayAmount;
    long m3forPrepay;
    String mUserEmail;
    boolean mIsBillPayment;

    public MakePaymentWithConektaJob(Activity activity, int payAmount, long m3forPrepay, String userEmail, boolean isBillPayment) {
        super(new Params(Priority.LOW).requireNetwork().groupBy("pay"));
        mActivity = activity;
        mPayAmount = payAmount;
        this.m3forPrepay = m3forPrepay;
        mUserEmail = userEmail;
        mIsBillPayment = isBillPayment;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        ConektaAndroid conekta = new ConektaAndroid("key_eyD5sHqgVCzppFn6f35BzQ", mActivity);
        try {

            JSONObject pay = new JSONObject(
                    "{" +
                            "'currency':'MXN'" + "," +
                            "'amount':" + mPayAmount + "," +
                            "'description':'Android Pay'" + "," +
                            "'reference_id':'9999-quantum_wolf'" + "," +
                            //"'card':'" + tokenId + "'" + "," +
                            "'card':'tok_test_visa_4242'" + "," +
                            "'details':" +
                            "{" +
                            "'email':" + "'" + mUserEmail + "'" +
                            "}" +
                            "}");

            conekta.payThing(pay, new ConektaCallback() {
                @Override
                public void failure(Exception error) {
                    // TODO: Output the error in your app
                    String result = null;
                    if (error instanceof com.conekta.Error) {
                        result = ((com.conekta.Error) error).message_to_purchaser;
                    } else {
                        result = error.getMessage();
                    }
                    retry = false;
                    MainActivity.bus.post(new MakePaymentWithConektaEvent(Type.COMPLETED, 99, true, 0, result));
                }

                @Override
                public void success(Token token) {
                    retry = false;
                }

                @Override
                public void success(Charge token) {
                    if (mIsBillPayment) {
                        MainActivity.bus.post(new MakePaymentWithConektaEvent(Type.COMPLETED, 1, true, 0, null));
                    } else {
                        MainActivity.bus.post(new MakePaymentWithConektaEvent(Type.COMPLETED, 1, false, m3forPrepay, null)); // is a prepay
                    }
                    retry = false;
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCancel() {
        Logger.d("Error i  MakePaymentWithConektaJob");
        MainActivity.bus.post(new MakePaymentWithConektaEvent(Type.COMPLETED, 99, true, 0, "unknown error"));

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
