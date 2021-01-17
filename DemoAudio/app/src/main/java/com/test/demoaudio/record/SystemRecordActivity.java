package com.test.demoaudio.record;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.test.demoaudio.BaseActivity;
import com.test.demoaudio.R;
import com.test.demoaudio.utils.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * 调用系统录音机
 */
public class SystemRecordActivity extends BaseActivity {

    private final String TAG = "SystemRecordActivity";
    private final int REQUEST_CODE = 0x11;

    private TextView textPath;
    private Button btnCallRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_record);

        textPath = findViewById(R.id.sys_record_act_text_path);
        btnCallRecord = findViewById(R.id.sys_record_act_btn_call_record);
        btnCallRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSysRecorder();
            }
        });
    }

    /**
     * 启动录音机，创建文件
     */
    private void callSysRecorder() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
//        File voiceFile = createVoiceFile();
//        if (null != voiceFile) {
//            Uri uri = Uri.fromFile(voiceFile);
//            intent.setData(uri);
//            LogUtil.d(TAG, "set file is ok");
//        }
        LogUtil.d(TAG, "启动系统录音机，开始录音...");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "调用系统录音机-- onActivityResult() -- requestCode = " +
                requestCode + ", resultCode = " + resultCode);
        // 在vivo x7Plus中测试，resultCode = 0；
        // 在华为手机上测试，录音完成后会提示是否使用此录音，选择使用则会在data中返回路径。
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            try {
                Uri uri = data.getData();
                String filePath = getAudioFilePathFromUri(uri);
                if (null != uri) {
                    LogUtil.d(TAG, "根据uri获取文件路径：" + uri.toString());
                    if (!TextUtils.isEmpty(filePath)) {
                        textPath.setText(filePath);
                    }
                } else {
                    LogUtil.d(TAG, "根据uri获取文件路径：uri = null");
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "调用系统录音机失败了： " + e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            LogUtil.e(TAG, "调用系统录音机失败了");
        }
    }

    /**
     * 创建音频目录
     */
    private File createVoiceFile() {
        String mVoiceName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordAudio/testSys.amr";
        LogUtil.d(TAG, "录音文件名称：" + mVoiceName);
        File mVoiceFile = new File(mVoiceName);
        File dir = mVoiceFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!mVoiceFile.exists()) {
            try {
                mVoiceFile.createNewFile();

                return mVoiceFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 通过Uri，获取录音文件的路径（绝对路径）
     *
     * @param uri 录音文件的uri
     * @return 录音文件的路径（String）
     */
    private String getAudioFilePathFromUri(Uri uri) {
        if (null == uri) {
            return null;
        }
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
        String temp = cursor.getString(index);
        cursor.close();
        return temp;
    }

}
