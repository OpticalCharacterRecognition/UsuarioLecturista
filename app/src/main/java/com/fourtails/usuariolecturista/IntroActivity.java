package com.fourtails.usuariolecturista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * This will show an introduction tutorial images to the user so he knows what
 * the app can do, and upon calling another activity it will die.
 */
public class IntroActivity extends ActionBarActivity {

    public static final String PREF_FIRST_TIME = "firstTimePref";


    public static Bus introBus;

    IntroFragmentAdapter mAdapter;

    @InjectView(R.id.pagerInstructions)
    ViewPager mPager;

    PageIndicator mIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isFirstTime = prefs.getBoolean(PREF_FIRST_TIME, true);
        // if is the first time then we call for the "tutorial" slides
        if (isFirstTime) {
            setContentView(R.layout.activity_instructions);
            introBus = new AndroidBus();
            introBus.register(this);
            ButterKnife.inject(this);

            mAdapter = new IntroFragmentAdapter(getSupportFragmentManager());

            mPager.setAdapter(mAdapter);

            mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
            mIndicator.setViewPager(mPager);
        } else { // otherwise we go directly to the Main activity
            Intent intent = new Intent(this, DispatchActivity.class);
            startAnyActivity(intent);
        }
    }

    /**
     * Bus event called by fragments to start any activity
     *
     * @param intent the activity we want to start
     */
    @Subscribe
    public void startAnyActivity(Intent intent) {
        startActivity(intent);
        finish();
    }

}
