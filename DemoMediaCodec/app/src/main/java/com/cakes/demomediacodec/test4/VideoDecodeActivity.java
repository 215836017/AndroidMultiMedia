package com.cakes.demomediacodec.test4;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cakes.demomediacodec.R;
import com.cakes.demomediacodec.test3.VideoEncodeResult;
import com.cakes.utils.LogUtil;

public class VideoDecodeActivity extends AppCompatActivity {

    private final String TAG = "VideoDecodeActivity";

    private SurfaceView surfaceView;
    private Button btnDecode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_decode);

        surfaceView = findViewById(R.id.video_decode_surface);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtil.w(TAG, "surfaceCreated() --- 1111111111");

                LogUtil.d(TAG, "解码Surface创建完成");
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
        });

        btnDecode = findViewById(R.id.video_decode_btn_init);
        btnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDecodeVideo();
            }
        });
    }

    //解码器
    private MediaCodecUtil codecUtil;
    //读取文件解码线程
    private VideoDecodeThread thread;
    private static String path = VideoEncodeResult.TEST_DIR_PATH + VideoEncodeResult.FILE_H264;

    private boolean isStartDecode;

    private void testDecodeVideo() {
        if (isStartDecode) {
            isStartDecode = false;
            btnDecode.setText("开始播放");

        } else {
            isStartDecode = true;
            btnDecode.setText("正在播放中");

            if (thread != null) {
                thread.start();
            }
        }
    }
}

