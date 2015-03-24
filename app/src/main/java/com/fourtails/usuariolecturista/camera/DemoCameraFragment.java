/***
 7  Copyright (c) 2013 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.fourtails.usuariolecturista.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraUtils;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.fourtails.usuariolecturista.R;

public class DemoCameraFragment extends CameraFragment implements
        OnSeekBarChangeListener {
    private static final String KEY_USE_FFC =
            "com.commonsware.cwac.camera.demo.USE_FFC";
    private boolean singleShotProcessing = false;
    private SeekBar zoom = null;
    private long lastFaceToast = 0L;
    String flashMode = null;

    public static int kindOfPic = 0;


    static DemoCameraFragment newInstance(boolean useFFC) {
        DemoCameraFragment f = new DemoCameraFragment();
        Bundle args = new Bundle();

        args.putBoolean(KEY_USE_FFC, useFFC);
        f.setArguments(args);

        return (f);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setHasOptionsMenu(true);

        SimpleCameraHost.Builder builder =
                new SimpleCameraHost.Builder(new DemoCameraHost(getActivity()));

        setHost(builder.useFullBleedPreview(true).build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View cameraView =
                super.onCreateView(inflater, container, savedInstanceState);
        View results = inflater.inflate(R.layout.fragment_camera, container, false);

        ((ViewGroup) results.findViewById(R.id.camera)).addView(cameraView);
        zoom = (SeekBar) results.findViewById(R.id.zoom);
        zoom.setKeepScreenOn(true);


        return (results);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().invalidateOptionsMenu();
    }


    boolean isSingleShotProcessing() {
        return (singleShotProcessing);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (fromUser) {
            zoom.setEnabled(false);
            zoomTo(zoom.getProgress()).onComplete(new Runnable() {
                @Override
                public void run() {
                    zoom.setEnabled(true);
                }
            }).go();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // ignore
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // ignore
    }


    Contract getContract() {
        return ((Contract) getActivity());
    }

    void takeSimplePicture(int type) {
        kindOfPic = type;
        getContract().setSingleShotMode(true);
        singleShotProcessing = true;

        PictureTransaction xact = new PictureTransaction(getHost());

//        if (flashItem != null && flashItem.isChecked()) {
//            xact.flashMode(flashMode);
//        }

        takePicture(xact);
    }

    interface Contract {
        boolean isSingleShotMode();

        void setSingleShotMode(boolean mode);
    }

    class DemoCameraHost extends SimpleCameraHost {
        boolean supportsFaces = false;

        public DemoCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        public boolean useFrontFacingCamera() {
            if (getArguments() == null) {
                return (false);
            }

            return (getArguments().getBoolean(KEY_USE_FFC));
        }

        @Override
        public boolean useSingleShotMode() {
            return true;
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            singleShotProcessing = false;

            DisplayActivity.imageToShow = image;
            startActivity(new Intent(getActivity(), DisplayActivity.class));
            getActivity().finish();
        }

        @Override
        public void autoFocusAvailable() {
        }

        @Override
        public void autoFocusUnavailable() {
        }

        @Override
        public void onCameraFail(CameraHost.FailureReason reason) {
            super.onCameraFail(reason);

            Toast.makeText(getActivity(),
                    getResources().getString(R.string.camera_sorry),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public Parameters adjustPreviewParameters(Parameters parameters) {
            flashMode =
                    CameraUtils.findBestFlashModeMatch(parameters,
                            Camera.Parameters.FLASH_MODE_RED_EYE,
                            Camera.Parameters.FLASH_MODE_AUTO,
                            Camera.Parameters.FLASH_MODE_ON);

            if (doesZoomReallyWork() && parameters.getMaxZoom() > 0) {
                zoom.setMax(parameters.getMaxZoom());
                zoom.setOnSeekBarChangeListener(DemoCameraFragment.this);
            } else {
                zoom.setEnabled(false);
            }

//            if (parameters.getMaxNumDetectedFaces() > 0) {
//                supportsFaces = true;
//            } else {
////                Toast.makeText(getActivity(),
////                        "Face detection not available for this camera",
////                        Toast.LENGTH_LONG).show();
//            }

            return (super.adjustPreviewParameters(parameters));
        }

        @Override
        @TargetApi(16)
        public void onAutoFocus(boolean success, Camera camera) {
            super.onAutoFocus(success, camera);

        }

        @Override
        public boolean mirrorFFC() {
            return false;
        }
    }
}