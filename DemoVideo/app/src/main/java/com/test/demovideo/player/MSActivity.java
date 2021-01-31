package com.test.demovideo.player;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.test.demovideo.R;

import java.io.IOException;

/**
 * MediaPlayer+SurfaceView+自定义控制器
 */
public class MSActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private Button btnPlay, btnBack, btnForward;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ms);

        initViews();

        initMedias();

        setVideoHolder();

    }

    private void initViews() {
        surfaceView = findViewById(R.id.ms_activity_surfaceview);
        btnPlay = findViewById(R.id.ms_activity_btn_play);
        btnBack = findViewById(R.id.ms_activity_btn_back);
        btnForward = findViewById(R.id.ms_activity_btn_forward);
    }

    private void initMedias() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.setOnErrorListener(onErrorListener);
        mediaPlayer.setOnInfoListener(onInfoListener);
        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        mediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);

        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.mp4";
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mediaPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setVideoHolder() {
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(callback);
    }

    public void msActBtnClick(View view) {
        if (view.getId() == R.id.ms_activity_btn_play) {
            playVideo();

        } else if (view.getId() == R.id.ms_activity_btn_back) {


        } else if (view.getId() == R.id.ms_activity_btn_forward) {

        }
    }

    private void playVideo() {
        mediaPlayer.start();
    }

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

        }
    };

    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {

        }
    };

    MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {

        }
    };

    MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

        }
    };

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
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
}
