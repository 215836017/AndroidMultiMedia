package com.test.demovideo.record;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.test.demovideo.R;
import com.test.demovideo.utils.LogUtil;

public class MediaRecordActivity extends AppCompatActivity {

    private final String TAG = "MediaRecordActivity";

    private SensorManager sensorManager;
    private Sensor sensor;
    private int sensorRotation = 0;

    private SurfaceView surfaceView;
    private TextView textTime;
    private Button btnRecord;

    private int timeCount = 0;
    private boolean isRecording = false;
    private MyCameraManager cameraManager;
    private MediaRecorderManager mediaRecorderManager;

    private final int MSG_OPEN_CAMERA_FAIL = 10;
    private final int MSG_PREVIEW_CAMERA_FAIL = 11;
    private final int MSG_PREVIEW_CAMERA_SUCCESS = 12;
    private final int MSG_UPDATE_TIME = 0x13;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_TIME) {
                textTime.setText(timeCount + "s");
                if (isRecording) {
                    timeCount++;
                    handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);

        initSensor();
        initViews();

        mediaRecorderManager = new MediaRecorderManager(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != sensorManager) {
            sensorManager.unregisterListener(sensorEventListener, sensor);
        }
        release();
    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initViews() {
        surfaceView = findViewById(R.id.media_record_act_sv);
        textTime = findViewById(R.id.media_record_act_tv);
        btnRecord = findViewById(R.id.media_record_act_btn_record);

        surfaceView.getHolder().addCallback(surfaceCallback);
    }

    private void release() {
        if (null != mediaRecorderManager) {
            mediaRecorderManager.stopRecordVideo();
        }

        if (null != cameraManager) {
            cameraManager.release();
        }
    }

    public void mediaRecordBtnClick(View view) {
        if (view.getId() == R.id.media_record_act_btn_record) {
            startRecord();
        }
    }

    private void pauseRecord() {
        if (isRecording) {
            isRecording = false;
            mediaRecorderManager.pauseRecordVideo();
        } else {
            isRecording = true;
            mediaRecorderManager.resumeRecordVideo();
        }
    }

    private void startRecord() {

        if (isRecording) {
            isRecording = false;
            btnRecord.setText("录制视频");
            if (null != mediaRecorderManager) {
                mediaRecorderManager.stopRecordVideo();
            }
        } else {
            isRecording = true;
            btnRecord.setText("结束录制");

            int phoneRotation = -1;
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraManager.getCameraId(), info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                phoneRotation = (info.orientation - sensorRotation + 360) % 360;
            } else {
                phoneRotation = (info.orientation + sensorRotation) % 360;
            }

            mediaRecorderManager = new MediaRecorderManager(this);
            mediaRecorderManager.startRecordVideo(cameraManager.getCamera(), phoneRotation);
        }
    }

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (null == cameraManager) {
                cameraManager = new MyCameraManager(onCameraEventListener);
            }
            cameraManager.previewFrontCamera(MediaRecordActivity.this, holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private OnCameraEventListener onCameraEventListener = new OnCameraEventListener() {

        @Override
        public void onOpenCameraFailed() {
            handler.sendEmptyMessage(MSG_OPEN_CAMERA_FAIL);
        }

        @Override
        public void onPreviewSuccess() {
            handler.sendEmptyMessage(MSG_PREVIEW_CAMERA_SUCCESS);
        }

        @Override
        public void onPreviewFailed() {
            handler.sendEmptyMessage(MSG_PREVIEW_CAMERA_FAIL);
        }

        @Override
        public void onPictureTaken(byte[] data) {
            LogUtil.i(TAG, "onPictureTaken() --- data.len = " + data.length);
            // to save image file
        }
    };


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            sensorRotation = AngleUtil.getSensorAngle(event.values[0], event.values[1]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
