package com.fourtails.usuariolecturista;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;

import com.melnykov.fab.FloatingActionButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ServiceChooserActivity extends Activity {

    @InjectView(R.id.imageViewJmasIcon)
    FloatingActionButton jmasFAB;

    @OnClick(R.id.imageViewJmasIcon)
    public void jmasIconClicked() {
        startActivityWithSharedElementTransition();
    }

    /**
     * will start an activity transition with a shared element animation
     */
    private void startActivityWithSharedElementTransition() {
        Intent intent = new Intent(this, MainActivity.class);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                jmasFAB, getResources().getString(R.string.transitionJmas)
        );
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_service_chooser);
        ButterKnife.inject(this);

    }


}
