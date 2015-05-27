package com.fourtails.usuariolecturista;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.fourtails.usuariolecturista.jobs.AssignMeterToUserBackendJob;
import com.fourtails.usuariolecturista.jobs.CheckIfUserHasMeterJob;
import com.fourtails.usuariolecturista.jobs.RegisterMeterBackendJob;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.fourtails.usuariolecturista.ottoEvents.AssignMeterToUserBackendEvent;
import com.fourtails.usuariolecturista.ottoEvents.CheckIfUserHasMeterEvent;
import com.fourtails.usuariolecturista.ottoEvents.RegisterMeterBackendEvent;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.JobManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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

    JobManager jobManager;

    Meter meter;

    public static Bus bus;

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

        bus = new AndroidBus();
        bus.register(this);

        jobManager = FirstApplication.getInstance().getJobManager();

        emailAsUserIdFromActivity = getIntent().getExtras().getString(ServiceChooserActivity.EXTRA_USER_EMAIL);


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
     */
    private void checkIfUserHasMeter() {
        if (running) {
            progressDialog = ProgressDialog.show(MeterRegistrationActivity.this, getString(R.string.DialogTitleCheckingParameters), getString(R.string.DialogContentPleaseWait), true);
        }
        jobManager.addJobInBackground(new CheckIfUserHasMeterJob(emailAsUserIdFromActivity));
    }

    @Subscribe
    public void checkIfUserHasMeterResponse(CheckIfUserHasMeterEvent event) {
        meterExists = event.getMeterExists();
        if (running) {
            progressDialog.dismiss();
        }
        if (event.getResultCode() == 1) {
            if (meterExists) {
                setSharedPrefJmasMeterRegisteredTrue();
                Intent intent = new Intent(MeterRegistrationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                Logger.i("BACKEND, Good-checkIfUserHasMeter");
            }
        }
    }

    /**
     * Registers the meter in the database
     */
    public void registerMeter() {
        if (!meterExists) {
            meter = new Meter(
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
    private void registerMeterBackend(Meter meter) {
        progressDialog = ProgressDialog.show(MeterRegistrationActivity.this, getString(R.string.dialogTitleRegisterMeterBE), getString(R.string.DialogContentPleaseWait), true);

        jobManager.addJobInBackground(new RegisterMeterBackendJob(meter.accountNumber));
    }

    @Subscribe
    public void registerMeterBackendResponse(RegisterMeterBackendEvent event) {
        if (event.getResultCode() == 1) {
            Logger.i("BACKEND-registerMeter, Good-registerMeterBackend");
            meter.save(); // we only save if successful
            assignMeterToUserBackend(meter.accountNumber);
        } else if (event.getResultCode() == 2) {
            Toast.makeText(this, "El medidor ya esta registrado en otra cuenta", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    /**
     * BackendCall
     * tries to assign the meter to the newly created user
     *
     * @param accountNumber the meterNumber
     */
    private void assignMeterToUserBackend(String accountNumber) {
        jobManager.addJobInBackground(new AssignMeterToUserBackendJob(accountNumber, emailAsUserIdFromActivity));
    }

    @Subscribe
    public void assignMeterToUserBackendResponse(AssignMeterToUserBackendEvent event) {
        progressDialog.dismiss();
        if (event.getResultCode() == 1) {
            Logger.i("BACKEND-assignMeter, Good-assignMeterToUserBackend");
            setSharedPrefJmasMeterRegisteredTrue();
            Intent intent = new Intent(MeterRegistrationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (event.getResultCode() == 99) {
            Logger.e("BACKEND-assignMeter, Bad-assignMeterToUserBackend");
        }
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
