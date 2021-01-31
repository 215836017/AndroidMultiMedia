package com.test.demovideo.record;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.test.demovideo.R;
import com.test.demovideo.utils.LogUtil;

import java.io.File;

public class MediaRecordActivity extends AppCompatActivity {

    private final String TAG = "MediaRecordActivity";

    private SurfaceView surfaceView;
    private TextView textTime;
    private Button btnPause, btnRecord;
    private SurfaceHolder surfaceHolder;
    private MediaRecorder mediaRecorder;
    private Camera camera;

    private int timeCount = 0;
    private boolean isRecording = false;

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

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void initViews() {
        surfaceView = findViewById(R.id.media_record_act_sv);
        textTime = findViewById(R.id.media_record_act_tv);
        btnPause = findViewById(R.id.media_record_act_btn_pause);
        btnRecord = findViewById(R.id.media_record_act_btn_record);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // 必须-设置Surface不需要维护自己的缓冲区

    }

    private void release() {
        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if (null != camera) {
            camera.release();
            camera = null;
        }
    }

    private boolean initMediaRecord() {
        if (null != mediaRecorder) {
            return true;
        }
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.reset();
            camera = Camera.open();
            if (camera != null) {
                //设置旋转角度，顺时针方向，因为默认是逆向90度的，这样图像就是正常显示了
                camera.setDisplayOrientation(90);
                camera.unlock();
                mediaRecorder.setCamera(camera);
                LogUtil.e(TAG, "initMediaRecord() -- 11111 = ");
            }
            /*recorder设置部分*/
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            mediaRecorder.setOutputFile(getOutputMediaFile());
            mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
            mediaRecorder.prepare();
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "initMediaRecord() -- error = " + e.getMessage());
        }

        return false;
    }

    public void mediaRecordBtnClick(View view) {
        if (view.getId() == R.id.media_record_act_btn_pause) {
            pauseRecord();

        } else if (view.getId() == R.id.media_record_act_btn_record) {
            startRecord();
        }
    }

    private void pauseRecord() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "此功能需要Android24及以上的版本", Toast.LENGTH_LONG).show();
            return;
        }
        if (isRecording) {
            isRecording = false;
            mediaRecorder.pause();
            btnPause.setText("继续录制");
        } else {
            isRecording = true;
            mediaRecorder.resume();
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
            initMediaRecord();
            mediaRecorder.start();
        }
    }

    /*
     *创建视频存储文件夹 录制好的视频存储在手机外部存储中 以录像时间+mp4格式命名
     * */
    private String getOutputMediaFile() {
        LogUtil.d(TAG, "获取视频存储的位置 ");
        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.mp4";
        if (mediaPath != null) {
            File file = new File(mediaPath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (Exception e) {
            }

        }
        return mediaPath;
    }

}
