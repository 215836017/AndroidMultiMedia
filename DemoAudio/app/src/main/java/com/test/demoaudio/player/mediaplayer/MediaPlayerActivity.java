package com.test.demoaudio.player.mediaplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.test.demoaudio.R;

/**
 * 使用MediaPlayer播放音频资源， 缺点：只是一个demo，不支持后台播放。
 */
public class MediaPlayerActivity extends AppCompatActivity {

    private Button btnRaw, btnSdcard, btnUri;
    private Button btnPause, btnStop;
    private Button[] buttons = new Button[3];
    private MediaPlayerManager mediaPlayerManager;

    private final int MSG_PLAY_FINISH = 0x10;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PLAY_FINISH:
                    changeButton(-1);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        initViews();
        mediaPlayerManager = new MediaPlayerManager(musicPlayerListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayerManager.stop();
        mediaPlayerManager.release();
    }

    private void initViews() {
        btnRaw = findViewById(R.id.mediaPlayerAct_btn_load_raw);
        btnSdcard = findViewById(R.id.mediaPlayerAct_btn_load_sdcard);
        btnUri = findViewById(R.id.mediaPlayerAct_btn_load_url);
        buttons[0] = btnRaw;
        buttons[1] = btnSdcard;
        buttons[2] = btnUri;

        btnPause = findViewById(R.id.mediaPlayerAct_btn_pause);
        btnStop = findViewById(R.id.mediaPlayerAct_btn_stop);
    }

    public void mediaPlayerBtnClick(View v) {

        switch (v.getId()) {
            case R.id.mediaPlayerAct_btn_load_raw:
                if (mediaPlayerManager.isPlaying()) {
                    changeButton(-1);
                    mediaPlayerManager.stop();
                } else {
                    changeButton(0);
                    String pathRaw = "";
                    mediaPlayerManager.play(pathRaw);
                }
                break;

            case R.id.mediaPlayerAct_btn_load_sdcard:
                if (mediaPlayerManager.isPlaying()) {
                    changeButton(-1);
                    mediaPlayerManager.stop();
                } else {
                    changeButton(1);
                    String pathSdcard = "";
                    mediaPlayerManager.play(pathSdcard);
                }

                break;

            case R.id.mediaPlayerAct_btn_load_url:
                if (mediaPlayerManager.isPlaying()) {
                    changeButton(-1);
                    mediaPlayerManager.stop();
                } else {
                    changeButton(2);
                    String pathUrl = "";
                    mediaPlayerManager.play(pathUrl);
                }
                break;

            case R.id.mediaPlayerAct_btn_pause:
                if (mediaPlayerManager.isPlaying()) {
                    mediaPlayerManager.pausePlay();
                    btnPause.setText("继续播放");

                } else {
                    mediaPlayerManager.resumePlay();
                    btnPause.setText("暂停播放");
                }
                break;

            case R.id.mediaPlayerAct_btn_stop:
                mediaPlayerManager.stop();
                changeButton(-1);
                break;
        }
    }

    MediaPlayerManager.OnMusicPlayerListener musicPlayerListener = new MediaPlayerManager.OnMusicPlayerListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onComplete() {
            handler.sendEmptyMessage(MSG_PLAY_FINISH);
        }

        @Override
        public void onError(int errorCode) {

        }
    };

    private void changeButton(int index) {
        if (index == -1) {
            btnPause.setClickable(false);
            btnPause.setText("暂停播放");
            btnStop.setClickable(false);
            btnRaw.setText("播放程序自带的音频");
            btnSdcard.setText("从手机存储中加载音频");
            btnUri.setText("在线播放URL指定的音频");
        } else {
            btnPause.setClickable(true);
            btnPause.setText("暂停播放");
            btnStop.setClickable(true);

            if (index == 0) {
                btnRaw.setText("停止播放");
                btnSdcard.setText("从手机存储中加载音频");
                btnUri.setText("在线播放URL指定的音频");
            } else if (index == 1) {
                btnRaw.setText("播放程序自带的音频");
                btnSdcard.setText("停止播放");
                btnUri.setText("在线播放URL指定的音频");
            } else if (index == 2) {
                btnRaw.setText("播放程序自带的音频");
                btnSdcard.setText("从手机存储中加载音频");
                btnUri.setText("停止播放");
            }
        }
    }
}