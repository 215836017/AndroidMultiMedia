package com.test.demoaudio;

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

import com.test.demoaudio.player.audiotrack.AudioTrackActivity;
import com.test.demoaudio.player.ringtone.RingtoneActivity;
import com.test.demoaudio.player.mediaplayer.MediaPlayerActivity;
import com.test.demoaudio.player.soundpool.SoundPoolActivity;
import com.test.demoaudio.record.AudioRecordActivity;
import com.test.demoaudio.record.MediaRecorderActivity;
import com.test.demoaudio.record.SystemRecordActivity;
import com.test.demoaudio.utils.LogUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final int CODE_REQUEST_ALL_PERMISSIONS = 0x10;
    private AlertDialog alertDialog;
    private final String[] requestPermissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Intent intent = new Intent();

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
            case R.id.main_act_btn_sys_record:
                intent.setClass(this, SystemRecordActivity.class);
                break;

            case R.id.main_act_btn_audio_record:
                intent.setClass(this, AudioRecordActivity.class);
                break;

            case R.id.main_act_btn_media_recorder:
                intent.setClass(this, MediaRecorderActivity.class);
                break;

            case R.id.main_act_btn_sound_pool:
                intent.setClass(this, SoundPoolActivity.class);
                break;

            case R.id.main_act_btn_media_player:
                intent.setClass(this, MediaPlayerActivity.class);
                break;

            case R.id.main_act_btn_audio_track:
//                intent.setClass(this, AudioTrackActivity.class);
                intent.setClass(this, AudioTrackActivity.class);
                break;

            case R.id.main_act_btn_ringtone:
                intent.setClass(this, RingtoneActivity.class);
                break;
        }
        startActivity(intent);
    }

}
