package com.fourtails.usuariolecturista;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.melnykov.fab.FloatingActionButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ServiceChooserActivity extends Activity {

    @InjectView(R.id.imageViewJmasIcon)
    FloatingActionButton jmasImageView;

    @OnClick(R.id.imageViewJmasIcon)
    public void jmasIconClicked() {
        startActivityWithSharedElementTransition();
    }

    /**
     * will start an activity transition with a shared element animation
     */
    private void startActivityWithSharedElementTransition() {
        Intent intent = new Intent(this, MainActivity.class);

//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                this,
//                jmasImageView,
//                getResources().getString(R.string.transitionJmas)
//        );
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//             this
//        );
//        ActivityCompat.startActivity(this, intent, options.toBundle());
        startActivity(intent);

        finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

// set an exit transition
        getWindow().setExitTransition(new Explode());
        setContentView(R.layout.activity_service_chooser);
        ButterKnife.inject(this);

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

}
