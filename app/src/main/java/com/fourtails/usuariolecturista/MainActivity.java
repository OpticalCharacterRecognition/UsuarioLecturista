package com.fourtails.usuariolecturista;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesNewReading;
import com.appspot.ocr_backend.backend.model.MessagesNewReadingResponse;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.fourtails.usuariolecturista.model.ChartReading;
import com.fourtails.usuariolecturista.model.CreditCard;
import com.fourtails.usuariolecturista.model.Meter;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerItem;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerListAdapter;
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
import com.google.api.services.storage.StorageScopes;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.fourtails.usuariolecturista.LoginFragment.PREF_FACEBOOK_PROFILE_NAME;


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

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    public static final int GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE = 00233;


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
        //String facebookId = prefs.getString(PREF_FACEBOOK_PROFILE_ID, "");
        String facebookName = prefs.getString(PREF_FACEBOOK_PROFILE_NAME, "");

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
            createDummyReadingsForThisMonth();
            getReadingsForThisMonth();
            displayView(0);
        }

    }

    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Usuario Lecturista");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**********************************************************************************************
     *                                                                               Otto bus calls
     **********************************************************************************************/

    /**
     * This is the reading coming from CaptureActivity
     *
     * @param reading from the meter
     */
    @Subscribe
    public void readingReceived(Integer reading) {
        Toast.makeText(this, reading.toString(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "READINGGGGGGGGGGGGGGGGGGGGGGG " + reading.toString());
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        ChartReading chartReading = new ChartReading(
                time.monthDay,
                time.month,
                time.year,
                reading
        );
        //saveReadingToBackend(chartReading);
    }

    /**
     * Test test test
     *
     * @param l
     */
    @Subscribe
    public void testThing(Long l) {
        uploadFileToGCS();
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
        fragmentManager.popBackStack();
        Toast.makeText(this, "Pago Aceptado", Toast.LENGTH_SHORT).show();
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
     * DatabaseQuery
     * gets all the readings for this month
     *
     * @return >_>
     */
    public static List<ChartReading> getReadingsForThisMonth() {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        return new Select()
                .from(ChartReading.class)
                .where("month = ?", time.month)
                .orderBy("day ASC")
                .execute();
    }

    /**
     * DatabaseSave
     * testing purposes only
     * creates random dummy readings, duh
     */
    public void createDummyReadingsForThisMonth() {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        for (int i = 0; i < 10; i++) {
            Random r = new Random();
            int randomDay = r.nextInt(28 - 1 + 1) + 1;
            ChartReading chartReading = new ChartReading(
                    randomDay,
                    time.month,
                    time.year,
                    i + randomDay
            );
            chartReading.save();
        }
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

    private void enableToolbarSpinner(boolean enable) {
        if (enable) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

    }


    /**
     * BACKEND call to upload a picture from the camera to Google Cloud Storage
     * TODO: SECURITY PROBLEM remove the key from assets, find a way to do it
     */
    public void uploadFileToGCS() {
        new AsyncTask<Void, Void, Integer>() {
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

                    // byte array holds the data, in this case the image i want to upload in bytes.
                    Bitmap bm = savedBitmap.copy(Bitmap.Config.ARGB_8888, false);

                    byte[] byteArray = bitmapToByteArray(bm);

                    HttpContent contentSend = new ByteArrayContent("image/jpeg", byteArray);

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
                switch (transactionResponse) {
                    case 1:
                        Log.i("BACKEND", "Good-saveReadingToBackend");
                        break;
                    default:
                        Log.i("BACKEND", "Bad-saveReadingToBackend");
                        Toast.makeText(context, getString(R.string.toastImageUploadError), Toast.LENGTH_SHORT).show();

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

    /**
     * DatabaseSave
     * tries to save a reading to the backend
     *
     * @param chartReading the last reading from the camera
     */
    private void saveReadingToBackend(final ChartReading chartReading) {
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

                    MessagesNewReading messagesNewReading = new MessagesNewReading();
                    messagesNewReading.setAccountNumber(checkForSavedMeter().accountNumber);
                    //messagesNewReading.setImageName((long) chartReading.value);

                    MessagesNewReadingResponse response = service.reading().newImage(messagesNewReading).execute();

                    if (response.getOk()) {
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
                switch (transactionResponse) {
                    case 1:
                        chartReading.save();
                        Log.i("BACKEND", "Good-saveReadingToBackend");
                        break;
                    default:
                        Log.i("BACKEND", "Bad-saveReadingToBackend");
                }
            }
        }.execute();
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