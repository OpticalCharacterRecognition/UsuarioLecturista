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

import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesAssignMeterToUser;
import com.appspot.ocr_backend.backend.model.MessagesAssignMeterToUserResponse;
import com.appspot.ocr_backend.backend.model.MessagesCreateMeter;
import com.appspot.ocr_backend.backend.model.MessagesCreateMeterResponse;
import com.appspot.ocr_backend.backend.model.MessagesCreateUser;
import com.appspot.ocr_backend.backend.model.MessagesCreateUserResponse;
import com.appspot.ocr_backend.backend.model.MessagesGetMeters;
import com.appspot.ocr_backend.backend.model.MessagesGetMetersResponse;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.model.RegisteredUser;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * This Activity tries to register the new created user and the meter into the
 * inner database and also the backend
 */
public class MeterRegistrationActivity extends ActionBarActivity {

    public static final String PREF_METER_REGISTERED = "meterNotAddedPref";

    private String accountType = "Facebook";
    private long age;
    private String email;
    private String name;

    volatile boolean running;

    private boolean meterExists = false;

    ProgressDialog progressDialog;

    @OnClick(R.id.buttonAddMeter)
    public void clickedButtonAddMeter() {
        registerMeter();
    }

    @InjectView(R.id.editTextMeterNumber)
    EditText meterNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_registration);
        ButterKnife.inject(this);

        running = true;

        registerUserBackend();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isMeterRegistered = prefs.getBoolean(PREF_METER_REGISTERED, false);

        if (isMeterRegistered) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        checkIfUserHasMeter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
    }

    /**
     * DatabaseSave
     * BackendCall
     * checks if the user already has a meter linked to it
     *
     * @return true if it does then it saves it to the database, else it returns false so the app asks for it
     */
    private void checkIfUserHasMeter() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(MeterRegistrationActivity.this, "Verificando Parametros", "Por favor espere...", true);
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
                    messagesGetMeters.setUser(email);


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
                    } else if (response.getError().contains("No Meters found under specified criteria")) {
                        meterExists = false;
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
                if (running) {
                    progressDialog.dismiss();
                    if (transactionResponse) {
                        Intent intent = new Intent(MeterRegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Log.i("BACKEND", "Good-checkIfUserHasMeter");
                    } else {
                        Log.i("BACKEND", "Bad-checkIfUserHasMeter");
                    }
                }
            }
        }.execute();

    }

    /**
     * DatabaseSave
     * checks where is the user registering from and saves is to the database
     */
    private void registerUser() {
        // we need to check if the user is already in the database
        RegisteredUser previouslyRegisteredUser = checkForExistingUser(email);
        if (previouslyRegisteredUser == null) {
            RegisteredUser registeredUser = new RegisteredUser(
                    accountType,
                    age,
                    email,
                    name
            );
            registeredUser.save();
        }
    }

    /**
     * DatabaseQuery
     * We check in our database if the user is already registered
     *
     * @param emailAsUsername use the email as username
     * @return a registered user if exists
     */
    private RegisteredUser checkForExistingUser(String emailAsUsername) {
        return new Select()
                .from(RegisteredUser.class)
                .where("Email = ?", emailAsUsername)
                .executeSingle();
    }

    /**
     * fill user info into the global variables
     */
    private void fillUserInfo() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (ParseFacebookUtils.isLinked(parseUser)) {
            if (ParseFacebookUtils.getSession().isOpened()) {
                Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            age = 30L; //user.getBirthday() test this
                            email = user.getName() + "@test.com";
                            name = user.getName();
                        }
                    }
                }).executeAsync();
            }
        } else { // todo: some of this is dummy data, must change it
            age = 30L;
            email = parseUser.getEmail();
            name = parseUser.getUsername();
        }
    }

    /**
     * BackendCall
     * Registers the user on the backend
     */
    private void registerUserBackend() {

        // fill the global variables
        fillUserInfo();

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

                    MessagesCreateUser messagesCreateUser = new MessagesCreateUser();
                    messagesCreateUser.setAccountType(accountType);
                    messagesCreateUser.setAge(age);
                    messagesCreateUser.setEmail(email);
                    messagesCreateUser.setName(name);

                    MessagesCreateUserResponse response = service.user().create(messagesCreateUser).execute();

                    if (response.getOk()) {
                        Log.i("BACKEND", response.toPrettyString());
                        return 1;
                    } else {
                        if (response.getError().contains("User email already in platform")) {
                            return 2;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("golocky", e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer transactionResponse) {
                switch (transactionResponse) {
                    case 1:
                        registerUser();
                        Log.i("BACKEND", "Good-registerUserBackend");
                        break;
                    case 2:
                        registerUser();
                        Log.i("BACKEND", "Good-AlreadyExists-registerUserBackend");
                        break;
                    default:
                        Log.i("BACKEND", "Bad-registerUserBackend");
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
                progressDialog = ProgressDialog.show(MeterRegistrationActivity.this, "Registrando el Medidor", "Por favor espere...", true);
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
                    messagesCreateMeter.setBalance(meter.balance);
                    messagesCreateMeter.setModel(meter.modelType);

                    MessagesCreateMeterResponse response = service.meter().create(messagesCreateMeter).execute();

                    if (response.getOk()) {
                        Log.i("BACKEND-registerMeter", response.toPrettyString());
                        //return GoLocky.TRANSACTION_GET_USER_OK_CODE;
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
                if (running) {
                    switch (transactionResponse) {

                        //case GoLocky.TRANSACTION_GET_USER_OK_CODE:
                        case 1:
                            Log.i("BACKEND-registerMeter", "Good-registerMeterBackend");
                            meter.save();
                            assignMeterToUserBackend(meter.accountNumber);
                            break;
                        default:
                            Log.i("BACKEND-registerMeter", "Bad-registerMeterBackend");
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
                    messagesAssignMeterToUser.setEmail(email);

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
                if (running) {
                    progressDialog.dismiss();
                    switch (transactionResponse) {
                        case 1:
                            Log.i("BACKEND-assignMeter", "Good-assignMeterToUserBackend");
                            Intent intent = new Intent(MeterRegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        default:
                            Log.i("BACKEND-assignMeter", "Bad-assignMeterToUserBackend");
                    }
                }
            }
        }.execute();

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
