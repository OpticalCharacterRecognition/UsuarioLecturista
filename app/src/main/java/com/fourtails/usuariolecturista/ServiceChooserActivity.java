package com.fourtails.usuariolecturista;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.fourtails.usuariolecturista.jobs.RegisterUserJob;
import com.fourtails.usuariolecturista.model.RegisteredUser;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.fourtails.usuariolecturista.ottoEvents.RefreshMainActivityFromPrepayEvent;
import com.fourtails.usuariolecturista.ottoEvents.RegisterUserEvent;
import com.melnykov.fab.FloatingActionButton;
import com.orhanobut.logger.Logger;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.path.android.jobqueue.JobManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This class will show the available services and also will be in charge of registering
 * the user into the backend
 */
public class ServiceChooserActivity extends Activity {

    private static final String TAG = "ServiceChooserActivity";


    public static final String PREF_METER_JMAS_REGISTERED = "meterJMASNotAddedPref";
    public static final String EXTRA_USER_EMAIL = "userEmailSetAsExtra";
    public static final String EXTRA_SERVICE_TYPE = "extraServiceTypeForApp";
    public static final String EXTRA_JMAS = "jmasAccountSelected";

    volatile boolean running;

    private String accountType;
    private long age;
    private String email;
    private String name;
    private String installationId;

    JobManager jobManager;

    public static Bus bus;

    public boolean isFabButtonShowing = false;


    @Bind(R.id.imageViewJmasIcon)
    FloatingActionButton fabJmasIcon;

    @Bind(R.id.imageViewGasIcon)
    FloatingActionButton fabGasIcon;

    @Bind(R.id.imageViewCfeIcon)
    FloatingActionButton fabCfeIcon;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    @Bind(R.id.textViewIntroTitle)
    TextView introTitle;


    @OnClick(R.id.imageViewJmasIcon)
    public void jmasIconClicked() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isMeterRegistered = prefs.getBoolean(PREF_METER_JMAS_REGISTERED, false);
        if (isMeterRegistered) { // if the meter is already registered then we go straight to MainActivity
            startActivityWithSharedElementTransition();
            //finish();
        } else { // otherwise we have to register the meter
            Intent intent = new Intent(this, MeterRegistrationActivity.class);
            intent.putExtra(EXTRA_USER_EMAIL, email);
            intent.putExtra(EXTRA_SERVICE_TYPE, EXTRA_JMAS);
            startActivity(intent);
        }
    }

    /**
     * will start an activity transition with a shared element animation
     */
    private void startActivityWithSharedElementTransition() {
        Intent intent = new Intent(this, MainActivity.class);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                fabJmasIcon, getResources().getString(R.string.transitionJmas)
        );
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_service_chooser);
        ButterKnife.bind(this);

        bus = new AndroidBus();
        bus.register(this);

        jobManager = FirstApplication.getInstance().getJobManager();

//        ParseFacebookUtils.initialize(String.valueOf(R.string.facebook_app_id));

        running = true;

        fillUserInfoThenRegister();

        progressBar.setVisibility(View.VISIBLE);

        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryOCR), PorterDuff.Mode.SRC_IN);

        introTitle.setVisibility(View.GONE);

        fabJmasIcon.hide();
        fabJmasIcon.setVisibility(View.INVISIBLE);
        fabGasIcon.hide();
        fabGasIcon.setVisibility(View.INVISIBLE);
        fabCfeIcon.hide();
        fabCfeIcon.setVisibility(View.INVISIBLE);


    }

    /**
     * fill user info into the global variables
     */
    private void fillUserInfoThenRegister() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        installationId = ParseInstallation.getCurrentInstallation().getInstallationId();
        Logger.d("InstallationId ::" + installationId);
        if (ParseFacebookUtils.isLinked(parseUser)) {
            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject user, GraphResponse response) {
                            try {
                                if (user != null) {
                                    accountType = "Facebook";
                                    age = 30L; //user.getBirthday() test this
                                    name = user.get("name").toString();
                                    email = user.get("email").toString();
                                    registerUserBackend();
                                }
                            } catch (JSONException e) {
                                if (e.getMessage().equalsIgnoreCase("No value for email")) {
                                    email = name + "@test.com";
                                    registerUserBackend();
                                }
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,age_range,gender");
            request.setParameters(parameters);
            request.executeAsync();
        } else { // todo: some of this is dummy data, must change it
            accountType = "G+";
            age = 30L;
            email = parseUser.getEmail();
            name = parseUser.getUsername();
            registerUserBackend();
        }
    }

    /**
     * BackendCall
     * Registers the user on the backend
     */
    private void registerUserBackend() {
        jobManager.addJobInBackground(new RegisterUserJob(accountType, age, email, name, installationId));
    }

    @Subscribe
    public void registerUserBackendResponse(RegisterUserEvent event) {
        if (running) {
            progressBar.setVisibility(View.GONE);
            showFab();
        }
        if (event.getResultCode() == 1) {
            registerUser();
        } else if (event.getResultCode() == 99) {
            Logger.i("BACKEND, Bad-registerUserBackend");
        }
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
                    name,
                    installationId
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

    @Subscribe
    public void refreshMainActivityFromPrepay(RefreshMainActivityFromPrepayEvent event) {
        if (event.getResultCode() == 1) {
            startActivityWithSharedElementTransition();
        }
    }


    /**
     * Show the calculate button
     */
    private void showFab() {
        if (!isFabButtonShowing) {
            isFabButtonShowing = true;
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            fabJmasIcon.setAlpha(0f);
            fabJmasIcon.setVisibility(View.VISIBLE);
            fabGasIcon.setAlpha(0f);
            fabGasIcon.setVisibility(View.VISIBLE);
            fabCfeIcon.setAlpha(0f);
            fabCfeIcon.setVisibility(View.VISIBLE);
            introTitle.setAlpha(0f);
            introTitle.setVisibility(View.VISIBLE);

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            fabJmasIcon.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fabJmasIcon.show();
                            fabGasIcon.animate()
                                    .alpha(1f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            fabGasIcon.show();
                                            fabCfeIcon.animate()
                                                    .alpha(1f)
                                                    .setDuration(300)
                                                    .setListener(new AnimatorListenerAdapter() {
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            fabCfeIcon.show();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });


            introTitle.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                        }
                    });
        }
    }


}
