package com.fourtails.usuariolecturista.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;

public class DisplayActivity extends Activity {
    static byte[] imageToShow = null;

    public static String TAG = "DisplayActivity";


    private Handler mHandler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (imageToShow == null) {
            Toast.makeText(this, R.string.no_image, Toast.LENGTH_LONG).show();
            finish();
        } else {
            ImageView iv = new ImageView(this);
            BitmapFactory.Options opts = new BitmapFactory.Options();

            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inMutable = false;
            opts.inSampleSize = 2;

            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageToShow,
                    0,
                    imageToShow.length,
                    opts);
            iv.setImageBitmap(bitmap);

            MainActivity.bus.post(imageToShow);


            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            setContentView(iv);

            mHandler = new Handler();

            // kill the activity after 500 ms
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 500);

        }
    }


}
