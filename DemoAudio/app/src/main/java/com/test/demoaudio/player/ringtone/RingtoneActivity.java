package com.test.demoaudio.player.ringtone;

import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.test.demoaudio.R;

import java.io.File;

public class RingtoneActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnPlayPhoneComing, btnPlayRandom;
    private Button btnPlayRington, btnPlayAlarm, btnPlayNotification;

    /* 自定义的类型 */
    public static final int ButtonRingtone = 0;
    public static final int ButtonAlarm = 1;
    public static final int ButtonNotification = 2;
    /* 铃声文件夹 */
    private String strRingtoneFolder = "/sdcard/music/ringtones";
    private String strAlarmFolder = "/sdcard/music/alarms";
    private String strNotificationFolder = "/sdcard/music/notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);

        init();
        addListeners();
    }

    private void init() {
        btnPlayPhoneComing = findViewById(R.id.ringtoneAct_btn_defaultPhoneComing);
        btnPlayRandom = findViewById(R.id.ringtoneAct_btn_randomRingtone);
        btnPlayRington = findViewById(R.id.ringtoneAct_btn_ringtones);
        btnPlayAlarm = findViewById(R.id.ringtoneAct_btn_alarms);
        btnPlayNotification = findViewById(R.id.ringtoneAct_btn_notifications);
    }

    private void addListeners() {
        btnPlayPhoneComing.setOnClickListener(this);
        btnPlayRandom.setOnClickListener(this);
        btnPlayRington.setOnClickListener(this);
        btnPlayAlarm.setOnClickListener(this);
        btnPlayNotification.setOnClickListener(this);
    }

    /**
     * 记得添加权限：
     * <uses-permission android:name="android.permission.MEDIA_CONTENT_CONTROL"/>
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ringtoneAct_btn_defaultPhoneComing:
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Ringtone mRingtone = RingtoneManager.getRingtone(this, uri);
                mRingtone.play();
                break;

            case R.id.ringtoneAct_btn_randomRingtone:
                RingtoneManager manager = new RingtoneManager(this);
                Cursor cursor = manager.getCursor();
                int count = cursor.getCount();
                int position = (int) (Math.random() * count);
                Ringtone ringtone = manager.getRingtone(position);
                ringtone.play();
                break;

            case R.id.ringtoneAct_btn_ringtones:

                /*
                在android系统中，不同铃声存放的铃声路径：
                /system/media/audio/ringtones        来电铃声
                /system/media/audio/notifications        短信通知铃声
                /system/media/audio/alarms        闹钟铃声
                */
                if (bFolder(strRingtoneFolder)) {
                    //打开系统铃声设置
                    Intent intent01 = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    //类型为来电RINGTONE
                    intent01.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                    //设置显示的title
                    intent01.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置来电铃声");
                    //当设置完成之后返回到当前的Activity
                    startActivityForResult(intent01, ButtonRingtone);
                }
                break;

            case R.id.ringtoneAct_btn_alarms:
                if (bFolder(strAlarmFolder)) {
                    //打开系统铃声设置
                    Intent intent02 = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    //设置铃声类型和title
                    intent02.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                    intent02.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置闹铃铃声");
                    //当设置完成之后返回到当前的Activity
                    startActivityForResult(intent02, ButtonAlarm);
                }
                break;

            case R.id.ringtoneAct_btn_notifications:
                if (bFolder(strNotificationFolder)) {
                    //打开系统铃声设置
                    Intent intent03 = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    //设置铃声类型和title
                    intent03.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent03.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置通知铃声");
                    //当设置完成之后返回到当前的Activity
                    startActivityForResult(intent03, ButtonNotification);
                }
                break;
        }
    }

    /* 当设置铃声之后的回调函数 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ButtonRingtone:
                try {
                    //得到我们选择的铃声
                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    //将我们选择的铃声设置成为默认
                    if (pickedUri != null) {
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, pickedUri);
                    }
                } catch (Exception e) {
                }
                break;
            case ButtonAlarm:
                try {
                    //得到我们选择的铃声
                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    //将我们选择的铃声设置成为默认
                    if (pickedUri != null) {
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, pickedUri);
                    }
                } catch (Exception e) {
                }
                break;
            case ButtonNotification:
                try {
                    //得到我们选择的铃声
                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    //将我们选择的铃声设置成为默认
                    if (pickedUri != null) {
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, pickedUri);
                    }
                } catch (Exception e) {
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //检测是否存在指定的文件夹
    //如果不存在则创建
    private boolean bFolder(String strFolder) {
        boolean btmp = false;
        File f = new File(strFolder);
        if (!f.exists()) {
            if (f.mkdirs()) {
                btmp = true;
            } else {
                btmp = false;
            }
        } else {
            btmp = true;
        }
        return btmp;
    }
}
