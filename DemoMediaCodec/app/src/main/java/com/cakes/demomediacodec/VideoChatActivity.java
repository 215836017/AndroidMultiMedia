package com.cakes.demomediacodec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cakes.demomediacodec.R;
import com.cakes.demomediacodec.camera.CameraConfiguration;
import com.cakes.demomediacodec.camera.CameraHelpers;
import com.cakes.demomediacodec.test.AvcDecoder;
import com.cakes.demomediacodec.test.AvcEncoder;
import com.cakes.demomediacodec.test.MediaCodecThread;
import com.cakes.demomediacodec.test.MediaCodecUtil;
import com.cakes.utils.LogUtil;

/**
 * https://github.com/sszhangpengfei/MediaCodecEncodeH264
 * https://blog.csdn.net/a512337862/article/details/72629779
 */
public class VideoChatActivity extends AppCompatActivity {

    private final String TAG = "VideoChatActivity";

    private Context context;
    private SurfaceView surfaceViewCamera;
    private Button btnEncode;
    private CameraHelpers cameraHelpers;
    private AvcEncoder avcEncoder;

    private SurfaceView surfaceViewPlayer;
    private Button btnDecode;
    private AvcDecoder avcDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        context = this;
        initView();
    }

    private void initView() {
        surfaceViewCamera = findViewById(R.id.video_chat_surface_camera);
        btnEncode = findViewById(R.id.video_chat_btn_encode);
        surfaceViewCamera.getHolder().addCallback(surfaceCallbackOfCamera);
        btnEncode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEncodeClick();
            }
        });


        surfaceViewPlayer = findViewById(R.id.video_chat_surface_player);
        surfaceViewPlayer.getHolder().addCallback(surfaceCallbackOfPlay);
        btnDecode = findViewById(R.id.video_chat_btn_decode);
        btnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDecodeClick();
            }
        });

    }


    private boolean isStartEncode;

    private void btnEncodeClick() {
        if (isStartEncode) {
            isStartEncode = false;
            btnEncode.setText("开始编码");

            avcEncoder.stopThread();
        } else {
            isStartEncode = true;
            btnEncode.setText("正在编码中");

//            avcEncoder.startEncoderThread();
        }
    }

    private SurfaceHolder.Callback surfaceCallbackOfCamera = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            cameraHelpers = new CameraHelpers(context, previewCallback);
            if (cameraHelpers.initCameraDevice()) {
                avcEncoder = new AvcEncoder(CameraConfiguration.DEFAULT_PICTURE_WIDTH,
                        CameraConfiguration.DEFAULT_PREVIEW_HEIGHT, CameraConfiguration.DEFAULT_PREVIEW_FPS_MIN, 1);

                cameraHelpers.startPreview(holder);

            } else {
                showToast("open Camera failed");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            LogUtil.i(TAG, "onPreviewFrame() -- data.length = " + data.length);
            if (isStartEncode) {
                avcEncoder.inputYUVToQueue(data);
            }
        }
    };


    private boolean isStartDecode;

    private void btnDecodeClick() {
        if (isStartDecode) {
            isStartDecode = false;
            btnDecode.setText("开始播放");

        } else {
            isStartDecode = true;
            btnDecode.setText("正在播放中");

            avcDecoder.startDecodingThread();
            if (thread != null) {
                thread.start();
            }
        }
    }


    //解码器
    private MediaCodecUtil codecUtil;
    //读取文件解码线程
    private MediaCodecThread thread;
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testDecode.h264";
    private SurfaceHolder.Callback surfaceCallbackOfPlay = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            showToast("解码Surface创建完成");
//            avcDecoder = new AvcDecoder(holder.getSurface());
            if (codecUtil == null) {
                codecUtil = new MediaCodecUtil(holder);
                codecUtil.startCodec();
            }
            if (thread == null) {
                //解码线程第一次初始化
                thread = new MediaCodecThread(codecUtil, path);
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };


    private void showToast(String msg) {
        LogUtil.i(TAG, "showToast() -- msg = " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}