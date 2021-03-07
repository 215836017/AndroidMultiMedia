package com.cakes.demomediacodec.test3;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.cakes.demomediacodec.R;

public class VideoEncodeActivity extends AppCompatActivity {

    private final String TAG = "VideoEncodeActivity";

    private final String TEXT_STOP_ENCODE = "停止编码";
    private final String TEXT_START_ENCODE = "开始编码";

    private TextureView textureView;
    private Button btnEncode;

    private boolean isEncoding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_encode);

        textureView = findViewById(R.id.video_en_texture);
        btnEncode = findViewById(R.id.video_en_button);
        isEncoding = false;
        btnEncode.setText(TEXT_START_ENCODE);

        btnEncode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testEncodeVideo();
            }
        });

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startPreviewCamera(surface);
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
        });
    }

    private void testEncodeVideo() {
        if (isEncoding) {
            isEncoding = false;
            btnEncode.setText(TEXT_START_ENCODE);

        } else {
            isEncoding = true;
            btnEncode.setText(TEXT_STOP_ENCODE);
        }
    }

    private void startPreviewCamera(SurfaceTexture surfaceTexture) {

    }
}