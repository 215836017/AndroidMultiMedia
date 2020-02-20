package com.cakes.democamera.oldAPI;

import android.hardware.Camera;

import androidx.appcompat.app.AppCompatActivity;

public class BaseOldCameraActivity extends AppCompatActivity {

    public Camera camera;

    @Override
    protected void onStop() {
        super.onStop();

        if (null != camera) {
            camera.release();
            camera = null;
        }
    }
}
