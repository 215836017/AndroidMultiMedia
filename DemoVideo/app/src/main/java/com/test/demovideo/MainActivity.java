package com.test.demovideo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.test.demovideo.player.MSActivity;
import com.test.demovideo.player.MTActivity;
import com.test.demovideo.player.VideoViewActivity;
import com.test.demovideo.record.MediaRecordActivity;
import com.test.demovideo.record.SysRecordActivity;
import com.test.demovideo.utils.LogUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 相关SDK：
 * 1. https://www.meishesdk.com/
 * 2. VCamera
 * 相关连接：
 * https://blog.csdn.net/liuzhi0724/article/details/81318816
 * https://www.jianshu.com/p/f057a03ded0b
 * https://www.jianshu.com/p/d3c0b2a2188a
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private final int CODE_REQUEST_ALL_PERMISSIONS = 0x10;
    private AlertDialog alertDialog;
    private final String[] requestPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyAllNeedPermissions();
    }

    private void applyAllNeedPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 一次性申请需要的所有权限
            ActivityCompat.requestPermissions(this, requestPermissions, CODE_REQUEST_ALL_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtil.d(TAG, "onRequestPermissionsResult() -- permissions.len = " + permissions.length
                + ", grantResults.len = " + grantResults.length);

        switch (requestCode) {
            case CODE_REQUEST_ALL_PERMISSIONS:
                handleAllPermissions(permissions, grantResults);
                break;
        }
    }

    private void handleAllPermissions(String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PERMISSION_GRANTED) {
                // 选择了“允许”
                // Toast.makeText(this, "权限：" + permissions[i] + "申请成功", Toast.LENGTH_LONG).show();

            } else {   //选择禁止,
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {  // 选择禁止, 但没有勾选不再询问

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("permission")
                            .setMessage("点击允许才可以继续使用！")
                            .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            CODE_REQUEST_ALL_PERMISSIONS);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (null != alertDialog && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                }
                            });
                    alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        }
    }

    public void btnClick(View view) {
        switch (view.getId()) {
            case R.id.main_act_btn_sys_app:
                startActivity(new Intent(this, SysRecordActivity.class));
                break;

            case R.id.main_act_btn_media_record:
                startActivity(new Intent(this, MediaRecordActivity.class));
                break;

            case R.id.main_act_btn_mv:
                startActivity(new Intent(this, VideoViewActivity.class));
                break;

            case R.id.main_act_btn_ms:
                startActivity(new Intent(this, MSActivity.class));
                break;

            case R.id.main_act_btn_mt:
                startActivity(new Intent(this, MTActivity.class));
                break;
        }
    }

}
