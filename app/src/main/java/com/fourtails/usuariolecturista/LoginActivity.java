package com.fourtails.usuariolecturista;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;


/**
 * All the facebook login happens in the fragment of this activity and calls the Main activity
 */
public class LoginActivity extends FragmentActivity {

    public static String TAG = "LoginActivity";


    public static Bus loginBus;

    private LoginFragment loginFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //this is important you dumb! :(
        loginBus = new Bus(ThreadEnforcer.MAIN);
        loginBus.register(this);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            loginFragment = new LoginFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, loginFragment)
                    .commit();
            Log.d(TAG, "fragment added " + loginFragment.getTag());

        } else {
            // Or set the fragment from restored state info
            loginFragment = (LoginFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
        }
    }

    /**
     * Normally called from main activity to close the entire app when the user
     * presses back button.
     *
     * @param b true if we want to finish this activity
     */
    @Subscribe
    public void closeActivity(Boolean b) {
        if (b) {
            finish();
        }
    }

    /**
     * Bus event called by fragments to start any activity
     *
     * @param intent the activity we want to start
     */
    @Subscribe
    public void startAnyActivity(Intent intent) {
        Log.i(TAG, "opening next activity");
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
