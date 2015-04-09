package com.fourtails.usuariolecturista;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
import android.text.format.Time;
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

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesBill;
import com.appspot.ocr_backend.backend.model.MessagesGetBills;
import com.appspot.ocr_backend.backend.model.MessagesGetBillsResponse;
import com.appspot.ocr_backend.backend.model.MessagesGetReadings;
import com.appspot.ocr_backend.backend.model.MessagesGetReadingsResponse;
import com.appspot.ocr_backend.backend.model.MessagesPayBill;
import com.appspot.ocr_backend.backend.model.MessagesPayBillResponse;
import com.appspot.ocr_backend.backend.model.MessagesReading;
import com.conekta.Charge;
import com.conekta.Token;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.fourtails.usuariolecturista.conekta.ConektaAndroid;
import com.fourtails.usuariolecturista.conekta.ConektaCallback;
import com.fourtails.usuariolecturista.model.ChartBill;
import com.fourtails.usuariolecturista.model.ChartReading;
import com.fourtails.usuariolecturista.model.CreditCard;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerItem;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerListAdapter;
import com.fourtails.usuariolecturista.utilities.CheckNetwork;
import com.fourtails.usuariolecturista.utilities.CircleTransform;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.storage.StorageScopes;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {
    private static final String BUCKET_NAME = "ocr-test-pics";

    public static Bus bus;

    public static String TAG = "MainActivity";

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

    public static Bitmap savedBitmap;

    private String globalUserEmail;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    public static final int GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE = 00233;

    ProgressDialog progressDialog;

    boolean refreshBillsOnly = false;

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bus = new Bus(ThreadEnforcer.MAIN);
        bus.register(this);

        ButterKnife.inject(this);

        context = getApplicationContext();

        ParseFacebookUtils.initialize(String.valueOf(R.string.facebook_app_id));


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
        getReadingsFromBackend();

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
        Log.d(TAG, "fragment added " + fragment.getTag());
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
        // TODO: add all the server calls here
        FragmentManager fragmentManager = getSupportFragmentManager();


        fragmentManager.popBackStack();
        int finalAmount = (int) (payAmount * 100);
        payThing(finalAmount);
        //Toast.makeText(this, "Pago Aceptado", Toast.LENGTH_SHORT).show();
    }

    public void payThing(int payAmount) {

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
                            "'email':" + "'" + globalUserEmail + "'" +
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
                    makePaymentOnBackend();
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
     * Generates a random Identifier for the image
     *
     * @return random id
     */
    public String generateIdentifierForImage() {
        UUID id = UUID.randomUUID();
        return id.toString();
    }


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
            Log.d(TAG, "fragment added " + fragment.getTag());

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        } else {
            // error in creating fragment
            Log.e(TAG, "Error in creating fragment");
        }
    }

    /**
     * Backend call
     * Tries to register the payment on the backend
     */
    public void makePaymentOnBackend() {
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
                        Log.i("BACKEND", response.toPrettyString());
                        return 1;
                    } else {
                        return 2;
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
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    switch (transactionResponse) {
                        case 1:
                            Log.i("BACKEND", "Good-makePaymentOnBackend");
                            refreshBillsOnly = true;
                            getPaidBillsFromBackend();
                            Toast.makeText(getApplicationContext(), "Pago Registrado", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Log.i("BACKEND", "Good-NoBills-makePaymentOnBackend");
                            break;
                        case 99:
                            Log.i("ERROR", "NO INTERNET");
                            break;
                        default:
                            Log.i("BACKEND", "Bad-makePaymentOnBackend");
                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-makePaymentOnBackend");
                }
            }
        }.execute();
    }


    /**
     * BACKEND call to upload a picture from the camera to Google Cloud Storage
     * TODO: SECURITY PROBLEM remove the key from assets, find a way to do it
     *
     * @param image
     */
    public void uploadFileToGCS(final byte[] image) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                Toast.makeText(context, getString(R.string.camera_message_uploading), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    // convert key into class File. from inputStream to file. in an aux class.
                    File file = createFileFromInputStream();

                    NetHttpTransport httpTransport = new NetHttpTransport();

                    String emailAddress = "382197999605-nc5h44q7v54mgn915eqvtd71is4r46jg@developer.gserviceaccount.com";

                    // Google credentials
                    GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                            .setJsonFactory(JSON_FACTORY)
                            .setServiceAccountId(emailAddress)
                            .setServiceAccountScopes(Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL))
                            .setServiceAccountPrivateKeyFromP12File(file)
                            .build();

                    String imageName = generateIdentifierForImage();


                    String URI = "https://storage.googleapis.com/" + BUCKET_NAME + "/" + imageName + ".jpg";
                    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

                    GenericUrl url = new GenericUrl(URI);

                    HttpContent contentSend = new ByteArrayContent("image/jpeg", image);

                    HttpRequest putRequest = requestFactory.buildPutRequest(url, contentSend);

                    HttpResponse response = putRequest.execute();

                    if (response.isSuccessStatusCode()) {
                        Log.i("BACKEND", response.getStatusMessage());
                        return 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    Log.d("debug", "Error in  user profile image uploading", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer transactionResponse) {
                if (transactionResponse != null) {
                    switch (transactionResponse) {
                        case 1:
                            Log.i("BACKEND", "Good-uploadFileToGCS");
                            Toast.makeText(context, getString(R.string.camera_message_upload_finished), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Log.i("BACKEND", "Bad-uploadFileToGCS");
                            Toast.makeText(context, getString(R.string.toastImageUploadError), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-uploadFileToGCS");
                }
            }
        }.execute();
    }

    /**
     * Creates a temporary file that stores the .p12 GCS authentication services
     * TODO: security concern about the file being on assets.
     *
     * @return a p12 file
     */
    private File createFileFromInputStream() {
        File file = null;
        try {
            // we take the key and put it in a temporary file
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open("privatekey.p12"); //you should not put the key in assets in prod version.

            file = File.createTempFile("tempKeyFile", "p12");

            OutputStream outputStream = new FileOutputStream(file);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Cant create a file from input stream");
        }
        return file;
    }

    /**
     * Bitmap to array method
     *
     * @param bitmap bitmap to convert
     * @return
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
        return baos.toByteArray();
    }

//    /**
//     * DatabaseSave
//     * tries to save a reading to the backend
//     *
//     * @param chartReading the last reading from the camera
//     */
//    private void saveReadingToBackend(final ChartReading chartReading) {
//        new AsyncTask<Void, Void, Integer>() {
//            @Override
//            protected Integer doInBackground(Void... params) {
//                try {
//
//                    Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
//                    Backend service = builder.build();
//
//                    MessagesNewReading messagesNewReading = new MessagesNewReading();
//                    messagesNewReading.setAccountNumber(checkForSavedMeter().accountNumber);
//                    //messagesNewReading.setImageName((long) chartReading.value);
//
//                    MessagesNewReadingResponse response = service.reading().newImage(messagesNewReading).execute();
//
//                    if (response.getOk()) {
//                        Log.i("BACKEND", response.toPrettyString());
//                        return 1;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.e(TAG, e.getMessage());
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Integer transactionResponse) {
//                if (transactionResponse != null) {
//                    switch (transactionResponse) {
//                        case 1:
//                            chartReading.save();
//                            Log.i("BACKEND", "Good-saveReadingToBackend");
//                            break;
//                        default:
//                            Log.i("BACKEND", "Bad-saveReadingToBackend");
//                    }
//                } else {
//                    Log.e(TAG, "BackendError - Unknown-saveReadingToBackend");
//                }
//            }
//        }.execute();
//    }

    /**
     * DatabaseSave
     * BackendCall
     * tries to get the readings from the backend
     * onPostExecute calls for the bills on backend
     * TODO: ask for a period of time on the backend, we can't just go and get all the readings ever
     */
    private void getReadingsFromBackend() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    if (!CheckNetwork.isInternetAvailable(MainActivity.this)) {
                        return 99;
                    }
                    // Use a builder to help formulate the API request.
                    Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
                    Backend service = builder.build();

                    MessagesGetReadings messagesGetReadings = new MessagesGetReadings();
                    messagesGetReadings.setAccountNumber("3");

                    MessagesGetReadingsResponse response = service.reading().get(messagesGetReadings).execute();

                    if (response.getOk()) {
                        List<MessagesReading> readingsArray = response.getReadings();
                        eraseReadingsDataFromLocalDB();
                        populateDBWithReadings(readingsArray);
                        Log.i("BACKEND", response.toPrettyString());
                        return 1;
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
                    switch (transactionResponse) {
                        case 1:
                            getPaidBillsFromBackend();
                            Log.i("BACKEND", "Good-getReadingsFromBackend");
                            break;
                        case 99:
                            Log.i("ERROR", "NO INTERNET");
                            break;
                        default:
                            Log.i("BACKEND", "Bad-getReadingsFromBackend");
                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-getReadingsFromBackend");
                }
            }
        }.execute();
    }

    /**
     * DatabaseSave
     * BackendCall
     * tries to get the bills from the backend
     * TODO: ask for all the readings on the backend, no matter the status.
     */
    private void getPaidBillsFromBackend() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    if (!CheckNetwork.isInternetAvailable(MainActivity.this)) {
                        return 99;
                    }
                    // Use a builder to help formulate the API request.
                    Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
                    Backend service = builder.build();

                    MessagesGetBills messagesGetBills = new MessagesGetBills();
                    messagesGetBills.setAccountNumber("3");
                    messagesGetBills.setStatus("Paid");

                    MessagesGetBillsResponse response = service.bill().get(messagesGetBills).execute();

                    if (response.getOk()) {
                        List<MessagesBill> billsArray = response.getBills();
                        eraseBillsDataFromLocalDB();
                        populateDBWithBills(billsArray);
                        Log.i("BACKEND", response.toPrettyString());
                        return 1;
                    } else {
                        if (response.getError().contains("No Bills found under specified criteria")) {
                            eraseBillsDataFromLocalDB();
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
                    switch (transactionResponse) {
                        case 1:
                            getUnPaidBillsFromBackend();
                            Log.i("BACKEND", "Good-getPaidBillsFromBackend");
                            break;
                        case 99:
                            Log.i("ERROR", "NO INTERNET");
                            break;
                        default:
                            Log.i("BACKEND", "Bad-getPaidBillsFromBackend");
                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-getPaidBillsFromBackend");
                }
            }
        }.execute();
    }

    /**
     * DatabaseSave
     * BackendCall
     * tries to get the bills from the backend
     * TODO: ask for all the readings on the backend, no matter the status.
     */
    private void getUnPaidBillsFromBackend() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    if (!CheckNetwork.isInternetAvailable(MainActivity.this)) {
                        return 99;
                    }
                    // Use a builder to help formulate the API request.
                    Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
                    Backend service = builder.build();

                    MessagesGetBills messagesGetBills = new MessagesGetBills();
                    messagesGetBills.setAccountNumber("3");
                    messagesGetBills.setStatus("Unpaid");

                    MessagesGetBillsResponse response = service.bill().get(messagesGetBills).execute();

                    if (response.getOk()) {
                        List<MessagesBill> billsArray = response.getBills();
                        populateDBWithBills(billsArray);
                        Log.i("BACKEND", response.toPrettyString());
                        return 1;
                    } else {
                        if (response.getError().contains("No Bills found under specified criteria")) {
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
                    switch (transactionResponse) {
                        case 1:
                            if (refreshBillsOnly) { // will only refresh the bills, gets called when a payment is made
                                BillsFragment.billsBus.post(1);
                                refreshBillsOnly = false;
                            } else {
                                ReadingsFragment.readingsBus.post(1);
                            }
                            Log.i("BACKEND", "Good-getUnPaidBillsFromBackend");
                            break;
                        case 2:
                            ReadingsFragment.readingsBus.post(2);
                            Log.i("BACKEND", "Good-NoBills-getUnPaidBillsFromBackend");
                            break;
                        case 99:
                            ReadingsFragment.readingsBus.post(2);
                            Log.i("ERROR", "NO INTERNET");
                            break;
                        default:
                            Log.i("BACKEND", "Bad-getUnPaidBillsFromBackend");
                    }
                } else {
                    Log.e(TAG, "BackendError - Unknown-getUnPaidBillsFromBackend");
                }
            }
        }.execute();
    }

    /**
     * Database save
     * This will attempt to save the readings from the backend to the
     *
     * @param readingsArray they array from the backend
     */
    private void populateDBWithReadings(List<MessagesReading> readingsArray) {
        Time time = new Time();

        ActiveAndroid.beginTransaction();
        try {
            for (MessagesReading readings : readingsArray) {
                DateTime dateTime = readings.getCreationDate();
                time.set(dateTime.getValue());

                ChartReading chartReading = new ChartReading(
                        time.monthDay,
                        time.month,
                        time.year,
                        readings.getCreationDate(),
                        readings.getMeasure(),
                        readings.getUrlsafeKey(),
                        readings.getAccountNumber());
                chartReading.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "there was an error saving to the database, most likely the data doesn't have" +
                    "the needed fields from the database or they are null");
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * same as readings
     *
     * @param billsArray from the backend
     */
    private void populateDBWithBills(List<MessagesBill> billsArray) {
        Time time = new Time();

        ActiveAndroid.beginTransaction();
        try {
            for (MessagesBill bills : billsArray) {
                DateTime dateTime = bills.getCreationDate();
                time.set(dateTime.getValue());

                ChartBill chartBill = new ChartBill(
                        time.monthDay,
                        time.month,
                        time.year,
                        bills.getCreationDate(),
                        bills.getAmount(),
                        bills.getBalance(),
                        bills.getUrlsafeKey(),
                        bills.getAccountNumber(),
                        bills.getStatus());
                chartBill.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "there was an error saving to the database, most likely the data doesn't have" +
                    "the needed fields from the database or they are null");
        } finally {
            ActiveAndroid.endTransaction();
        }

    }


    /**
     * Database erase
     * Erases the db so we don't have to check if the reading already exists and don't put duplicates
     */
    private void eraseReadingsDataFromLocalDB() {
        List<ChartReading> tempList = new Select().from(ChartReading.class).execute();
        if (tempList != null && tempList.size() > 0) {
            ActiveAndroid.beginTransaction();
            try {
                new Delete().from(ChartReading.class).execute();
                ActiveAndroid.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, "error deleting existing db");
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

    }

    /**
     * see top
     */
    private void eraseBillsDataFromLocalDB() {
        List<ChartReading> tempList = new Select().from(ChartBill.class).execute();
        if (tempList != null && tempList.size() > 0) {
            ActiveAndroid.beginTransaction();
            try {
                new Delete().from(ChartBill.class).execute();
                ActiveAndroid.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, "error deleting existing db");
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    /**
     * Here we are going to check if the user is from facebook, and if it is
     * then we call the other method with picasso to load it
     */
    public void loadImageInBackground() {
        final ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            globalUserEmail = parseUser.getEmail();
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

    // Todo: remove if not used
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // This comes from the OCR activity
        if (requestCode == GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE) {
            startBalanceFragment();
        }
    }

    /**
     * start the balance fragment when coming from the OCR activity
     */
    private void startBalanceFragment() {
        Fragment fragment = new ReadingsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fragment).commit();
        Log.d(TAG, "fragment added in the beginning " + fragment.getTag());
        mDrawerLayout.closeDrawer(mDrawerRelativeLayout);

    }
}