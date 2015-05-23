package com.fourtails.usuariolecturista.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Displays a confirm activity to the user
 */
public class CameraDisplayActivity extends Activity {
    static byte[] imageToShow = null;

    public static String TAG = "DisplayActivity";

    private Bitmap rotatedBitmap;

    private boolean isBitmapFinished = false;

    @InjectView(R.id.imageViewDisplayConfirmPreview)
    ImageView imageViewDisplayConfirm;

    @OnClick(R.id.fabAccept)
    public void acceptClicked() {
        if (isBitmapFinished && rotatedBitmap != null) {
            MainActivity.bus.post(rotatedBitmap);
            CameraScreenActivity.cameraBus.post(true);
            finish();
        }
    }

    @OnClick(R.id.fabCancel)
    public void cancelClicked() {
        finish();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        isBitmapFinished = false;

        ButterKnife.inject(this);


        if (imageToShow == null) {
            Toast.makeText(this, R.string.no_image, Toast.LENGTH_LONG).show();
            finish();
        } else {
            BitmapFactory.Options opts = new BitmapFactory.Options();

            opts.inPurgeable = true;
            opts.inInputShareable = true;
            opts.inMutable = false;
            opts.inSampleSize = 2;


            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageToShow,
                    0,
                    imageToShow.length,
                    opts);
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageViewDisplayConfirm.setImageBitmap(rotatedBitmap);

            isBitmapFinished = true;

        }
    }


}
