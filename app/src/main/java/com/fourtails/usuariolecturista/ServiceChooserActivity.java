package com.fourtails.usuariolecturista;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesCreateUser;
import com.appspot.ocr_backend.backend.model.MessagesCreateUserResponse;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.fourtails.usuariolecturista.model.RegisteredUser;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.melnykov.fab.FloatingActionButton;
import com.orhanobut.logger.Logger;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * This class will show the available services and also will be in charge of registering
 * the user into the backend
 */
public class ServiceChooserActivity extends Activity {

    private static final String TAG = "ServiceChooserActivity";


    public static final String PREF_METER_JMAS_REGISTERED = "meterJMASNotAddedPref";
    public static final String EXRA_USER_EMAIL = "userEmailSetAsExtra";
    public static final String EXTRA_SERVICE_TYPE = "extraServiceTypeForApp";
    public static final String EXTRA_JMAS = "jmasAccountSelected";

    volatile boolean running;

    private String accountType = "Facebook";
    private long age;
    private String email;
    private String name;

    public boolean isFabButtonShowing = false;


    @InjectView(R.id.imageViewJmasIcon)
    FloatingActionButton fabJmasIcon;

    @InjectView(R.id.imageViewGasIcon)
    FloatingActionButton fabGasIcon;

    @InjectView(R.id.imageViewCfeIcon)
    FloatingActionButton fabCfeIcon;

    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    @InjectView(R.id.textViewIntroTitle)
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
            intent.putExtra(EXRA_USER_EMAIL, email);
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
        ButterKnife.inject(this);

        ParseFacebookUtils.initialize(String.valueOf(R.string.facebook_app_id));

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
        if (ParseFacebookUtils.isLinked(parseUser)) {
            if (ParseFacebookUtils.getSession().isOpened()) {
                Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            age = 30L; //user.getBirthday() test this
                            email = user.getName() + "@test.com";
                            name = user.getName();
                            registerUserBackend();
                        }
                    }
                }).executeAsync();
            }
        } else { // todo: some of this is dummy data, must change it
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
                        Logger.json(response.toPrettyString());
                        return 1;
                    } else {
                        if (response.getError().contains("User email already in platform")) {
                            return 2;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer transactionResponse) {
                if (transactionResponse != null) {
                    if (running) {
                        progressBar.setVisibility(View.GONE);
                        showFab();
                        switch (transactionResponse) {
                            case 1:
                                registerUser();
                                Logger.i("BACKEND, Good-registerUserBackend");
                                break;
                            case 2:
                                registerUser();
                                Logger.i("BACKEND, Good-AlreadyExists-registerUserBackend");
                                break;
                            default:
                                Logger.i("BACKEND, Bad-registerUserBackend");
                        }
                    }
                } else {
                    Logger.e("BackendError - Unknown-registerUserBackend");
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
