package com.cakes.demomediacodec.test;

import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cakes.demomediacodec.R;

public class H264FileDecodeActivity extends AppCompatActivity {

    SurfaceView testSurfaceView;

    private SurfaceHolder holder;
    //解码器
    private MediaCodecUtil codecUtil;
    //读取文件解码线程
    private MediaCodecThread thread;
    //文件路径
    private String path = Environment.getExternalStorageDirectory().toString() + "/SONA/test.h264";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h264_file_decodec);
        initSurface();
    }

    //初始化播放相关
    private void initSurface() {
        holder = testSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
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
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (codecUtil != null) {
                    codecUtil.stopCodec();
                    codecUtil = null;
                }
                if (thread != null && thread.isAlive()) {
                    thread.stopThread();
                    thread = null;
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_h264_file:
                if (thread != null) {
                    thread.start();
                }
                break;
        }
    }
}