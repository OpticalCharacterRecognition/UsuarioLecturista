package com.fourtails.usuariolecturista;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.conekta.Charge;
import com.conekta.Token;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.fourtails.usuariolecturista.conekta.ConektaAndroid;
import com.fourtails.usuariolecturista.conekta.ConektaCallback;
import com.fourtails.usuariolecturista.fragments.BillsFragment;
import com.fourtails.usuariolecturista.fragments.ContactFragment;
import com.fourtails.usuariolecturista.fragments.NotificationsFragment;
import com.fourtails.usuariolecturista.fragments.PrepayModeFragment;
import com.fourtails.usuariolecturista.fragments.PromotionsFragment;
import com.fourtails.usuariolecturista.fragments.ReadingsFragment;
import com.fourtails.usuariolecturista.fragments.SettingsFragment;
import com.fourtails.usuariolecturista.jobs.CheckBalanceJob;
import com.fourtails.usuariolecturista.jobs.CreateNewBillJob;
import com.fourtails.usuariolecturista.jobs.CreatePrepayJob;
import com.fourtails.usuariolecturista.jobs.GetPaidBillsJob;
import com.fourtails.usuariolecturista.jobs.GetPrepayFactorJob;
import com.fourtails.usuariolecturista.jobs.GetPrepaysJob;
import com.fourtails.usuariolecturista.jobs.GetReadingsJob;
import com.fourtails.usuariolecturista.jobs.GetUnPaidBillsJob;
import com.fourtails.usuariolecturista.jobs.MakePaymentOnBackendJob;
import com.fourtails.usuariolecturista.jobs.RegisterImageNameJob;
import com.fourtails.usuariolecturista.jobs.UploadFileToGCSJob;
import com.fourtails.usuariolecturista.model.CreditCard;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.model.RegisteredUser;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerItem;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerListAdapter;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent;
import com.fourtails.usuariolecturista.ottoEvents.BillPaymentAttemptEvent;
import com.fourtails.usuariolecturista.ottoEvents.CheckBalanceEvent;
import com.fourtails.usuariolecturista.ottoEvents.CreateNewBillEvent;
import com.fourtails.usuariolecturista.ottoEvents.CreatePrepayJobEvent;
import com.fourtails.usuariolecturista.ottoEvents.GetPrepayFactorEvent;
import com.fourtails.usuariolecturista.ottoEvents.MakePaymentOnBackendEvent;
import com.fourtails.usuariolecturista.ottoEvents.PrepayPaymentAttemptEvent;
import com.fourtails.usuariolecturista.ottoEvents.RefreshMainActivityFromPrepayEvent;
import com.fourtails.usuariolecturista.ottoEvents.UploadImageEvent;
import com.fourtails.usuariolecturista.utilities.CircleTransform;
import com.orhanobut.logger.Logger;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.path.android.jobqueue.JobManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import static com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent.Status;
import static com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent.Type;


public class MainActivity extends ActionBarActivity {

    public static String TAG = "MainActivity";

    private static final String BUCKET_NAME = "ocr-test-pics";

    public static Bus bus;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.list_slidermenu)
    ListView mDrawerList;
    @InjectView(R.id.linearLayoutDrawer)
    RelativeLayout mDrawerRelativeLayout;

    @InjectView(R.id.imageViewFBProfileImage)
    ImageView imageViewFacebookProfilePic;
    @InjectView(R.id.textViewFacebookName)
    TextView textViewFacebookName;

    private SharedPreferences prefs;

    private ActionBarDrawerToggle mDrawerToggle;
    // we need this because when we try to close the drawer we have to pass the container view

    /**
     * Used to store the last screen title. For use in {@link #()}.
     */
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private String[] navMenuTitlesPrepay;
    private TypedArray navMenuIcons;
    private TypedArray navMenuIconsPrepay;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    private Context context;

    private String mUserEmail;
    private String mAccountNumber;


    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    ProgressDialog progressDialog;

    boolean refreshBillsOnly = false;

    public static boolean ranAtLeastOnce = false;


    public static long oldReadingsLastDateInMillis;

    public static int mShortAnimationDuration;

    private Crouton imageUploadCrouton;

    boolean asyncJobRunning = false;

    public static boolean isFirstTime = false;

    public static boolean prepayModeEnabled = false;

    public static boolean userHasAPrepay = false;

    public static boolean allowUserToPrepay = false;

    public static boolean loadOnlyPrepayFirst = false;

    public static double prepayFactor = 0;

    JobManager jobManager;

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bus = new AndroidBus();
        bus.register(this);

        ButterKnife.inject(this);

        ranAtLeastOnce = false;

        context = getApplicationContext();

        jobManager = FirstApplication.getInstance().getJobManager();


        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        ParseFacebookUtils.initialize(String.valueOf(R.string.facebook_app_id));

        // Account Number
        Meter meter = checkForSavedMeter();
        mAccountNumber = meter.accountNumber;

        /**toolBar **/
        setUpToolBar();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // load slide menu items
        navMenuTitlesPrepay = getResources().getStringArray(R.array.nav_drawer_items_prepay);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        // nav drawer icons from resources
        navMenuIconsPrepay = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons_prepay);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        isFirstTime = prefs.getBoolean(IntroActivity.PREF_FIRST_TIME, true);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IntroActivity.PREF_FIRST_TIME, false); // is no the first time anymore
        editor.apply();

        loadImageInBackground();

        navDrawerItems = new ArrayList<NavDrawerItem>();

        updateMeterBalance(savedInstanceState);
        getPrepayFactor();
    }

    /**
     * If the meter balance is positive then we must not allow any prepay events
     *
     * @param savedInstanceState used to draw the drawer later on
     */
    public void updateMeterBalance(Bundle savedInstanceState) {
        asyncJobRunning = true;
        mUserEmail = checkForSavedUser().email;
        jobManager.addJobInBackground(new CheckBalanceJob(mUserEmail, savedInstanceState));
    }

    @Subscribe
    public void updateMeterBalanceResponse(CheckBalanceEvent event) {
        if (event.getResultCode() == 1) {
            if (event.getBalance() < 0) { // negative balance means user has a prepay
                userHasAPrepay = true;
                allowUserToPrepay = false;
                loadOnlyPrepayFirst = true;
            } else if (event.getBalance() == 0) { // means user can be allowed to prepay
                userHasAPrepay = false;
                allowUserToPrepay = true;
                loadOnlyPrepayFirst = false;
            } else { // positive balance user must pay bills first
                userHasAPrepay = false;
                allowUserToPrepay = false;
                loadOnlyPrepayFirst = false;
            }
            finishDrawingTheDrawer(event.getSavedInstanceState());
            initiateJobs();
        }
    }

    /**
     * Finish to ad nav drawer items
     *
     * @param savedInstanceState
     */
    private void finishDrawingTheDrawer(Bundle savedInstanceState) {
        if (userHasAPrepay) {
            // Prepay
            navDrawerItems.add(new NavDrawerItem(navMenuTitlesPrepay[0], navMenuIconsPrepay.getResourceId(0, -1)));
            // History
            navDrawerItems.add(new NavDrawerItem(navMenuTitlesPrepay[1], navMenuIconsPrepay.getResourceId(1, -1)));
            // Promotions
            navDrawerItems.add(new NavDrawerItem(navMenuTitlesPrepay[2], navMenuIconsPrepay.getResourceId(2, -1)));
            // Notifications (has a counter)
            navDrawerItems.add(new NavDrawerItem(navMenuTitlesPrepay[3], navMenuIconsPrepay.getResourceId(3, -1), true, "4"));
            // Contact
            navDrawerItems.add(new NavDrawerItem(navMenuTitlesPrepay[4], navMenuIconsPrepay.getResourceId(4, -1)));
            // Settings
            navDrawerItems.add(new NavDrawerItem(navMenuTitlesPrepay[5], navMenuIconsPrepay.getResourceId(5, -1)));
        } else {
            // Readings
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
            // Promotions
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
            // Notifications (has a counter)
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, "4"));
            // Contact
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
            // Settings
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        }


        // Recycle the typed array
        navMenuIcons.recycle();
        navMenuIconsPrepay.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                mDrawerList.setItemChecked(1, true);
                mDrawerList.setSelection(1);
                getSupportActionBar().setTitle(R.string.title_activity_login);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }

        asyncJobRunning = false;
        onPostCreate(savedInstanceState);
    }

    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_login));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    /**
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        int fragmentExitTransition;
        int fragmentEnterTransition;

        if (userHasAPrepay) {
            switch (position) {
                case 0:
                    fragment = new PrepayModeFragment();
                    break;
                case 1:
                    userClickedHistory();
                    fragment = new ReadingsFragment();
                    break;
                case 2:
                    fragment = new PromotionsFragment();
                    break;
                case 3:
                    fragment = new NotificationsFragment();
                    break;
                case 4:
                    fragment = new ContactFragment();
                    break;
                case 5:
                    fragment = new SettingsFragment();
                    break;
                default:
                    break;
            }
        } else {
            switch (position) {
                case 0:
                    fragment = new ReadingsFragment();
                    break;
                case 1:
                    fragment = new PromotionsFragment();
                    break;
                case 2:
                    fragment = new NotificationsFragment();
                    break;
                case 3:
                    fragment = new ContactFragment();
                    break;
                case 4:
                    fragment = new SettingsFragment();
                    break;
                default:
                    break;
            }
        }


        if (fragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
                fragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, fragment)
                    .commit();
            Logger.d("fragment added " + fragment.getTag());

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            if (userHasAPrepay) {
                setTitle(navMenuTitlesPrepay[position]);
            } else {
                setTitle(navMenuTitles[position]);
            }
            mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        } else {
            // error in creating fragment
            Logger.e("Error in creating fragment");
        }
    }


    /**
     * *******************************************************************************************
     * Otto bus calls
     * ********************************************************************************************
     */

    public void initiateJobs() {
        if (userHasAPrepay) {
            jobManager.addJobInBackground(new GetPrepaysJob(mAccountNumber));
        } else {
            jobManager.addJobInBackground(new GetReadingsJob(mAccountNumber));
        }
    }

    public void userClickedHistory() {
        loadOnlyPrepayFirst = false;
        jobManager.addJobInBackground(new GetPrepaysJob(mAccountNumber));
        jobManager.addJobInBackground(new GetReadingsJob(mAccountNumber));
    }

    /**
     * Most of the initial backend logic will happen here
     *
     * @param backendObject that can be a READING, UNPAID_BILL, PAID_BILL, PREPAY
     */
    @Subscribe
    public void initiateJobsResponse(BackendObjectsEvent backendObject) {
        if (backendObject.status == Status.ERROR) {
            Logger.e("Error getting objects from backend");
        } else if (userHasAPrepay && loadOnlyPrepayFirst) {
            if (backendObject.type == Type.PREPAY) {
                PrepayModeFragment.bus.post(1);
            }
        } else {
            if (backendObject.type == Type.READING) {
                // Readings finished
                jobManager.addJobInBackground(new GetPaidBillsJob(mAccountNumber));
            } else if (backendObject.type == Type.PAID_BILL) {
                // Paid bills finished
                jobManager.addJobInBackground(new GetUnPaidBillsJob(mAccountNumber));
            } else if (backendObject.type == Type.UNPAID_BILL && backendObject.status == Status.NORMAL) {
                // Unpaid Bills finished and there is unpaid bills
                prepayModeEnabled = false;
                if (refreshBillsOnly) { // will only refresh the bills, gets called when a payment is made
                    BillsFragment.billsBus.post(1);
                    refreshBillsOnly = false;
                } else {
                    try {
                        ReadingsFragment.readingsBus.post(1);
                    } catch (Exception e) {
                        Logger.e(e, "did the fragment died because user took to long?");
                    }
                }
            } else if (backendObject.type == Type.UNPAID_BILL && backendObject.status == Status.NOT_FOUND) {
                // no unpaid bills found so we enable the prepaid mode
                prepayModeEnabled = true;
                jobManager.addJobInBackground(new GetPrepaysJob(mAccountNumber));
                if (refreshBillsOnly) { // will only refresh the bills, gets called when a payment is made
                    BillsFragment.billsBus.post(1);
                    refreshBillsOnly = false;
                } else {
                    try {
                        ReadingsFragment.readingsBus.post(2);
                    } catch (Exception e) {
                        Logger.e(e, "did the fragment died because user took to long?");
                    }
                }
            }
        }

    }

    @Subscribe
    public void imageCaptured(byte[] image) {
        uploadFileToGCS(image);
    }

    /**
     * Maybe we dont want this here, we have to rethink this
     *
     * @param event standar event
     */
    @Subscribe
    public void createNewBill(CreateNewBillEvent event) {
        if (event.getResultCode() == 1) {
            if (event.getType() == CreateNewBillEvent.Type.STARTED) {
                jobManager.addJobInBackground(new CreateNewBillJob(mAccountNumber));
            } else {
                Toast.makeText(this, "Nueva factura creada", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * DatabaseSave
     * Bus event called by AddCreditCardFragment that takes the credit card and then pops the
     * BackStack, this prevents the back button from going to the AddCreditCardFragment again
     *
     * @param creditCard CC that is going to be saved on the database
     */
    @Subscribe
    public void saveCreditCardAndGoBackToLists(CreditCard creditCard) {
        creditCard.save();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.popBackStack();
        Log.d(TAG, "fragment poped");
    }


    /**
     * Bus event called by fragments to change into other fragments
     *
     * @param fragment that is going to replace the current fragment
     */
    @Subscribe
    public void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragmentManager != null;
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
        Logger.d("fragment added " + fragment.getTag());
    }

    /**
     * Bus event called by fragments to start any activity
     *
     * @param intent
     */
    @Subscribe
    public void startAnyActivity(Intent intent) {
        startActivity(intent);
    }

    /**
     * Every fragment opened from the drawer must call this method to set the
     * correct toolbar title
     *
     * @param string the title that we want to show on the toolbar
     */
    @Subscribe
    public void changeTitle(String string) {
        getSupportActionBar().setTitle(string);
    }

    /**
     * Called by the settings fragment when the user wants to logout from the app.
     * Because otto library needs an object we need to pass a boolean, otherwise we
     * wouldn't just always pass a true
     *
     * @param wantsToLogOut true if we want to logout
     */
    @Subscribe
    public void logOut(Boolean wantsToLogOut) {
        if (wantsToLogOut) {
            // reset the first time to show the intro again.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(IntroActivity.PREF_FIRST_TIME, true);
            editor.apply();

            ParseUser.logOut();

            // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
            // logs out on older devices, we'll just exit.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Intent intent = new Intent(MainActivity.this,
                        DispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                finish();
            }
        }
    }

    /**
     * This will get the pay amount from the PayFragment and will attempt to call our server
     * to make a successful transaction
     *
     * @param event payment event
     */
    @Subscribe
    public void billPaymentAttempt(BillPaymentAttemptEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.popBackStack();
        int finalAmount = (int) (event.getAmount() * 100);
        paymentWithConekta(finalAmount, true, 0);
    }

    @Subscribe
    public void prepayPaymentAttempt(PrepayPaymentAttemptEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.popBackStack();
        int finalAmount = (int) (event.getAmount() * 100);
        paymentWithConekta(finalAmount, false, event.getPrepay());
    }

    public void paymentWithConekta(int payAmount, final boolean isBillPayment, final long m3forPrepay) {

        ConektaAndroid conekta = new ConektaAndroid("key_eyD5sHqgVCzppFn6f35BzQ", this);
        try {
            progressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.DialogTitlePaying), getString(R.string.DialogContentPleaseWait), true);

            JSONObject pay = new JSONObject(
                    "{" +
                            "'currency':'MXN'" + "," +
                            "'amount':" + payAmount + "," +
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
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    // TODO: Output the error in your app
                    String result = null;
                    if (error instanceof com.conekta.Error)
                        result = ((com.conekta.Error) error).message_to_purchaser;
                    else
                        result = error.getMessage();
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void success(Token token) {

                }

                @Override
                public void success(Charge token) {
                    if (isBillPayment) {
                        makeBillPaymentOnBackend();
                    } else {
                        makePrepayPaymentOnBackend(m3forPrepay);
                    }
                    Toast.makeText(getApplicationContext(), "Pago Aceptado", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * *****************************************************************************************
     */

    /**
     * DatabaseQuery
     * Retrieves the first saved CC from the database
     *
     * @return first saved CC
     */
    public static CreditCard checkForSavedCreditCard() {
        return new Select().from(CreditCard.class).executeSingle();
    }

    /**
     * DatabaseQuery
     * Retrieves the first saved Meter from the database
     *
     * @return meter
     */
    public static Meter checkForSavedMeter() {
        return new Select().from(Meter.class).executeSingle();
    }

    /**
     * DatabaseQuery
     * Retrieves the first registered user from the db
     *
     * @return the user
     */
    public static RegisteredUser checkForSavedUser() {
        return new Select().from(RegisteredUser.class).executeSingle();
    }


    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    /**
     * We want to exit the app on many back pressed
     */
    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments > 1) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Esta seguro que quiere salir de la aplicación?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            // this will call for a finish on the top login activity
                            //LoginActivityBack.loginBus.post(true);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    /**
     * Backend call
     * Tries to register the payment on the backend
     */
    public void makeBillPaymentOnBackend() {
        jobManager.addJobInBackground(new MakePaymentOnBackendJob());
    }

    @Subscribe
    public void makeBillPaymentOnBackendResponse(MakePaymentOnBackendEvent event) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (event.getResultCode() == 1) {
            refreshBillsOnly = true;
            jobManager.addJobInBackground(new GetPaidBillsJob(mAccountNumber));
            Toast.makeText(getApplicationContext(), "Pago Registrado", Toast.LENGTH_SHORT).show();
        } else if (event.getResultCode() == 99) {
            Logger.e("BACKEND, Bad-makeBillPaymentOnBackend");
        }
    }

    /**
     * Backend call
     * Tries to register the payment on the backend
     */
    public void makePrepayPaymentOnBackend(long m3forPrepay) {
        jobManager.addJobInBackground(new CreatePrepayJob(mAccountNumber, m3forPrepay));
    }

    @Subscribe
    public void makePrepayPaymentOnBackendResponse(CreatePrepayJobEvent event) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (event.getResultCode() == 1) {
            Logger.i("Prepay created, cool cool cool");
            ServiceChooserActivity.bus.post(new RefreshMainActivityFromPrepayEvent(RefreshMainActivityFromPrepayEvent.Type.COMPLETED, 1));
            finish();
        }
    }

    public void uploadFileToGCS(byte[] image) {
        showImageLoadingCrouton();
        Logger.i("Initiating Image upload");
        jobManager.addJobInBackground(new UploadFileToGCSJob(image, context, BUCKET_NAME));
    }

    @Subscribe
    public void uploadFileToGCSResponse(UploadImageEvent event) {
        if (event.getResultCode() == 1) {
            jobManager.addJobInBackground(new RegisterImageNameJob(mAccountNumber, event.getImageName()));
            showImageLoadingDoneCrouton();
        } else if (event.getResultCode() == 99) {
            showImageLoadingErrorCrouton();
        }
    }

    /**
     * Shows am infinite crouton to show image is being upload to the server
     */
    private void showImageLoadingCrouton() {
        View view = getLayoutInflater().inflate(R.layout.crouton_upload_custom_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.uploadAnimationContainer);

        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        if (!animationDrawable.isRunning()) {
            animationDrawable.start();
        }

        de.keyboardsurfer.android.widget.crouton.Configuration configuration = new de.keyboardsurfer.android.widget.crouton.Configuration.Builder()
                .setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE)
                .build();
        imageUploadCrouton = Crouton.make(this, view, toolbar).setConfiguration(configuration);
        imageUploadCrouton.show();
    }

    private void showImageLoadingDoneCrouton() {
        if (imageUploadCrouton != null) {
            imageUploadCrouton.hide();
        }
        View view = getLayoutInflater().inflate(R.layout.crouton_upload_custom_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.uploadAnimationContainer);

        imageView.setImageResource(R.drawable.ic_cloud_done_white_24dp);

        TextView textView = (TextView) view.findViewById(R.id.textViewCustomCrouton);
        textView.setText(getString(R.string.camera_message_upload_finished));

        imageUploadCrouton = Crouton.make(this, view, toolbar);
        imageUploadCrouton.show();

        createImageUploadDoneNotification();
    }

    private void showImageLoadingErrorCrouton() {
        if (imageUploadCrouton != null) {
            imageUploadCrouton.hide();
        }
        View view = getLayoutInflater().inflate(R.layout.crouton_upload_custom_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.uploadAnimationContainer);

        imageView.setImageResource(R.drawable.ic_cloud_off_white_24dp);

        TextView textView = (TextView) view.findViewById(R.id.textViewCustomCrouton);
        textView.setText(getString(R.string.toastImageUploadError));

        imageUploadCrouton = Crouton.make(this, view, toolbar);
        imageUploadCrouton.show();
    }

    @SuppressLint("NewApi")
    private void createImageUploadDoneNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.title_activity_login))
                .setContentText(getString(R.string.camera_message_upload_finished))
                .setSmallIcon(R.drawable.push_icon_white)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }


    /**
     * Here we are going to check if the user is from facebook, and if it is
     * then we call the other method with picasso to load it
     */
    public void loadImageInBackground() {
        final ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            if (ParseFacebookUtils.isLinked(parseUser)) {
                if (ParseFacebookUtils.getSession().isOpened()) {
                    Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                loadImageInBackground(user.getId());
                                textViewFacebookName.setText(parseUser.get("name").toString());
                            }
                        }
                    }).executeAsync();
                }
            } else {
                textViewFacebookName.setText(parseUser.get("name").toString());
                Picasso.with(this)
                        .load(R.drawable.ic_person_grey600_48dp)
                        .transform(new CircleTransform())
                        .into(imageViewFacebookProfilePic);
            }

        }

    }

    /**
     * This will tell picasso to load the image from the web, after a 1 sec delay
     *
     * @param facebookId id needed to load from web
     */
    public void loadImageInBackground(final String facebookId) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Picasso.with(MainActivity.this)
                        .load("https://graph.facebook.com/"
                                + facebookId + "/picture?type=large")
                        .placeholder(R.drawable.ic_person_grey600_48dp)
                        .transform(new CircleTransform())
                        .error(R.drawable.ic_person_grey600_48dp)
                        .into(imageViewFacebookProfilePic);
            }
        }, 500);

    }

    /**
     * To be used on cubic meters to money convertion
     */
    public void getPrepayFactor() {
        jobManager.addJobInBackground(new GetPrepayFactorJob(1));
    }

    @Subscribe
    public void getPrepayFactorResponse(GetPrepayFactorEvent event) {
        if (event.getResultCode() == 1) {
            prepayFactor = event.getFactor();
        } else {
            prepayFactor = 0;
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (!asyncJobRunning) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
       /* boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);*/
        return super.onPrepareOptionsMenu(menu);
    }

}