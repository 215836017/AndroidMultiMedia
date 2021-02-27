package com.test.demovideo.record;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.test.demovideo.utils.LogUtil;

import java.io.IOException;

public class MyCameraManager {

    private final String TAG = "MyCameraManager";

    public static final int DEFAULT_PREVIEW_WIDTH = 960;
    public static final int DEFAULT_PREVIEW_HEIGHT = 960;

    public static final int DEFAULT_PICTURE_WIDTH = 1600;
    public static final int DEFAULT_PICTURE_HEIGHT = 1200;

    private OnCameraEventListener onCameraEventListener;
    private Camera camera;
    private int cameraId = -1;

    public MyCameraManager(OnCameraEventListener onCameraEventListener) {
        if (null == onCameraEventListener) {
            throw new NullPointerException("onCameraEventListener is null");
        }
        this.onCameraEventListener = onCameraEventListener;
    }

    public void previewFrontCamera(Activity activity, SurfaceHolder holder) {
        openCamera(activity, Camera.CameraInfo.CAMERA_FACING_FRONT, holder);
    }

    public void previewBackCamera(Activity activity, SurfaceHolder holder) {
        openCamera(activity, Camera.CameraInfo.CAMERA_FACING_BACK, holder);
    }

    public int getCameraId() {
        return cameraId;
    }

    private void openCamera(Activity activity, int cameraDeviceId, SurfaceHolder holder) {
        try {
            camera = Camera.open(cameraDeviceId);
            int previewOrientation = OrientationUtil.getCameraDisplayOrientation(activity, cameraDeviceId);
            camera.setDisplayOrientation(previewOrientation);
            cameraId = cameraDeviceId;
        } catch (Exception e) {
            camera = null;
            cameraId = -1;
        }

        if (null == camera) {
            onCameraEventListener.onOpenCameraFailed();
            return;
        }

        startPreview(holder);
    }

    public Camera getCamera() {
        return camera;
    }

    public void startPreview(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
            parameters.setPictureSize(DEFAULT_PICTURE_WIDTH, DEFAULT_PICTURE_HEIGHT);
            camera.setParameters(parameters);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            onCameraEventListener.onPreviewSuccess();
        } catch (IOException e) {
            LogUtil.e(TAG, "startPreview() -- error: " + e.getLocalizedMessage());
            onCameraEventListener.onPreviewFailed();
        }
    }

    public void takePicture(int photoOrientation) {
        if (null != camera) {
            Camera.Parameters parameters = camera.getParameters();
            if (photoOrientation >= 0) {
                parameters.setRotation(photoOrientation);
            }
            camera.setParameters(parameters);
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    LogUtil.i(TAG, "takePicture() --- start");
                }
            }, null, pictureCallback);
        }
    }

    public void release() {
        if (null != camera) {
            try {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.setPreviewDisplay(null);
                camera.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        camera = null;
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            onCameraEventListener.onPictureTaken(data);
        }
    };
}
