package com.cakes.democamera.systemAPI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cakes.democamera.R;
import com.cakes.democamera.utils.LogUtil;

import java.io.File;

public class SysCameraActivity extends AppCompatActivity {

    private final String TAG = "SysCameraActivity";

    private TextView textView;
    private ImageView imageView;

    private final int REQUEST_CODE_RECORD_VIDEO = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_camera);

        textView = findViewById(R.id.sys_camera_activity_text);
        imageView = findViewById(R.id.sys_camera_activity_image);
        findViewById(R.id.sys_camera_activity_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSysCameraAPP();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult() --- 111111111");
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == REQUEST_CODE_RECORD_VIDEO) {
                LogUtil.d(TAG, "onActivityResult() --- 2222222222");
//                Uri uri = data.getData();          // 视频的保存路径
//                textView.setText(uri.toString());

                /*缩略图信息是储存在返回的intent中的Bundle中的，
                 * 对应Bundle中的键为data，因此从Intent中取出
                 * Bundle再根据data取出来Bitmap即可*/

                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                LogUtil.d(TAG, "onActivityResult() ---  bitmap.getWidth = " + bitmap.getWidth()
                        + " bitmap.getHeight = " + bitmap.getHeight());
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void callSysCameraAPP() {
        // 拍照
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aDemoCamera/testSys.png";

        Uri uri = Uri.fromFile(new File(filePath));   // 将路径转换为Uri对象  android 5以上的系统不兼容
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  // 表示跳转至相机的录视频界面

        // 录像
//        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DemoCamera/testSys.mp4";
//        Uri uri = Uri.fromFile(new File(filePath));   // 将路径转换为Uri对象
//        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);  // 表示跳转至相机的录视频界面
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 设置视频录制的最长时间
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大

//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径

        if (intent.resolveActivity(getPackageManager()) != null) {//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
            startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);//启动相机
        }
    }

}
