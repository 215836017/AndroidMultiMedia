package com.cakes.demomediacodec.test3;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.cakes.demomediacodec.BaseActivity;
import com.cakes.demomediacodec.R;
import com.cakes.demomediacodec.camera.CameraConfiguration;
import com.cakes.demomediacodec.camera.CameraHelper;

public class VideoEncodeActivity extends BaseActivity {

    private final String TAG = "VideoEncodeActivity";

    private final String TEXT_STOP_ENCODE = "停止编码";
    private final String TEXT_START_ENCODE = "开始编码";

    private TextureView textureView;
    private Button btnEncode;

    private boolean isEncoding;
    private CameraHelper cameraHelper;
    private VideoHardEncoder videoHardEncoder;

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
            videoHardEncoder.stopThread();

        } else {
            isEncoding = true;
            btnEncode.setText(TEXT_STOP_ENCODE);
            videoHardEncoder.startEncoderThread();
        }
    }

    private void startPreviewCamera(SurfaceTexture surfaceTexture) {
        cameraHelper = new CameraHelper(previewCallback);
        if (cameraHelper.initCameraDevice()) {
            cameraHelper.startPreview(surfaceTexture);

            videoHardEncoder = new VideoHardEncoder(CameraConfiguration.DEFAULT_PICTURE_WIDTH,
                    CameraConfiguration.DEFAULT_PREVIEW_HEIGHT, CameraConfiguration.DEFAULT_PREVIEW_FPS_MIN);
        } else {
            showToast(TAG, "open Camera failed");
        }
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (isEncoding) {
                videoHardEncoder.inputYUVToQueue(data);
            }
        }
    };
}