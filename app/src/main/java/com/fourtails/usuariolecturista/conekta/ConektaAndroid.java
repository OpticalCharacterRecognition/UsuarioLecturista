package com.fourtails.usuariolecturista.conekta;

import android.app.Activity;
import android.os.AsyncTask;
import android.provider.Settings;

import com.conekta.Charge;
import com.conekta.Token;
import com.devicecollector.DeviceCollector;

import org.json.JSONObject;

/**
 * Created by mauriciomurga on 3/2/15.
 */

/**
 * Created by mauriciomurga on 3/2/15.
 */


public class ConektaAndroid implements DeviceCollector.StatusListener {

    private String publicKey;
    private DeviceCollector dc;

    public ConektaAndroid(String publicKey, Activity activity) {
        this.setApiKey(publicKey);
        this.setDeviceCollector(activity);
    }

    private void setApiKey(String publicKey) {
        this.publicKey = publicKey;
        com.conekta.Conekta.setApiKey(publicKey);
    }

    private void setDeviceCollector(Activity activity) {
        this.dc = new DeviceCollector(activity);
        this.dc.setStatusListener(this);
        this.dc.setMerchantId("205000");
        this.dc.setCollectorUrl("https://api.conekta.io/fraud_providers/kount/logo.htm");
    }

    public void tokenizeCard(final JSONObject card, final ConektaCallback callback) {
        if (card == null) {
            throw new RuntimeException("Parameter Validation Error: missing card");
        }
        if (callback == null) {
            throw new RuntimeException("Parameter Validation Error: missing callback to hander errors");
        }
        final String sessionId = Settings.Secure.ANDROID_ID;
        this.dc.collect(sessionId);
        AsyncTask<Void, Void, Response> task = new AsyncTask<Void, Void, Response>() {
            protected Response doInBackground(Void... params) {
                try {
                    JSONObject tokenParams = new JSONObject();
                    tokenParams.put("card", ((JSONObject) card.get("card")).put("device_fingerprint", sessionId));
                    Token token = Token.create(tokenParams);
                    return new Response(token, null);
                } catch (Exception e) {
                    return new Response(null, e);
                }
            }

            protected void onPostExecute(Response result) {
                if (result.token != null) {
                    callback.success(result.token);
                } else if (result.error != null) {
                    callback.failure(result.error);
                }
            }
        };
        task.execute();

    }

    public void payThing(final JSONObject chargeJson, final ConektaCallback callback) {
        if (chargeJson == null) {
            throw new RuntimeException("Parameter Validation Error: missing card");
        }
        if (callback == null) {
            throw new RuntimeException("Parameter Validation Error: missing callback to hander errors");
        }
        final String sessionId = Settings.Secure.ANDROID_ID;
        this.dc.collect(sessionId);
        AsyncTask<Void, Void, ResponseCharge> task = new AsyncTask<Void, Void, ResponseCharge>() {
            protected ResponseCharge doInBackground(Void... params) {
                try {
                    Charge charge = Charge.create(chargeJson);
                    return new ResponseCharge(charge, null);
                } catch (Exception e) {
                    return new ResponseCharge(null, e);
                }
            }

            protected void onPostExecute(ResponseCharge result) {
                if (result.charge != null) {
                    callback.success(result.charge);
                } else if (result.error != null) {
                    callback.failure(result.error);
                }
            }
        };
        task.execute();

    }

    @Override
    public void onCollectorStart() {
        System.out.println("Device Collector Started");
    }

    @Override
    public void onCollectorSuccess() {
        System.out.println("Device Collector Finished Successfully");
    }

    @Override
    public void onCollectorError(DeviceCollector.ErrorCode errorCode, Exception e) {
        String error = null;
        if (null != e) {
            if (errorCode.equals(DeviceCollector.ErrorCode.MERCHANT_CANCELLED)) {
                error += "Merchant Cancelled\n";
            } else {
                error += "Device Collector Failed. It had an error [" + errorCode + "]:" + e.getMessage();
                error += "Stack Trace:";
                for (StackTraceElement element : e.getStackTrace()) {
                    error += element.getClassName() + " " + element.getMethodName() + "(" + element.getLineNumber() + ")";
                }
            }
        } else {
            error += "Device Collector failed. It had an error [" + errorCode + "]:";
        }
        throw new RuntimeException(error);
    }

    private static class Response {
        public final Token token;
        public final Exception error;

        private Response(Token token, Exception error) {
            this.error = error;
            this.token = token;
        }
    }

    private static class ResponseCharge {
        public final Charge charge;
        public final Exception error;

        private ResponseCharge(Charge charge, Exception error) {
            this.error = error;
            this.charge = charge;
        }
    }
}