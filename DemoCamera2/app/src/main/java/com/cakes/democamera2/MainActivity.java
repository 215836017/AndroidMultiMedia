package com.cakes.democamera2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    需手动在手机设置里面开启权限。
     */
    public void btnClick(View view) {
        if (view.getId() == R.id.main_test_01) {
            startActivity(new Intent(this, Test1Activity.class));
        }
    }
}