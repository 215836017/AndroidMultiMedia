package com.test.demoaudio.player.audiotrack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.test.demoaudio.R;

public class AudioTrackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_track);

        findViewById(R.id.btn_play_pcm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPcm();
            }
        });
    }

    private void playPcm() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordAudio/test.pcm";
        AudioTrackManager.getInstance().startPlay(filePath);
    }
}