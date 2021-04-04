package com.cakes.demolame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cakes.demolame.jni.JniLameUtil;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.sample_text);
        tv.setText("Lame version: " + JniLameUtil.getLameVersion());
    }

    public void btnClick(View view) {
        if (view.getId() == R.id.btn_start_encode) {
            startEncode();
        }
    }

    private void startEncode() {
        

    }

}