package com.cakes.demomediacodec;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cakes.utils.LogUtil;

public class BaseActivity extends AppCompatActivity {

    public void showToast(String TAG, String msg) {
        LogUtil.d(TAG, "showToast() -- msg: " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
