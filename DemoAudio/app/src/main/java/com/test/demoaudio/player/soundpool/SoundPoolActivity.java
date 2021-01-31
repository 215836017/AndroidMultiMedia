package com.test.demoaudio.player.soundpool;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.test.demoaudio.R;

public class SoundPoolActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn01, btn02, btn03, btnStop;

    private SoundPoolUtil soundPoolUtil;
    private boolean isPlaying01 = false;
    private boolean isPlaying02 = false;
    private boolean isPlaying03 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_pool);

        init();
    }

    private void init() {
        soundPoolUtil = new SoundPoolUtil(this);

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
                    isPlaying01 = false;
                    btn01.setText("播放音效 1");
                    soundPoolUtil.stopSound(soundPoolUtil.playIdOne);

                } else {
                    isPlaying01 = true;
                    btn01.setText("停止音效 1");
                    soundPoolUtil.playSoundOne();
                }
                break;

            case R.id.soundPoolAct_btn_play_02:
                if (isPlaying02) {
                    isPlaying02 = false;
                    btn02.setText("播放音效 2");
                    soundPoolUtil.stopSound(soundPoolUtil.playIdTwo);

                } else {
                    isPlaying02 = true;
                    btn02.setText("停止音效 2");
                    soundPoolUtil.playSoundTwo();
                }
                break;

            case R.id.soundPoolAct_btn_play_03:
                if (isPlaying03) {
                    isPlaying03 = true;
                    btn03.setText("播放音效 3");
                    soundPoolUtil.stopSound(soundPoolUtil.playIdThree);

                } else {
                    isPlaying03 = false;
                    btn03.setText("停止音效 3");
                    soundPoolUtil.playSoundThree(this);
                }
                break;

            case R.id.soundPoolAct_btn_stop:
                soundPoolUtil.stopSound();
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
