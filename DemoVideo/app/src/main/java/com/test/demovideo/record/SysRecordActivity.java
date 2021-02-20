package com.test.demovideo.record;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.test.demovideo.R;
import com.test.demovideo.utils.LogUtil;

import java.io.File;

public class SysRecordActivity extends AppCompatActivity {

    private final String TAG = "SysRecordActivity";
    private Uri uri;
    private final int REQUEST_CODE_RECORD_VIDEO = 1;

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
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE); // ��ʾ��ת�������¼��Ƶ����

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordVideo/testSys.mp4";
        Uri uri = Uri.fromFile(new File(filePath));   // ��·��ת��ΪUri����
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    // MediaStore.EXTRA_VIDEO_QUALITY ��ʾ¼����Ƶ���������� 0-1��Խ���ʾ����Խ�ã�ͬʱ��ƵҲԽ��
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // ��ʾ¼����󱣴��¼�ƣ������д����ᱣ�浽Ĭ�ϵ�·������onActivityResult()�Ļص���ͨ��intent.getData�з��ر����·��
//        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // ������Ƶ¼�Ƶ��ʱ��
        startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);  // ��ת
    }

}
