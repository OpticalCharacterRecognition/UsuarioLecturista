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
import android.os.AsyncTask;
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
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesPayBill;
import com.appspot.ocr_backend.backend.model.MessagesPayBillResponse;
import com.conekta.Charge;
import com.conekta.Token;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.fourtails.usuariolecturista.conekta.ConektaAndroid;
import com.fourtails.usuariolecturista.conekta.ConektaCallback;
import com.fourtails.usuariolecturista.jobs.GetPaidBillsJob;
import com.fourtails.usuariolecturista.jobs.GetReadingsJob;
import com.fourtails.usuariolecturista.jobs.GetUnPaidBillsJob;
import com.fourtails.usuariolecturista.jobs.MakePaymentOnBackendJob;
import com.fourtails.usuariolecturista.jobs.RegisterImageNameJob;
import com.fourtails.usuariolecturista.jobs.UploadFileToGCSJob;
import com.fourtails.usuariolecturista.model.ChartBill;
import com.fourtails.usuariolecturista.model.CreditCard;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerItem;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerListAdapter;
import com.fourtails.usuariolecturista.ottoEventBus.AndroidBus;
import com.fourtails.usuariolecturista.ottoEventBus.BackendObjectsEvent;
import com.fourtails.usuariolecturista.ottoEventBus.MakePaymentOnBackendEvent;
import com.fourtails.usuariolecturista.ottoEventBus.UploadImageEvent;
import com.fourtails.usuariolecturista.utilities.CheckNetwork;
import com.fourtails.usuariolecturista.utilities.CircleTransform;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
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
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import static com.fourtails.usuariolecturista.ottoEventBus.BackendObjectsEvent.Status;
import static com.fourtails.usuariolecturista.ottoEventBus.BackendObjectsEvent.Type;


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
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    private Context context;

    private String mUserEmail;
    private String mAccountNumber;


    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    ProgressDialog progressDialog;

    boolean refreshBillsOnly = false;

    public static boolean prepaidModeEnabled = false;

    public static long oldReadingsLastDateInMillis;

    public static int mShortAnimationDuration;

    private Crouton imageUploadCrouton;

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

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IntroActivity.PREF_FIRST_TIME, false); // is no the first time anymore
        editor.apply();

        loadImageInBackground();

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Balance
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Promotions
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Notifications (has a counter)
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, "4"));
        // Contact
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        // Settings
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

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

        jobManager.addJobInBackground(new GetReadingsJob(mAccountNumber));
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
     * *******************************************************************************************
     * Otto bus calls
     * ********************************************************************************************
     */

    /**
     * Most of the initial backend logic will happen here
     *
     * @param backendObject that can be a READING, UNPAID_BILL, PAID_BILL, UNPAID_PREPAY, PAID_PREPAY
     */
    @Subscribe
    public void initiateJobs(BackendObjectsEvent backendObject) {
        if (backendObject.status == Status.ERROR) {
            Logger.e("Error getting objects from backend");
        } else { //if there is no error
            if (backendObject.type == Type.READING) {
                // Readings finished
                jobManager.addJobInBackground(new GetPaidBillsJob(mAccountNumber));
            } else if (backendObject.type == Type.PAID_BILL) {
                // Paid bills finished
                jobManager.addJobInBackground(new GetUnPaidBillsJob(mAccountNumber));
            } else if (backendObject.type == Type.UNPAID_BILL && backendObject.status == Status.NORMAL) {
                // Unpaid Bills finished and there is unpaid bills
                prepaidModeEnabled = false;
                if (refreshBillsOnly) { // will only refresh the bills, gets called when a payment is made
                    BillsFragment.billsBus.post(1);
                    refreshBillsOnly = false;
                } else {
                    ReadingsFragment.readingsBus.post(1);
                }
            } else if (backendObject.type == Type.UNPAID_BILL && backendObject.status == Status.NOT_FOUND) {
                // no unpaid bills found so we enable the prepaid mode
                prepaidModeEnabled = true;
                if (refreshBillsOnly) { // will only refresh the bills, gets called when a payment is made
                    BillsFragment.billsBus.post(1);
                    refreshBillsOnly = false;
                } else {
                    ReadingsFragment.readingsBus.post(2);
                }
            }
        }

    }

    @Subscribe
    public void imageCaptured(byte[] image) {
        uploadFileToGCS(image);
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
     * @param payAmount
     */
    @Subscribe
    public void paymentAttempt(Double payAmount) {
        FragmentManager fragmentManager = getSupportFragmentManager();


        fragmentManager.popBackStack();
        int finalAmount = (int) (payAmount * 100);
        paymentWithConekta(finalAmount);
    }

    public void paymentWithConekta(int payAmount) {

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
                    if (prepaidModeEnabled) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            //TODO make prepaid backend call
                        }
                    } else {
                        makeNormalPaymentOnBackend();
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
                    .setMessage("Esta seguro que quiere salir de la applicacion?")
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
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        int fragmentExitTransition;
        int fragmentEnterTransition;
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
            default:
                break;
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
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        } else {
            // error in creating fragment
            Logger.e("Error in creating fragment");
        }
    }

    /**
     * Backend call
     * Tries to register the payment on the backend
     */
    public void makeNormalPaymentOnBackend() {
        jobManager.addJobInBackground(new MakePaymentOnBackendJob());
    }

    @Subscribe
    public void makeNormalPaymentOnBackendResponse(MakePaymentOnBackendEvent event) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (event.getResultCode() == 1) {
            refreshBillsOnly = true;
            jobManager.addJobInBackground(new GetPaidBillsJob(mAccountNumber));
            Toast.makeText(getApplicationContext(), "Pago Registrado", Toast.LENGTH_SHORT).show();
        } else if (event.getResultCode() == 99) {
            Logger.e("BACKEND, Bad-makeNormalPaymentOnBackend");
        }
    }

    /**
     * Backend call
     * Tries to register the payment on the backend
     */
    public void makePrepayPaymentOnBackend() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    if (!CheckNetwork.isInternetAvailable(MainActivity.this)) {
                        return 99;
                    }
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
                        return 1;
                    } else {
                        return 2;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e, e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer transactionResponse) {
                if (transactionResponse != null) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    switch (transactionResponse) {
                        case 1:
                            Logger.i("BACKEND, Good-makeNormalPaymentOnBackend");
                            refreshBillsOnly = true;
                            jobManager.addJobInBackground(new GetPaidBillsJob(mAccountNumber));
                            Toast.makeText(getApplicationContext(), "Pago Registrado", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Logger.i("BACKEND, Good-NoBills-makeNormalPaymentOnBackend");
                            break;
                        case 99:
                            Logger.e("NO INTERNET");
                            break;
                        default:
                            Logger.e("Bad-makeNormalPaymentOnBackend");
                    }
                } else {
                    Logger.e("BackendError - Unknown-makeNormalPaymentOnBackend");
                }
            }
        }.execute();
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
            mUserEmail = parseUser.getEmail();
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
        mDrawerToggle.syncState();
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