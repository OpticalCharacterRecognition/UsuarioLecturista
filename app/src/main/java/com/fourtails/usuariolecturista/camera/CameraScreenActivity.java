/***
 Copyright (c) 2013-2014 CommonsWare, LLC

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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.fourtails.usuariolecturista.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraScreenActivity extends Activity implements
        DemoCameraFragment.Contract {
    DemoCameraFragment frag = null;


    @OnClick(R.id.buttonRed)
    public void redClicked() {
        int pic = 1;
        frag.takeSimplePicture(pic);
    }

    @OnClick(R.id.buttonBlue)
    public void blueClicked() {
        int pic = 2;
        frag.takeSimplePicture(pic);
    }

    @OnClick(R.id.buttonGreen)
    public void greenClicked() {
        int pic = 3;
        frag.takeSimplePicture(pic);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_full_screen);
        ButterKnife.inject(this);


        frag = (DemoCameraFragment) getFragmentManager().findFragmentById(R.id.camera_preview);
    }

    @Override
    public boolean isSingleShotMode() {
        return (false);
    }

    @Override
    public void setSingleShotMode(boolean mode) {
        // hardcoded, unused
    }

    public void takePicture(View v) {
        //frag.takeSimplePicture();
    }
}
