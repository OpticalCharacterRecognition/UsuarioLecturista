package com.fourtails.usuariolecturista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import com.facebook.FacebookSdk;
import com.squareup.otto.Bus;


/**
 * This will show an introduction tutorial images to the user so he knows what
 * the app can do, and upon calling another activity it will die.
 */
public class IntroActivity extends ActionBarActivity {

    public static final String PREF_FIRST_TIME = "firstTimePref";


    public static Bus introBus;

    IntroFragmentAdapter mAdapter;

//    @Bind(R.id.pagerInstructions)
//    ViewPager mPager;


    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_instructions);

        FacebookSdk.sdkInitialize(this);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isFirstTime = prefs.getBoolean(PREF_FIRST_TIME, true);
        mHandler = new Handler();

        // the reason we want this on a handler is because it appears to look better if we
        // let the transition finish and then animate our chart
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnyActivity();
            }
        }, 500);


        // if is the first time then we call for the "tutorial" slides
        if (isFirstTime) {
//            setContentView(R.layout.activity_instructions);
//            introBus = new AndroidBus();
//            introBus.register(this);
//            ButterKnife.bind(this);
//
//            mAdapter = new IntroFragmentAdapter(getSupportFragmentManager());
//
//            mPager.setAdapter(mAdapter);

//            mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
//            mIndicator.setViewPager(mPager);
        } else { // otherwise we go directly to the Main activity
            Intent intent = new Intent(this, DispatchActivity.class);
            startAnyActivity();
        }
    }

    /**
     * Bus event called by fragments to start any activity
     */
    public void startAnyActivity() {
        Intent intent = new Intent(this, DispatchActivity.class);
        startActivity(intent);
        finish();
    }

}
