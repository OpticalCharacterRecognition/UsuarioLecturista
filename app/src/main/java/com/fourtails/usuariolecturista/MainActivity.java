package com.fourtails.usuariolecturista;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerItem;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerListAdapter;
import com.fourtails.usuariolecturista.utilities.CircleTransform;
import com.fourtails.usuariolecturista.utilities.CreditCard;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.fourtails.usuariolecturista.LoginFragment.PREF_FACEBOOK_PROFILE_ID;
import static com.fourtails.usuariolecturista.LoginFragment.PREF_FACEBOOK_PROFILE_NAME;


public class MainActivity extends ActionBarActivity {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

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


    private ActionBarDrawerToggle mDrawerToggle;
    // we need this because when we try to close the drawer we have to pass the container view


    // nav drawer title
    private CharSequence mDrawerTitle;

    /**
     * Used to store the last screen title. For use in {@link #()}.
     */
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    Spinner toolbarSpinner;

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

        /**toolBar **/
        setUpToolBar();

        mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String facebookId = prefs.getString(PREF_FACEBOOK_PROFILE_ID, "");
        String facebookName = prefs.getString(PREF_FACEBOOK_PROFILE_NAME, "");

        Picasso.with(this)
                .load("https://graph.facebook.com/"
                        + facebookId + "/picture?type=large")
                .placeholder(R.drawable.ic_titular)
                .transform(new CircleTransform())
                .error(R.drawable.ic_titular)
                .into(imageViewFacebookProfilePic);

        textViewFacebookName.setText(facebookName);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Balance
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Dummy
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, "4"));
        // OCR scanner
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
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
                getSupportActionBar().setTitle("Usuario Lecturista");
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

        // we might not need this
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        // boolean carlos = true;
                    }
                });
    }

    private void setUpToolBar() {
        toolbarSpinner = (Spinner) toolbar.getChildAt(0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Usuario Lecturista");
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        String[] frags = new String[]{
                "1 Nov - 30 Nov 2014",
                "1 Oct - 31 Oct 2014",
                "1 Sep - 30 Sep 2014"
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, frags);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        //TODO: fix the goddamn arrow on the goddamn spinner >:\
        toolbarSpinner.setAdapter(spinnerAdapter);
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "thing clicked " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Bus event called by fragments to change into other fragments
     *
     * @param fragment
     */
    @Subscribe
    public void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragmentManager != null;
        enableToolbarSpinner(false);

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
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

    @Subscribe
    public void changeTitle(String string) {
        if (string.equals(BalanceFragment.TAG)) {
            enableToolbarSpinner(true);
        } else {
            enableToolbarSpinner(false);
            getSupportActionBar().setTitle(string);
        }
    }

    /**
     * Bus event called by AddCreditCardFragment that takes the credit card and then pops the
     * BackStack, this prevents the back button from going to the AddCreditCardFragment again
     * @param creditCard
     */
    @Subscribe
    public void saveCreditCardAndGoBackToLists(CreditCard creditCard) {
        PayOptionsFragment fragment = new PayOptionsFragment();
        //changeFragment(fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.popBackStack();
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
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            // we will start from case 0 is mascot, so we break
            case 0:
                fragment = new BalanceFragment();
                enableToolbarSpinner(true);
                break;
            case 1:
//                Intent ocrCaptureActivity = new Intent(this, CaptureActivity.class);
//                startActivityForResult(ocrCaptureActivity, GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE);
                fragment = new HomeFragment();
                enableToolbarSpinner(false);
                break;
            case 2:
                finish();
                //fragment = new BalanceFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();


            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    private void enableToolbarSpinner(boolean b) {
        if (b) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarSpinner.setVisibility(View.VISIBLE);
        } else {
            toolbarSpinner.setVisibility(View.GONE);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        //mTopBarTitle.setText(mTitle);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE) {
            startBalanceFragment();
        }
    }

    private void startBalanceFragment() {
        Fragment fragment = new BalanceFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

        mDrawerLayout.closeDrawer(mDrawerRelativeLayout);

    }
}