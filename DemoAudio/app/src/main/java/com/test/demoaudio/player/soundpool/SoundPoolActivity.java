package com.test.demoaudio.player.soundpool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.test.demoaudio.R;

/**
 * 使用SoundPool播放音效
 */
public class SoundPoolActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn01, btn02, btn03, btnStop;

    private SoundPoolUtils soundPoolUtils;
    private boolean isPlaying01 = false;
    private boolean isPlaying02 = false;
    private boolean isPlaying03 = false;

    private final int MSG_PLAY_SOUND_01 = 0x11;
    private final int MSG_PLAY_SOUND_02 = 0x12;
    private final int MSG_PLAY_SOUND_03 = 0x13;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            // re-try one time
            if (msg.what == MSG_PLAY_SOUND_01) {
                soundPoolUtils.playSoundForHassium();

            } else if (msg.what == MSG_PLAY_SOUND_02) {
                soundPoolUtils.playSoundForRingSynthShree();

            } else if (msg.what == MSG_PLAY_SOUND_03) {
                soundPoolUtils.playSoundForUmbriel();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_pool);

        init();
    }

    private void init() {
        soundPoolUtils = new SoundPoolUtils(this);

        btn01 = findViewById(R.id.soundPoolAct_btn_play_01);
        btn02 = findViewById(R.id.soundPoolAct_btn_play_02);
        btn03 = findViewById(R.id.soundPoolAct_btn_play_03);
        btnStop = findViewById(R.id.soundPoolAct_btn_stop);

        btn01.setOnClickListener(this);
        btn02.setOnClickListener(this);
        btn03.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.soundPoolAct_btn_play_01:
                if (isPlaying01) {
                    isPlaying01 = true;
                    btn01.setText("播放音效 1");
                    soundPoolUtils.stopSoundForHassium();

                } else {
                    isPlaying01 = false;
                    btn01.setText("停止音效 1");
                    if (soundPoolUtils.playSoundForHassium() <= 0) {
                        handler.sendEmptyMessageDelayed(MSG_PLAY_SOUND_01, 1000);
                    }
                }
                break;

            case R.id.soundPoolAct_btn_play_02:
                if (isPlaying02) {
                    isPlaying02 = true;
                    btn02.setText("播放音效 2");
                    soundPoolUtils.stopSoundForRingSynthShree();

                } else {
                    isPlaying02 = false;
                    btn02.setText("停止音效 2");
                    if (soundPoolUtils.playSoundForRingSynthShree() <= 0) {
                        handler.sendEmptyMessageDelayed(MSG_PLAY_SOUND_02, 1000);
                    }
                }
                break;

            case R.id.soundPoolAct_btn_play_03:
                if (isPlaying03) {
                    isPlaying03 = true;
                    btn03.setText("播放音效 3");
                    soundPoolUtils.stopSoundForUmbriel();

                } else {
                    isPlaying03 = false;
                    btn03.setText("停止音效 3");
                    if (soundPoolUtils.playSoundForUmbriel() <= 0) {
                        handler.sendEmptyMessageDelayed(MSG_PLAY_SOUND_03, 1000);
                    }
                }
                break;

            case R.id.soundPoolAct_btn_stop:
                soundPoolUtils.stopSound();
                btn01.setText("播放音效 1");
                btn02.setText("播放音效 2");
                btn03.setText("播放音效 3");

                isPlaying01 = false;
                isPlaying02 = false;
                isPlaying03 = false;

                break;
        }
    }
}
