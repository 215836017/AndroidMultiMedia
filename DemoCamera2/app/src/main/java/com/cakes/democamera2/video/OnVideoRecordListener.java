package com.cakes.democamera2.video;

public interface OnVideoRecordListener {

    void onVideoRecordMaxDurationReached();

    void onVideoRecord(String videoFilePath);

    void onVideoRecordError();
}
