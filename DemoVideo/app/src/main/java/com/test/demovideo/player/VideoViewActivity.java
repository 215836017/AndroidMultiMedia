package com.test.demovideo.player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.widget.MediaController;
import android.widget.VideoView;

import com.test.demovideo.R;

/**
 * MediaController+VideoView
 */
public class VideoViewActivity extends AppCompatActivity {

    private MediaController mediaController;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        videoView = findViewById(R.id.mv_activity_videoview);

        //加载指定的视频文件
        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testRecord/test.mp4";
        videoView.setVideoPath(mediaPath);

        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        //让VideoView获取焦点
        videoView.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != videoView) {
            videoView.stopPlayback();
            videoView = null;
        }

    }
}
