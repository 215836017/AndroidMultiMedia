package com.cakes.demomediacodec.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.cakes.utils.LogUtil;

import java.io.IOException;

public class CameraHelpers {

    private final String TAG = "CameraHelpers";

    private Context context;
    private Camera.PreviewCallback previewCallback;
    private Camera camera;
    private boolean isCanTakePicture = false;

    public CameraHelpers(Context context, Camera.PreviewCallback previewCallback) {
        this.context = context;
        this.previewCallback = previewCallback;
    }

    public boolean initCameraDevice() {
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            Camera.Parameters cameraParameters = camera.getParameters();
            CameraConfiguration.setDefaultParameters(cameraParameters);
            camera.setParameters(cameraParameters);

            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "initCameraDevice -- error:" + e.getMessage());
            if (null != camera) {
                camera.release();
            }
            camera = null;
            return false;
        }
    }

    public void startPreview(SurfaceHolder holder) {
        try {
//            camera.setPreviewTexture(surface);
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(previewCallback);

            camera.startPreview();
//            isCanTakePicture = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void restartPreview() {
        camera.stopPreview();
        camera.startPreview();
    }


    public void takePicture(Camera.PictureCallback mPictureCallback) {
        if (isCanTakePicture && null != camera) {
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    LogUtil.i(TAG, "takePicture() --- start");
                }
            }, null, mPictureCallback);
        }
    }

    public void release() {
        isCanTakePicture = false;
        if (null == camera) {
            return;
        }
        try {
            camera.stopPreview();
            camera.setPreviewDisplay(null);
            camera.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera = null;
    }
}
