package com.fourtails.usuariolecturista;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.parse.Parse;


/**
 * This class initializes all the goodies on the app
 */
public class FirstApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize ORM
        ActiveAndroid.initialize(this);

        // Required - Initialize the Parse SDK
        Parse.initialize(this, getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // If we try to initialize this here the fb login doesn't work :S
        //ParseFacebookUtils.initialize(String.valueOf(R.string.facebook_app_id));

//
//        // Optional - If you don't want to allow Twitter login, you can
        // remove this line (and other related ParseTwitterUtils calls)
//        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key),
//                getString(R.string.twitter_consumer_secret));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
