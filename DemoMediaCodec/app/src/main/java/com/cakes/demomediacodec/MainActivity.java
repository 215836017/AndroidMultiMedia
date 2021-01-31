package com.cakes.demomediacodec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cakes.demomediacodec.test.VideoChatActivity;

/**
 * https://www.jianshu.com/p/7cdf5b495ada
 * https://www.jianshu.com/p/7cdf5b495ada
 * https://blog.csdn.net/qq_17441227/article/details/82893445
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private Intent intent = new Intent();

    public void mainBtnClick(View view) {

        switch (view.getId()) {
            case R.id.btn_decode_audio:
                intent.setClass(this, AudioDecodeActivity.class);
                break;

            case R.id.btn_decode_video:
                intent.setClass(this, VideoDecodeActivity.class);
                break;

            case R.id.btn_encode_audio:
                intent.setClass(this, AudioEncodeActivity.class);
                break;

            case R.id.btn_encode_video:
                intent.setClass(this, VideoEncodeActivity.class);
                break;

            case R.id.btn_video_chat:
                intent.setClass(this, VideoChatActivity.class);
                break;
        }

        startActivity(intent);
    }
}