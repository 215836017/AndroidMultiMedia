package com.test.demovideo.player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;

import com.test.demovideo.R;

import java.io.IOException;

/**
 * MediaPlayer+TextureView
 * https://www.jianshu.com/p/9c74c61ab441
 */
public class MTActivity extends AppCompatActivity {

    private final String TAG = "MTActivity";

    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private Surface surface;
    private final int MSG_TEXTURE_AVAILABLE = 10;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_TEXTURE_AVAILABLE) {
                initMediaPlayer();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_t);

        textureView = findViewById(R.id.mt_activity_textureview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private void initMediaPlayer() {
        if (null != mediaPlayer) {
            return;
        }
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.setOnErrorListener(onErrorListener);
        mediaPlayer.setOnInfoListener(onInfoListener);
        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        mediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mediaPlayer.setSurface(surface);
//        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.mp4";
        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.3gp";
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mediaPath);

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            if (null != mp) {
                mp.start();
            }
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

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);
            handler.sendEmptyMessage(MSG_TEXTURE_AVAILABLE);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
}