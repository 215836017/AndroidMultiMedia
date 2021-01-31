package com.test.demovideo;

import android.os.Build;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class BaseActivity extends AppCompatActivity {

    public final String TOAST_PERMISSION_AUDIO = "需要在系统设置中同意该APP的录音权限！";
    public final String TOAST_PERMISSION_FILE = "需要在系统设置中同意该APP的文件权限！";

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean hasPermission(String permission) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
