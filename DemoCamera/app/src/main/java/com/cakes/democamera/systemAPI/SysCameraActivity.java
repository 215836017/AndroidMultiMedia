package com.cakes.democamera.systemAPI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.cakes.democamera.R;
import com.cakes.democamera.utils.LogUtil;

import java.io.File;
import java.io.IOException;

public class SysCameraActivity extends AppCompatActivity {

    private final String TAG = "SysCameraActivity";

    private TextView textView;
    private ImageView imageView;

    private final int REQUEST_CODE_TAKE_PHOTO = 100;
    private final int REQUEST_CODE_RECORD_VIDEO = 101;

    private final String AUTH_FILE_PROVIDER = "com.cakes.democamera.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_camera);

        textView = findViewById(R.id.sys_camera_activity_text);
        imageView = findViewById(R.id.sys_camera_activity_image);
        findViewById(R.id.sys_camera_activity_btn_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSysCameraAppToTakePhoto();
            }
        });

        findViewById(R.id.sys_camera_activity_btn_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSysCameraAppToRecordVideo();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult() --- 111111111, requestCode = " + requestCode
                + ", resultCode = " + resultCode);

        if (null == data) {
            /* 如果在调用startActivityForResult()之前设置照片的路径，那么这里返回的data为空，
             获取拍照的数据就要设置的文件中读取。*/
            LogUtil.e(TAG, "onActivityResult() --- error: 1111111");
        }
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                LogUtil.d(TAG, "onActivityResult() --- 2222222222");
                Uri uri = data.getData();          // 视频的保存路径
                if (null != uri) {
                    textView.setText(uri.toString());
                } else {
                    LogUtil.e(TAG, "onActivityResult() --- error: 22222");
                }
                /*缩略图信息是储存在返回的intent中的Bundle中的，
                 * 对应Bundle中的键为data，因此从Intent中取出
                 * Bundle再根据data取出来Bitmap即可*/

                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                LogUtil.d(TAG, "onActivityResult() ---  bitmap.getWidth = " + bitmap.getWidth()
                        + " bitmap.getHeight = " + bitmap.getHeight());
                imageView.setImageBitmap(bitmap);

            } else if (requestCode == REQUEST_CODE_RECORD_VIDEO) {
                Uri uri = data.getData();          // 视频的保存路径
                if (null != uri) {
                    textView.setText(uri.toString());
                    LogUtil.i(TAG, "videoPath = " + uri.toString());
                } else {
                    LogUtil.e(TAG, "onActivityResult() --- error: 444444444");
                }

                // 不设置保存路径的情况下，data.getExtras()在有些机型中返回的是null；而在有些机型中extras.get("data")返回的是null
//                Bundle extras = data.getExtras();
//                Object oc = extras.get("data");
//                if (null == oc) {
//                    LogUtil.e(TAG, "onActivityResult() --- error: 555555");
//                } else {
//                    LogUtil.i(TAG, "onActivityResult() --- 44444444444");
//                }
            }
        } else {
            LogUtil.e(TAG, "onActivityResult() --- error: 333333333333");
        }
    }

    private void callSysCameraAppToTakePhoto() {
        // 拍照
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aDemoCamera/testSys.jpg";
        File imageFile = createImageFile(filePath);
        Uri imageUri;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  // 表示跳转至相机的拍照界面
        if (intent.resolveActivity(getPackageManager()) != null) {//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退

            if (null != imageFile) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /* Android7及以上需要使用FileProvider */
                    imageUri = FileProvider.getUriForFile(this, AUTH_FILE_PROVIDER, imageFile);

                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    imageUri = Uri.fromFile(imageFile);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径

                startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);//启动相机
            } else {
                LogUtil.e(TAG, "callSysCameraAppToTakePhoto() -- error: imageFile == null");
            }
        } else {
            LogUtil.e(TAG, "callSysCameraAppToTakePhoto() -- error: open camera App failed");
        }
    }

    private void callSysCameraAppToRecordVideo() {
        // 录像
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aDemoCamera/testSys.mp4";
        File videoFile = createImageFile(filePath);
        Uri videoUri;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);  // 表示跳转至相机的录视频界面
        if (intent.resolveActivity(getPackageManager()) != null) {//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
            if (null != videoFile) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /* Android7及以上需要使用FileProvider */
                    videoUri = FileProvider.getUriForFile(this, AUTH_FILE_PROVIDER, videoFile);
                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    videoUri = Uri.fromFile(videoFile);
                }

                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);   // 设置视频录制的最长时间
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大

                startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);//启动相机
            } else {
                LogUtil.e(TAG, "callSysCameraAppToRecordVideo() -- error: videoFile == null");
            }
        }
    }

    /**
     * 创建指定的文件
     */
    private File createImageFile(String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        File dirFile = file.getParentFile();
        if (null != dirFile) {
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        }

        return file;
    }

}
