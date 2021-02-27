package com.test.demovideo.record;

public interface OnCameraEventListener {

    void onOpenCameraFailed();

    void onPreviewSuccess();

    void onPreviewFailed();

    void onPictureTaken(byte[] data);
}
