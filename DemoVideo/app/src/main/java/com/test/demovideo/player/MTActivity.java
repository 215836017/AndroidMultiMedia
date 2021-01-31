package com.test.demovideo.player;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.test.demovideo.R;

import java.io.IOException;

/**
 * MediaPlayer+TextureView+自定义控制器
 */
public class MTActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mt);

        initMediaPlayer();
        initTextureView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void mtActivityBtnClick(View view) {
        if (view.getId() == R.id.mt_activity_btn_play) {
            mediaPlayer.start();
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.mp4";
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mediaPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTextureView() {
        textureView = findViewById(R.id.mt_activity_textureview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mediaPlayer.setSurface(new Surface(surface));
            mediaPlayer.prepareAsync();
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
