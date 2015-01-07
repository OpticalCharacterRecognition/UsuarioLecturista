package com.fourtails.usuariolecturista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class IntermediateActivity extends Activity {

    public static final String COMES_FROM_LOGOUT = "COMES_FROM_LOGOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        final Button logOutButton = (Button) findViewById(R.id.ButtonLogout);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutButtonClicked();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.intermediate, menu);
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

    public void logoutButtonClicked() {
        Intent intent = new Intent(this, LoginActivityBack.class);
        intent.putExtra(COMES_FROM_LOGOUT, true);
        //staticComesFromLogout = true;
        startActivity(intent);
        // TODO: we might not want to start another intent and just finish this one and thus exposing the main login with the logout button
        finish();
    }
}
