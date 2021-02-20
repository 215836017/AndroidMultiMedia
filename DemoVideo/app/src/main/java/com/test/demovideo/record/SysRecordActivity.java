package com.test.demovideo.record;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.test.demovideo.R;
import com.test.demovideo.utils.LogUtil;

import java.io.File;

public class SysRecordActivity extends AppCompatActivity {

    private final String TAG = "SysRecordActivity";
    private Uri uri;
    private final int REQUEST_CODE_RECORD_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_record);

        findViewById(R.id.sys_record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            LogUtil.d(TAG, "onActivityResult() -- path = " + uri.toString());
        }
    }

    private void btnClick() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE); // 表示跳转至相机的录视频界面

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordVideo/testSys.mp4";
        Uri uri = Uri.fromFile(new File(filePath));   // 将路径转换为Uri对象
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 设置视频录制的最长时间
        startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);  // 跳转
    }

}
