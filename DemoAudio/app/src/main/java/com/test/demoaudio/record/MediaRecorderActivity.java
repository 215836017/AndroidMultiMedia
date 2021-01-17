package com.test.demoaudio.record;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.test.demoaudio.BaseActivity;
import com.test.demoaudio.R;
import com.test.demoaudio.utils.LogUtil;
import com.test.demoaudio.utils.TimeUtil;

import java.io.IOException;

/**
 * 使用MediaRecorder进行录音(不录制视频)，并保存在本地
 */
public class MediaRecorderActivity extends BaseActivity {

    private final String TAG = "MediaRecorderActivity";
    private TextView textView;
    private Button btnRecord, btnPause;
    private int timeCount = 0;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private boolean isPaused = false;

    private final int MSG_UPDATE = 0x11;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE) {
                textView.setText(TimeUtil.getTime(timeCount));

                timeCount++;
                handler.sendEmptyMessageDelayed(MSG_UPDATE, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_recorder);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mediaRecorder) {
            if (isRecording) {
                mediaRecorder.stop();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void initViews() {
        textView = findViewById(R.id.mediaRecord_text);
        btnRecord = findViewById(R.id.media_record_btn_start);
        btnPause = findViewById(R.id.media_record_btn_pause);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
                    showToast(TOAST_PERMISSION_AUDIO);
                    return;
                }
                if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showToast(TOAST_PERMISSION_FILE);
                    return;
                }

                initAudio();
                mediaRecordBtnClick();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecord();
            }
        });
    }

    private void initAudio() {

        if (null != mediaRecorder) {
            return;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  // 设置音频录入源
        // 设置录制音频的输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // 设置音频的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置录制音频文件输出文件路径
        String fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/recordAudio/mediaRecord.amr";
        mediaRecorder.setOutputFile(fileName);

        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                LogUtil.e(TAG, "OnErrorListener()-- what = " + what + ", extra = " + extra);
                stopRecord();
            }
        });

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                LogUtil.i(TAG, "OnInfoListener() -- onInfo() -- what = " + what + ", extra = " + extra);
            }
        });
    }

    public void mediaRecordBtnClick() {
        if (isRecording) {
            isRecording = false;
            btnRecord.setText("开始录制");
            btnPause.setClickable(false);
            stopRecord();

        } else {
            isRecording = true;
            btnRecord.setText("停止录制");
            isPaused = false;
            btnPause.setText("暂停录制");
            startRecord();
        }
    }

    private void startRecord() {
        handler.sendEmptyMessage(MSG_UPDATE);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            LogUtil.e(TAG, "startRecord() -- error = " + e.getMessage());
        }
    }

    private void pauseRecord() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            showToast("暂停功能需要Android7.0及以上版本！");
            return;
        }

        if (isPaused) {
            isPaused = false;
            btnPause.setText("暂停录制");
            mediaRecorder.resume();

        } else {
            isPaused = true;
            btnPause.setText("继续录制");
            mediaRecorder.pause();
        }
    }

    private void stopRecord() {
        handler.removeMessages(MSG_UPDATE);
        if (null != mediaRecorder) {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                LogUtil.e(TAG, "stopRecord() -- error = " + e.getMessage());
                mediaRecorder.reset();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

}
