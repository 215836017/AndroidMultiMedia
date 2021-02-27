package com.cakes.demomediacodec.test2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.cakes.demomediacodec.R;

public class AudioDecodeActivity extends AppCompatActivity {

    private final String TAG = "AudioDecodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_decode);

        findViewById(R.id.audio_decode_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDecode();
            }
        });
    }

    private void testDecode() {
//        AudioDecodeProcessor audioDecodeProcessor = new AudioDecodeProcessor();
//        audioDecodeProcessor.run();
        AudioDecodeProcessor2 audioDecodeProcessor2 = new AudioDecodeProcessor2();
        audioDecodeProcessor2.run();
    }
}