package com.cakes.demomediacodec.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.cakes.utils.LogUtil;

import java.io.IOException;

public class CameraHelper {

    private final String TAG = "CameraHelper";

    private Camera.PreviewCallback previewCallback;
    private Camera camera;

    public CameraHelper(Camera.PreviewCallback previewCallback) {
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

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (null == camera) {
            return;
        }
        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.setPreviewCallback(previewCallback);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPreview(SurfaceHolder holder) {
        if (null == camera) {
            return;
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(previewCallback);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restartPreview() {
        camera.stopPreview();
        camera.startPreview();
    }

    public void release() {
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
