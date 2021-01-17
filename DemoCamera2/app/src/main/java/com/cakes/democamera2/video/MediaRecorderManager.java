package com.cakes.democamera2.video;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.Surface;

import com.cakes.democamera2.LogUtil;

import java.io.File;
import java.io.IOException;

public class MediaRecorderManager {

    private final String TAG = "MediaRecorderManager";

    private Context context;
    private MediaRecorder mediaRecorder;
    private String videoFileTitle;
    private String videoFilePath;

    private boolean isRecording;
    private OnVideoRecordListener onVideoRecordListener;

    private boolean isConfigFinish;

    public MediaRecorderManager(Context context, OnVideoRecordListener onVideoRecordListener) {
        this.context = context;
        this.onVideoRecordListener = onVideoRecordListener;
    }

    private void initOutputFilePath() {

        videoFileTitle = MediaConstant.generateImgName(false, System.currentTimeMillis());
        videoFilePath = MediaConstant.MEDIA_DIR_PATH + videoFileTitle + MediaConstant.DEFAULT_VIDEO_SUFFIX;
        File file = new File(videoFilePath);
        if (null != file) {
            File parentFile = file.getParentFile();
            if (null != parentFile && !parentFile.exists()) {
                parentFile.mkdirs();
            }

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void configVideoRecorder(int width, int height) {

        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        try {
            mediaRecorder.reset();
            //解锁相机，为MediaRecorder设置相机
//        camera.unlock();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mediaRecorder.setPreviewDisplay(surface);  // camera2
//        } else {
//            mediaRecorder.setCamera(camera); // camera1
//        }
            //设置音频源和视频源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // camera1
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);  // camera2
            // mediaRecorder.setOrientationHint(90);
            //设置视频的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置视频的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //设置要捕获的视频的帧速率
            mediaRecorder.setVideoFrameRate(15);
            //设置编码比特率
            mediaRecorder.setVideoEncodingBitRate(MediaConfiguration.DEFAULT_VIDEO_BIT_RATE);
            //设置视频大小
            mediaRecorder.setVideoSize(width, height);
//        mediaRecorder.setOrientationHint(270);
            //设置输出文件
            initOutputFilePath();
            mediaRecorder.setOutputFile(videoFilePath);
//            mediaRecorder.setMaxDuration(MediaConfiguration.MAX_VIDEO_DURATION);
            mediaRecorder.setOnErrorListener(onErrorListener);
            mediaRecorder.setOnInfoListener(onInfoListener);

            mediaRecorder.prepare();
            isConfigFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private MediaRecorder.OnErrorListener onErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            LogUtil.w(TAG, "onErrorListener() -- error");
            if (null != onVideoRecordListener) {
                onVideoRecordListener.onVideoRecordError();
            }
        }
    };

    private MediaRecorder.OnInfoListener onInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED && null != onVideoRecordListener) {
                LogUtil.i(TAG, "onInfo() -- video record max duration reached");
                onVideoRecordListener.onVideoRecordMaxDurationReached();
            }
        }
    };

    public void startRecordVideo() {
        if (!isConfigFinish) {
            LogUtil.w(TAG, "startRecordVideo() -- error: isConfigFinish = false");
            return;
        }
        try {
            isRecording = true;
            mediaRecorder.start();
            LogUtil.e(TAG, "startRecordVideo() --- after call start()");
        } catch (Exception e) {
            isRecording = false;
            LogUtil.e(TAG, "startRecordVideo() --- error: " + e.getMessage());
            if (null != onVideoRecordListener) {
                onVideoRecordListener.onVideoRecordError();
            }
        }
    }

    public Surface getSurface() {
        if (null != mediaRecorder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return mediaRecorder.getSurface();
            }
        }
        return null;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void stopRecordVideo() {
        if (!isRecording) {
            return;
        }
        isRecording = false;
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            LogUtil.w(TAG, "stopRecordVideo() -- call mediaRecorder.stop()");
            mediaRecorder.stop();
            LogUtil.w(TAG, "stopRecordVideo() -- call mediaRecorder.reset()");
            mediaRecorder.reset();
            mediaRecorder.release();
        }
        mediaRecorder = null;
        isConfigFinish = false;
        if (null != onVideoRecordListener) {
            onVideoRecordListener.onVideoRecord(videoFilePath);
        }
    }

}
