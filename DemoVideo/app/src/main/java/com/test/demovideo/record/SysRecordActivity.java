package com.test.demovideo.record;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.test.demovideo.R;
import com.test.demovideo.utils.LogUtil;

public class SysRecordActivity extends AppCompatActivity {

    private final String TAG = "SysRecordActivity";
    private Uri uri;

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
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, 300);
    }

}
