package com.fourtails.usuariolecturista;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerItem;
import com.fourtails.usuariolecturista.navigationDrawer.NavDrawerListAdapter;
import com.fourtails.usuariolecturista.ocr.CaptureActivity;
import com.fourtails.usuariolecturista.utilities.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.fourtails.usuariolecturista.LoginFragment.PREF_FACEBOOK_PROFILE_ID;
import static com.fourtails.usuariolecturista.LoginFragment.PREF_FACEBOOK_PROFILE_NAME;


public class MainDrawerActivity extends ActionBarActivity implements
        HomeFragment.OnFragmentInteractionListener,
        BalanceFragment.OnFragmentInteractionListener,
        PayOptionsFragment.OnFragmentInteractionListener,
        AddCreditCardFragment.OnFragmentInteractionListener {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    // we need this because when we try to close the drawer we have to pass the container view
    private RelativeLayout mDrawerRelativeLayout;

    private ImageView imageViewFacebookProfilePic;
    private TextView textViewFacebookName;

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

    public static final int GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE = 00233;


    @SuppressWarnings("ConstantConditions")
    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        /**toolBar **/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Usuario Lecturista");

        mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);


        mDrawerRelativeLayout = (RelativeLayout) findViewById(R.id.linearLayoutDrawer);

        imageViewFacebookProfilePic = (ImageView) findViewById(R.id.imageViewFBProfileImage);
        textViewFacebookName = (TextView) findViewById(R.id.textViewFacebookName);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String facebookId = prefs.getString(PREF_FACEBOOK_PROFILE_ID, "");
        String facebookName = prefs.getString(PREF_FACEBOOK_PROFILE_NAME, "");

        Picasso.with(this)
                .load("https://graph.facebook.com/"
                        + facebookId + "/picture?type=large")
                .transform(new CircleTransform())
                .into(imageViewFacebookProfilePic);

        textViewFacebookName.setText(facebookName);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Balance
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Dummy
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // OCR scanner
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));

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

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

//    @Override
//    public void onFragmentInteraction(String id) {
//
//    }

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
                break;
            case 1:
                Intent ocrCaptureActivity = new Intent(this, CaptureActivity.class);
                startActivityForResult(ocrCaptureActivity, GO_BACK_TO_MAIN_DRAWER_AND_OPEN_BALANCE_CODE);
                //fragment = new HomeFragment();
                break;
            case 2:
                finish();
                //fragment = new BalanceFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
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
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

        mDrawerLayout.closeDrawer(mDrawerRelativeLayout);

    }
}