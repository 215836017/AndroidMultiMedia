package com.test.demoaudio.player.mediaplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.test.demoaudio.R;

import java.io.IOException;

public class MediaPlayerActivity extends AppCompatActivity {

    private Button btnPlay;
    private Button btnStop;
    private MediaPlayerManager mediaPlayerManager;

    /*
     MediaPlay支持三种音频路径：1.从程序内加载音频文件 2.从设备的sdcard内读取 3.从网络加载
     在raw中添加了一个test.mp3文件，也可以把这个MP3文件copy到sdcard中进行测试
     */
    private final String musicPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/test/test.mp3";

    private static final String MP3_ASSET_TEST = "asset_test.mp3";

    /**
     * 获取asset目录下的mp3文件
     */
    private AssetFileDescriptor getAssetMP3(Context context) {
        try {
            return context.getAssets().openFd(MP3_ASSET_TEST);
        } catch (IOException e) {
            return null;
        }
    }

    private final int MSG_PLAY_FINISH = 0x10;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PLAY_FINISH:
                    doPlayFinish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        btnPlay = findViewById(R.id.mediaPlayerAct_btn_play);
        btnStop = findViewById(R.id.mediaPlayerAct_btn_stop);
        btnStop.setEnabled(false);
        mediaPlayerManager = new MediaPlayerManager(musicPlayerListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mediaPlayerManager && !mediaPlayerManager.isPlaying()) {
            mediaPlayerManager.stop();
            mediaPlayerManager.release();
        }
        mediaPlayerManager = null;
    }

    public void mediaPlayerBtnClick(View v) {
        switch (v.getId()) {
            case R.id.mediaPlayerAct_btn_play:
                if (mediaPlayerManager.isPlaying()) {
                    mediaPlayerManager.pausePlay();
                    btnPlay.setText("开始播放");

                } else {
                    btnPlay.setText("暂停播放");
                    btnStop.setEnabled(true);
                    mediaPlayerManager.play(musicPath);
                }
                break;

            case R.id.mediaPlayerAct_btn_stop:
                mediaPlayerManager.stop();
                doPlayFinish();
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

    private void doPlayFinish() {
        // 播放完成了，可以继续播放下一首

        // 播放完成，也可以释放资源
        btnPlay.setText("开始播放");
        btnStop.setEnabled(false);

        if (null != mediaPlayerManager) {
            mediaPlayerManager.stop();
            mediaPlayerManager.release();
        }
    }
}