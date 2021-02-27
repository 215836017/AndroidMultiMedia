package com.cakes.demomediacodec;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cakes.demomediacodec.test1.AudioEncodeActivity;
import com.cakes.demomediacodec.test2.AudioDecodeActivity;
import com.cakes.demomediacodec.test3.VideoEncodeActivity;
import com.cakes.demomediacodec.test4.VideoDecodeActivity;
import com.cakes.demomediacodec.test5.VideoChatActivity;
import com.cakes.demomediacodec.test6.VideoChatActivity2;
import com.cakes.demomediacodec.test7.VideoChatActivityX;
import com.cakes.demomediacodec.test8.VideoChatOpenGLActivity;

/**
 * https://www.jianshu.com/p/7cdf5b495ada
 * https://blog.csdn.net/qq_17441227/article/details/82893445
 * https://zhuanlan.zhihu.com/p/268441151
 */
public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";
    private Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showToast(TAG, "需要手动在系统设置里面开启权限");
    }

    public void mainBtnClick(View view) {

        switch (view.getId()) {
            case R.id.btn_encode_audio:
                intent.setClass(this, AudioEncodeActivity.class);
                break;

            case R.id.btn_decode_audio:
                intent.setClass(this, AudioDecodeActivity.class);
                break;

            case R.id.btn_encode_video:
                intent.setClass(this, VideoEncodeActivity.class);
                break;

            case R.id.btn_decode_video:
                intent.setClass(this, VideoDecodeActivity.class);
                break;

            case R.id.btn_video_chat:
                intent.setClass(this, VideoChatActivity.class);
                break;

            case R.id.btn_video_chat2:
                intent.setClass(this, VideoChatActivity2.class);
                break;

            case R.id.btn_video_chatX:
                intent.setClass(this, VideoChatActivityX.class);
                break;

            case R.id.btn_video_chat_opengl:
                intent.setClass(this, VideoChatOpenGLActivity.class);
                break;
        }

        startActivity(intent);
    }
}