package com.test.demovideo.player;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.TextureView;

import com.test.demovideo.R;

/**
 * MediaPlayer+TextureView
 */
public class MTActivity extends AppCompatActivity {

    private final String TAG = "MTActivity";

    private TextureView textureView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_t);

        textureView = findViewById(R.id.mt_activity_textureview);
    }
}