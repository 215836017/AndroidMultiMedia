package com.test.demovideo.player;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;

import com.test.demovideo.R;
import com.test.demovideo.utils.LogUtil;

import java.io.IOException;

/**
 * MediaPlayer+SurfaceView+MediaController
 */
public class MSMActivity extends AppCompatActivity {

    private final String TAG = "MSMActivity";

    private SurfaceView surfaceView;
    private MediaController mediaController;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msm);
        initMediaPlayer();
        initSurface();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mediaController.show();
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.mp4";
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mediaPath);
        } catch (IOException e) {
            LogUtil.i(TAG, "initMediaPlayer() 111111111");
            e.printStackTrace();
        }
    }

    private void initSurface() {
        surfaceView = findViewById(R.id.msm_activity_surfaceview);
        surfaceView.getHolder().addCallback(callback);

        mediaController = new MediaController(this);
//        mediaController.setAnchorView(findViewById(R.id.msm_activity_root_layout));
        mediaController.setAnchorView(surfaceView);
        mediaController.setMediaPlayer(mediaPlayerControl);
        mediaController.setEnabled(true);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            LogUtil.i(TAG, "SurfaceHolder.Callback -- surfaceCreated() 111111111");
            mediaPlayer.setDisplay(holder);
            mediaPlayer.prepareAsync();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    MediaController.MediaPlayerControl mediaPlayerControl = new MediaController.MediaPlayerControl() {
        @Override
        public void start() {
            // 点击了播放按钮后，isPlaying()回调了，但是start()一直没有回调，why?? 郁闷....
            LogUtil.i(TAG, "MediaController.MediaPlayerControl -- start() 111111111");
            if (null != mediaPlayer) {
                LogUtil.i(TAG, "MediaController.MediaPlayerControl -- start() 2222222");
                mediaPlayer.start();
            }
        }

        @Override
        public void pause() {
            if (null != mediaPlayer) {
                mediaPlayer.pause();
            }
        }

        @Override
        public int getDuration() {
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            return 0;
        }

        @Override
        public void seekTo(int pos) {

        }

        @Override
        public boolean isPlaying() {
            boolean isPlay = false;
            if (null != mediaPlayer) {
                isPlay = mediaPlayer.isPlaying();
            }
            LogUtil.i(TAG, "MediaController.MediaPlayerControl -- isPlaying() isPlay = " + isPlay);
            return isPlay;

        }

        @Override
        public int getBufferPercentage() {
            return 0;
        }

        @Override
        public boolean canPause() {
            return false;
        }

        @Override
        public boolean canSeekBackward() {
            return false;
        }

        @Override
        public boolean canSeekForward() {
            return false;
        }

        @Override
        public int getAudioSessionId() {
            return 0;
        }
    };
}
