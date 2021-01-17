package com.cakes.democamera2;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public void showToast(String TAG, String msg) {
        LogUtil.i(TAG, "showToast() -- msg = " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
