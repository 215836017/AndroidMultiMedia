package com.cakes.demomediacodec.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

public class CameraConfiguration {

    public static final int DEFAULT_PREVIEW_FPS_MAX = 30;
    public static final int DEFAULT_PREVIEW_FPS_MIN = 12;
    public static final int DEFAULT_PREVIEW_WIDTH = 320;
    public static final int DEFAULT_PREVIEW_HEIGHT = 240;
    public static final int DEFAULT_PREVIEW_FORMAT = ImageFormat.NV21;
    public static final int DEFAULT_FPS = 15;

    public static final int DEFAULT_PICTURE_WIDTH = 320;
    public static final int DEFAULT_PICTURE_HEIGHT = 320;
    public static final int DEFAULT_JPEG_QUALITY = 100;

    public static void setDefaultParameters(Camera.Parameters parameters) {

//        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
//        for (Camera.Size size : supportedPreviewSizes) {
//            LogUtil.i("previewSize", "size.width = " + size.width + ", size.height = " + size.height);
//        }

        parameters.setPreviewSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
//        parameters.setPreviewFpsRange(DEFAULT_PREVIEW_FPS_MAX, DEFAULT_PREVIEW_FPS_MIN);
        parameters.setPreviewFrameRate(DEFAULT_PREVIEW_FPS_MIN);
        parameters.setPreviewFormat(DEFAULT_PREVIEW_FORMAT); //设置预览回调的图片格式

        parameters.setPictureSize(DEFAULT_PICTURE_WIDTH, DEFAULT_PICTURE_HEIGHT);
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(DEFAULT_JPEG_QUALITY);

    }

    public static void switchFlash(Camera.Parameters parameters, String flashMode) {
        parameters.setFlashMode(flashMode);
    }

}
