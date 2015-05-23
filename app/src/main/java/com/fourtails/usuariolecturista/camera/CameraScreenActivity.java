package com.fourtails.usuariolecturista.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fourtails.usuariolecturista.R;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CameraScreenActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private Context myContext;

    public static Bus cameraBus;

    @InjectView(R.id.camera_preview)
    LinearLayout cameraPreview;

    @InjectView(R.id.fabHelpPicture)
    FloatingActionButton fabHelpPicture;

    @OnClick(R.id.fabHelpPicture)
    public void helpClicked() {
        Crouton.makeText(this, "Por favor centre los digitos del medidor en el recuadro verde", Style.ALERT).show();
    }

    @OnClick(R.id.fabTakePicture)
    public void takePictureClicked() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success)
                    mCamera.takePicture(null, null, mPicture);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_full_screen);
        ButterKnife.inject(this);


        cameraBus = new AndroidBus();
        cameraBus.register(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
    }

    @Subscribe
    public void endThisActivity(Boolean end) {
        if (end) {
            finish();
        }
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            int cameraId = findBackFacingCamera();
            mCamera = Camera.open(cameraId);
            setCameraDisplayOrientation(this, cameraId, mCamera);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }

    public void initialize() {
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
    }

    public void setCameraDisplayOrientation(Activity activity, int icameraId, Camera camera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        Camera.getCameraInfo(icameraId, cameraInfo);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0; // k

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;

        }

        int result;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // cameraType=CAMERATYPE.FRONT;

            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror

        } else { // back-facing

            result = (cameraInfo.orientation - degrees + 360) % 360;

        }
        // displayRotate=result;
        camera.setDisplayOrientation(result);


    }


    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Most important part, this will get the picture and pass it to the preview
     *
     * @return
     */
    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                CameraDisplayActivity.imageToShow = data;
                Intent intent = new Intent(CameraScreenActivity.this, CameraDisplayActivity.class);
                startActivity(intent);

                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }


    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}