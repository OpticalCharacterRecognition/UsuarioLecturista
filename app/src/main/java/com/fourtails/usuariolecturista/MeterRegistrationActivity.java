package com.fourtails.usuariolecturista;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesAssignMeterToUser;
import com.appspot.ocr_backend.backend.model.MessagesAssignMeterToUserResponse;
import com.appspot.ocr_backend.backend.model.MessagesCreateMeter;
import com.appspot.ocr_backend.backend.model.MessagesCreateMeterResponse;
import com.appspot.ocr_backend.backend.model.MessagesGetMeters;
import com.appspot.ocr_backend.backend.model.MessagesGetMetersResponse;
import com.fourtails.usuariolecturista.model.Meter;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * This Activity tries to register the new created user and the meter into the
 * inner database and also the backend
 */
public class MeterRegistrationActivity extends ActionBarActivity {

    private static final String TAG = "MeterRegistration";


    volatile boolean running;

    private boolean meterExists = false;

    ProgressDialog progressDialog;

    String emailAsUserIdFromActivity;


    @OnClick(R.id.buttonAddMeter)
    public void clickedButtonAddMeter() {
        registerMeter();
    }

    @InjectView(R.id.editTextMeterNumber)
    EditText meterNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        running = true;
        setContentView(R.layout.activity_meter_registration);
        ButterKnife.inject(this);

        emailAsUserIdFromActivity = getIntent().getExtras().getString(ServiceChooserActivity.EXRA_USER_EMAIL);


        checkIfUserHasMeter();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        running = false;
    }

    /**
     * DatabaseSave
     * BackendCall
     * checks if the user already has a meter linked to it
     *
     */
    private void checkIfUserHasMeter() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                if (running) {
                    progressDialog = ProgressDialog.show(MeterRegistrationActivity.this, getString(R.string.DialogTitleCheckingParameters), getString(R.string.DialogContentPleaseWait), true);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {

                    // Use a builder to help formulate the API request.
                    Backend.Builder builder = new Backend.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(),
                            null);
                    Backend service = builder.build();

                    MessagesGetMeters messagesGetMeters = new MessagesGetMeters();
                    messagesGetMeters.setUser(emailAsUserIdFromActivity);


                    MessagesGetMetersResponse response = service.meter().getAllAssignedToUser(messagesGetMeters).execute();

                    if (response.getOk()) {
                        Log.i("BACKEND", response.toPrettyString());
                        Meter meter = new Meter(
                                response.getMeters().get(0).getAccountNumber(),
                                response.getMeters().get(0).getBalance(),
                                response.getMeters().get(0).getModel(),
                                response.getMeters().get(0).getUrlsafeKey()
                        );
                        meter.save();
                        meterExists = true;
                        return true;
                    } else {
                        if (response.getError().contains("No Meters found under specified criteria")) {
                            meterExists = false;
                        }
                        if (response.getError().contains("User does not exist")) {
                            meterExists = false;
                        }
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("golocky", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean transactionResponse) {
                if (transactionResponse != null) {
                    if (running) {
                        progressDialog.dismiss();
                        if (transactionResponse) {
                            setSharedPrefJmasMeterRegisteredTrue();
                            Intent intent = new Intent(MeterRegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Log.i("BACKEND", "Good-checkIfUserHasMeter");
                        } else {
                            Log.i("BACKEND", "Bad-checkIfUserHasMeter");
                        }
                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-checkIfUserHasMeter");
                }
            }
        }.execute();

    }


    /**
     * Registers the meter in the database
     */
    public void registerMeter() {
        if (!meterExists) {
            Meter meter = new Meter(
                    meterNumber.getText().toString(),
                    0L,
                    "Cicasa",
                    null
            );
            registerMeterBackend(meter);
        }
    }

    /**
     * DatabaseSave
     * BackendCall
     * Tries to register the meter on the backend if successful it saves it into the database
     */
    private void registerMeterBackend(final Meter meter) {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(MeterRegistrationActivity.this, getString(R.string.dialogTitleRegisterMeterBE), getString(R.string.DialogContentPleaseWait), true);
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {

                    // Use a builder to help formulate the API request.
                    Backend.Builder builder = new Backend.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(),
                            null);
                    Backend service = builder.build();

                    MessagesCreateMeter messagesCreateMeter = new MessagesCreateMeter();
                    messagesCreateMeter.setAccountNumber(meter.accountNumber);

                    MessagesCreateMeterResponse response = service.meter().create(messagesCreateMeter).execute();

                    if (response.getOk()) {
                        Log.i("BACKEND-registerMeter", response.toPrettyString());
                        return 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("golocky", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer transactionResponse) {
                if (transactionResponse != null) {
                    if (running) {
                        switch (transactionResponse) {
                            case 1:
                                Log.i("BACKEND-registerMeter", "Good-registerMeterBackend");
                                meter.save();
                                assignMeterToUserBackend(meter.accountNumber);
                                break;
                            default:
                                Log.i("BACKEND-registerMeter", "Bad-registerMeterBackend");
                        }
                    } else {
                        Log.e(TAG, "BackendError - Unknown-registerMeterBackend");
                    }
                }
            }
        }.execute();

    }

    /**
     * BackendCall
     * tries to assign the meter to the newly created user
     *
     * @param accountNumber the meterNumber
     */
    private void assignMeterToUserBackend(final String accountNumber) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {

                    // Use a builder to help formulate the API request.
                    Backend.Builder builder = new Backend.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(),
                            null);
                    Backend service = builder.build();

                    MessagesAssignMeterToUser messagesAssignMeterToUser = new MessagesAssignMeterToUser();
                    messagesAssignMeterToUser.setAccountNumber(accountNumber);
                    messagesAssignMeterToUser.setEmail(emailAsUserIdFromActivity);

                    MessagesAssignMeterToUserResponse response = service.meter().assignToUser(messagesAssignMeterToUser).execute();

                    if (response.getOk()) {
                        Log.i("BACKEND-assignMeter", response.toPrettyString());
                        return 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("golocky", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer transactionResponse) {

                if (transactionResponse != null) {

                    if (running) {
                        progressDialog.dismiss();
                        switch (transactionResponse) {
                            case 1:
                                Log.i("BACKEND-assignMeter", "Good-assignMeterToUserBackend");
                                setSharedPrefJmasMeterRegisteredTrue();
                                Intent intent = new Intent(MeterRegistrationActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                Log.i("BACKEND-assignMeter", "Bad-assignMeterToUserBackend");
                        }
                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-assignMeterToUserBackend");
                }
            }
        }.execute();

    }

    public void setSharedPrefJmasMeterRegisteredTrue() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ServiceChooserActivity.PREF_METER_JMAS_REGISTERED, true); // there is one meter registered
        editor.apply();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meter_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
