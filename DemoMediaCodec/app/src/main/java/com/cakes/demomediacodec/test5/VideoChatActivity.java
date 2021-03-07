package com.cakes.demomediacodec.test5;

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
import com.cakes.demomediacodec.test3.VideoHardEncoder;
import com.cakes.demomediacodec.test4.VideoDecodeThread;
import com.cakes.demomediacodec.test4.VideoHardDecoder;
import com.cakes.demomediacodec.test4.MediaCodecUtil;
import com.cakes.utils.LogUtil;

/**
 * 使用旧的Camera API
 * https://github.com/sszhangpengfei/MediaCodecEncodeH264
 * https://blog.csdn.net/a512337862/article/details/72629779
 */
public class VideoChatActivity extends AppCompatActivity {

    private final String TAG = "VideoChatActivity";

    private Context context;
    private SurfaceView surfaceViewCamera;
    private Button btnEncode;
    private CameraHelpers cameraHelpers;
    private VideoHardEncoder videoHardEncoder;

    private SurfaceView surfaceViewPlayer;
    private Button btnDecode;
    private VideoHardDecoder videoHardDecoder;

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

            videoHardEncoder.stopThread();
        } else {
            isStartEncode = true;
            btnEncode.setText("正在编码中");

//            videoHardEncoder.startEncoderThread();
        }
    }

    private SurfaceHolder.Callback surfaceCallbackOfCamera = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            cameraHelpers = new CameraHelpers(previewCallback);
            if (cameraHelpers.initCameraDevice()) {
                videoHardEncoder = new VideoHardEncoder(CameraConfiguration.DEFAULT_PICTURE_WIDTH,
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
                videoHardEncoder.inputYUVToQueue(data);
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

            videoHardDecoder.startDecodingThread();
            if (thread != null) {
                thread.start();
            }
        }
    }

    //解码器
    private MediaCodecUtil codecUtil;
    //读取文件解码线程
    private VideoDecodeThread thread;
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testDecode.h264";
    private SurfaceHolder.Callback surfaceCallbackOfPlay = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            showToast("解码Surface创建完成");
//            videoHardDecoder = new VideoHardDecoder(holder.getSurface());
            if (codecUtil == null) {
                codecUtil = new MediaCodecUtil(holder);
                codecUtil.startCodec();
            }
            if (thread == null) {
                //解码线程第一次初始化
                thread = new VideoDecodeThread(codecUtil, path);
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