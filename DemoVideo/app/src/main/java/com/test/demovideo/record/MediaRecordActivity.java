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

public class MediaRecordActivity extends AppCompatActivity {

    private final String TAG = "MediaRecordActivity";

    private SensorManager sensorManager;
    private Sensor sensor;
    private int sensorRotation = 0;

    private SurfaceView surfaceView;
    private TextView textTime;
    private Button btnPause, btnRecord;
    private Camera camera;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private int timeCount = 0;
    private boolean isRecording = false;

    private MediaRecorderManager mediaRecorderManager;

    private final int MSG_UPDATE_TIME = 0x10;
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
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener, sensor);
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
        btnPause = findViewById(R.id.media_record_act_btn_pause);
        btnRecord = findViewById(R.id.media_record_act_btn_record);

        surfaceView.getHolder().addCallback(surfaceCallback);
    }

    private void release() {
        if (null != mediaRecorderManager) {
            mediaRecorderManager.stopRecordVideo();
        }

        if (null != camera) {
            camera.release();
            camera = null;
        }
    }

    public void mediaRecordBtnClick(View view) {
        if (view.getId() == R.id.media_record_act_btn_pause) {
            pauseRecord();

        } else if (view.getId() == R.id.media_record_act_btn_record) {
            startRecord();
        }
    }

    private void pauseRecord() {
        if (isRecording) {
            isRecording = false;
            mediaRecorderManager.pauseRecordVideo();
            btnPause.setText("继续录制");
        } else {
            isRecording = true;
            mediaRecorderManager.resumeRecordVideo();
            btnPause.setText("暂停录制");
        }
    }

    private void startRecord() {
        btnPause.setText("暂停录制");

        if (isRecording) {
            isRecording = false;
            btnRecord.setText("录制视频");
            btnPause.setClickable(false);
            release();

        } else {
            isRecording = true;
            btnRecord.setText("结束录制");
            btnPause.setClickable(true);
            if (null != camera) {
                int phoneRotation = -1;
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    phoneRotation = (info.orientation - sensorRotation + 360) % 360;
                } else {
                    phoneRotation = (info.orientation + sensorRotation) % 360;
                }
                mediaRecorderManager.startRecordVideo(camera, phoneRotation);
            }
        }
    }

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(cameraId);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

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
