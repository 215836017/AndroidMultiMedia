package com.cakes.demomediacodec.test1;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.cakes.demomediacodec.BaseActivity;
import com.cakes.demomediacodec.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AudioEncodeActivity extends BaseActivity {

    private final String TAG = "AudioEncodeActivity";

    private TextView textTime;
    private Button btnEncodeAudio;

    private AudioEncodeProcessor audioEncodeProcessor;
    private AudioEncodeResult onAudioEncodeListener;

    private boolean isEncoding;
    private int timeCount;

    private final int MSG_UPDATE_TIME = 10;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_TIME) {
                if (isEncoding) {
                    timeCount++;
                    textTime.setText(timeCount + "s");

                    handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_encode);

        initViews();

        onAudioEncodeListener = new AudioEncodeResult();
        audioEncodeProcessor = new AudioEncodeProcessor(onAudioEncodeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != audioEncodeProcessor) {
            audioEncodeProcessor.stopEncode();
            audioEncodeProcessor = null;
        }

        if (null != onAudioEncodeListener) {
            onAudioEncodeListener.release();
        }
    }

    private void initViews() {
        textTime = findViewById(R.id.audio_encode_text_time);
        btnEncodeAudio = findViewById(R.id.audio_encode_btn_start);
        btnEncodeAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrStopEncode();
            }
        });
    }

    private void startOrStopEncode() {

        if (null == audioEncodeProcessor) {
            audioEncodeProcessor = new AudioEncodeProcessor();
        }

        if (isEncoding) {
            isEncoding = false;
            btnEncodeAudio.setText("开始音频编码");
            audioEncodeProcessor.stopEncode();
            audioEncodeProcessor = null;

        } else {
            isEncoding = true;
            btnEncodeAudio.setText("结束音频编码");

            audioEncodeProcessor.start();
            timeCount = 0;
            handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
        }
    }

}