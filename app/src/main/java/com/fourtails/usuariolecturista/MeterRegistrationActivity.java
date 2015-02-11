package com.fourtails.usuariolecturista;

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
import com.appspot.ocr_backend.backend.model.MessagesCreateUser;
import com.appspot.ocr_backend.backend.model.MessagesCreateUserResponse;
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

    @OnClick(R.id.buttonAddMeter)
    public void clickedButtonAddMeter() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        registerMeter();
        finish();
    }

    @InjectView(R.id.editTextMeterNumber)
    EditText meterNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_registration);
        ButterKnife.inject(this);

        registerUser();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isMeterRegistered = prefs.getBoolean(PREF_METER_REGISTERED, false);
        if (isMeterRegistered) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * checks where is the user registering from and saves is to the database
     */
    private void registerUser() {
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
        RegisteredUser registeredUser = new RegisteredUser(
                accountType,
                age,
                email,
                name
        );
        registeredUser.save();
        registerUserBackend();
    }

    /**
     * Registers the user on the backend
     */
    private void registerUserBackend() {

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
                switch (transactionResponse) {
                    //case GoLocky.TRANSACTION_GET_USER_OK_CODE:
                    case 1:
                        Log.i("BACKEND", "Good-registerUserBackend");
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
        Meter meter = new Meter(
                meterNumber.getText().toString(),
                0L,
                "Cicasa"
        );
        meter.save();
        registerMeterBackend(meter);
    }

    /**
     * Tries to register the meter on the backend
     */
    private void registerMeterBackend(final Meter meter) {
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
                switch (transactionResponse) {
                    //case GoLocky.TRANSACTION_GET_USER_OK_CODE:
                    case 1:
                        Log.i("BACKEND-registerMeter", "Good-registerMeterBackend");
                        assignMeterToUserBackend(meter.accountNumber);
                        break;
                    default:
                        Log.i("BACKEND-registerMeter", "Bad-registerMeterBackend");
                }
            }
        }.execute();

    }

    /**
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
                switch (transactionResponse) {
                    //case GoLocky.TRANSACTION_GET_USER_OK_CODE:
                    case 1:
                        Log.i("BACKEND-assignMeter", "Good-assignMeterToUserBackend");
                        break;
                    default:
                        Log.i("BACKEND-assignMeter", "Bad-assignMeterToUserBackend");
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
