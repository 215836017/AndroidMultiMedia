package com.test.demovideo.record;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;

import com.test.demovideo.utils.LogUtil;

import java.io.File;

public class MediaRecorderManager {

    private final String TAG = "MediaRecorderManager";

    private final int MAX_VIDEO_DURATION = 30 * 1000; // 30秒

    private Context context;
    private MediaRecorder mediaRecorder;
    private String videoFileTitle;
    private String videoFilePath;

    private boolean isRecording;

    public static final String MEDIA_DIR_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "test" + File.separator;

    public MediaRecorderManager(Context context) {
        this.context = context;
    }

    private void initOutputFilePath() {

        videoFileTitle = MediaConstant.generateImgName(false, System.currentTimeMillis());
        videoFilePath = MEDIA_DIR_PATH + videoFileTitle + MediaConstant.DEFAULT_VIDEO_SUFFIX;
        File file = new File(videoFilePath);
        if (null != file) {
            File parentFile = file.getParentFile();
            if (null != parentFile && !parentFile.exists()) {
                parentFile.mkdirs();
            }
        }
    }

    private void configVideoRecorder(Camera camera, int orientation) {

        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.reset();
        //解锁相机，为MediaRecorder设置相机
        camera.unlock();
        mediaRecorder.setCamera(camera);
        //设置音频源和视频源
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // mediaRecorder.setOrientationHint(90);
        //设置视频的输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置音频的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //设置视频的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置要捕获的视频的帧速率
        mediaRecorder.setVideoFrameRate(15);
        //设置编码比特率 TODO 编码太大的话，会导致录制的视频非常卡顿
        mediaRecorder.setVideoEncodingBitRate(800 * 1024);
        //设置视频大小  todo 注意：这里size要写相机支持的分辨率，  不然的话mediaRecorder.start会报异常
        mediaRecorder.setVideoSize(640, 480);
        if (orientation >= 0) {
            mediaRecorder.setOrientationHint(orientation); // 配置录像的角度
        }
        //设置输出文件
        initOutputFilePath();
        mediaRecorder.setOutputFile(videoFilePath);
        //  mediaRecorder.setMaxDuration(MediaConfiguration.MAX_VIDEO_DURATION);
        mediaRecorder.setOnErrorListener(onErrorListener);
        mediaRecorder.setOnInfoListener(onInfoListener);
    }

    private MediaRecorder.OnErrorListener onErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            LogUtil.w(TAG, "onErrorListener() -- error");
        }
    };

    private MediaRecorder.OnInfoListener onInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                LogUtil.i(TAG, "onInfo() -- video record max duration reached");
            }
        }
    };

    public void startRecordVideo(Camera camera, int orientation) {
        configVideoRecorder(camera, orientation);
        try {
            isRecording = true;
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            isRecording = false;
            LogUtil.e(TAG, "startRecordVideo() --- error: " + e.getMessage());
        }
    }

    public void pauseRecordVideo() {

    }

    public void resumeRecordVideo() {

    }

    public void stopRecordVideo() {
        if (!isRecording) {
            return;
        }
        isRecording = false;
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
        }
        mediaRecorder = null;
    }

}
