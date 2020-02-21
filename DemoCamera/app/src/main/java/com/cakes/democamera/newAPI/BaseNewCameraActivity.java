package com.cakes.democamera.newAPI;


import android.hardware.camera2.CameraManager;

import androidx.appcompat.app.AppCompatActivity;

public class BaseNewCameraActivity extends AppCompatActivity {

    public CameraManager cameraManager;

    @Override
    protected void onStop() {
        super.onStop();

        if (null != cameraManager) {
            cameraManager = null;
        }
    }
}
