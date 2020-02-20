package com.cakes.democamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.cakes.democamera.systemAPI.SysCameraActivity;
import com.cakes.democamera.utils.LogUtil;

import java.io.File;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Base knowledge of Android Camera
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private final int CODE_REQUEST_ALL_PERMISSIONS = 0x10;
    private final String[] REQUEST_PERMISSIONS = {
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

    /**
     * 一次性申请程序需要的所有权限
     */
    private void applyAllNeedPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 一次性申请需要的所有权限
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, CODE_REQUEST_ALL_PERMISSIONS);
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

                }
            }
        }
    }

    public void btnClick(View view) {
        if (view.getId() == R.id.main_activity_btn_sys_camera) {
            startActivity(new Intent(this, SysCameraActivity.class));

        } else if (view.getId() == R.id.main_activity_btn_old_cmaera) {

        } else if (view.getId() == R.id.main_activity_btn_new_camera) {

        }
    }


}
