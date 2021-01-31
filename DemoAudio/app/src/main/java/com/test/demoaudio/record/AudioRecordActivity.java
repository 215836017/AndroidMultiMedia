package com.test.demoaudio.record;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.test.demoaudio.BaseActivity;
import com.test.demoaudio.R;
import com.test.demoaudio.utils.LogUtil;
import com.test.demoaudio.utils.PcmToWavUtil;
import com.test.demoaudio.utils.TimeUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * 使用AudioRecord进行录音，并保存在本地
 */
public class AudioRecordActivity extends BaseActivity {

    private final String TAG = "AudioRecordActivity";
    private TextView textView;
    private Button btnPcm;
    private Button btnWav;

    private int timeCount = 0;

    private final int SAMPLERATEINHZ = 16 * 1000; // 16000, 44100...
    private final int AUDIOFORMAT = AudioFormat.ENCODING_PCM_16BIT;  // 16bits, 32bits
    private final int CHANNELCONFIG = AudioFormat.CHANNEL_IN_MONO;
    private boolean isRecording = false;

    private AudioRecord audioRecord;
    private int recordBuffSize = 0;
    private byte[] recordDatas;

    private final int MSG_UPDATE_TIME = 0x11;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_TIME) {
                textView.setText(TimeUtil.getTime(timeCount));

                if (isRecording) {
                    timeCount++;
                    handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != audioRecord) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void initViews() {
        textView = findViewById(R.id.audioRecord_text);
        btnPcm = findViewById(R.id.audioRecord_btn_record_pcm);
        btnWav = findViewById(R.id.audioRecord_btn_record_wav);
        btnPcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
                    showToast(TOAST_PERMISSION_AUDIO);
                    return;
                }

                if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showToast(TOAST_PERMISSION_FILE);
                    return;
                }

                initAudio();
                handlePcmEvent();
            }
        });

        btnWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
                    showToast(TOAST_PERMISSION_AUDIO);
                    return;
                }

                if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showToast(TOAST_PERMISSION_FILE);
                    return;
                }
                initAudio();
                handleWavEvent();
            }
        });
    }

    private void initAudio() {
        recordBuffSize = AudioRecord.getMinBufferSize(SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT);
        recordDatas = new byte[recordBuffSize];
        LogUtil.i(TAG, "initAudio() -- recordBuffSize = " + recordBuffSize);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATEINHZ,
                CHANNELCONFIG, AUDIOFORMAT, recordBuffSize);
    }

    private File createFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (Exception e) {
                return null;
            }
        }

        return file;
    }

    private void handlePcmEvent() {
        if (isRecording) {
            isRecording = false;
            btnPcm.setText("开始录制PCM");
            btnWav.setClickable(true);

        } else {

            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordAudio/test.pcm";
            File file = createFile(filePath);
            if (null == file) {
                showToast("PCM文件创建失败");
                return;
            }
            isRecording = true;
            timeCount = 0;
            btnPcm.setText("停止录制PCM");
            btnWav.setClickable(false);

            handler.sendEmptyMessage(MSG_UPDATE_TIME);
            recordPcm(file);
        }
    }

    private void handleWavEvent() {

        if (isRecording) {
            isRecording = false;
            btnWav.setText("开始录制WAV");
            btnPcm.setClickable(true);

        } else {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordAudio/test.wav";
            File file = createFile(filePath);
            if (null == file) {
                showToast("WAV文件创建失败");
                return;
            }
            isRecording = true;
            timeCount = 0;
            btnWav.setText("停止录制WAV");
            btnPcm.setClickable(false);
            handler.sendEmptyMessage(MSG_UPDATE_TIME);
            recordWav(file);
        }
    }

    private void recordPcm(final File file) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                FileOutputStream fo = null;
                try {
                    audioRecord.startRecording();
                    int audioRecordState = audioRecord.getState();
                    LogUtil.i(TAG, "audioRecordState = " + audioRecordState);
                    int len;
                    fo = new FileOutputStream(file);
                    while (isRecording) {
                        len = audioRecord.read(recordDatas, 0, recordBuffSize);
                        LogUtil.i(TAG, "len = " + len);
                        if (AudioRecord.ERROR_INVALID_OPERATION != len) {
                            LogUtil.i(TAG, "recordDatas.len = " + recordDatas.length);
                            fo.write(recordDatas);
                        }
                    }

                    audioRecord.stop();
                } catch (Exception e) {

                }
            }
        }.start();
    }

    /**
     * 唯一需要注意的一点是在保存wav音频文件的时候，在pcm的基础上需要给它增加wav header信息，否则文件无法打开。
     */
    private void recordWav(final File file) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                FileOutputStream fo = null;
                ByteArrayOutputStream baos = null;
                int len = 0;
                try {
                    fo = new FileOutputStream(file);
                    baos = new ByteArrayOutputStream();

                    audioRecord.startRecording();
                    while (isRecording) {
                        len = audioRecord.read(recordDatas, 0, recordBuffSize);
//                        byte clearM = 0;
//                        Arrays.fill(recordDatas, clearM); // 替换为静音
                        LogUtil.i(TAG, "recordWav() --- len = " + len + ", recordDatas.len = " + recordDatas.length);
                        if (AudioRecord.ERROR_INVALID_OPERATION != len) {
                            baos.write(recordDatas, 0, len);
                        }
                    }
                    audioRecord.stop();

                    byte[] allAuidoBytes = baos.toByteArray();
                    fo.write(PcmToWavUtil.getWavHeader(CHANNELCONFIG, allAuidoBytes.length, SAMPLERATEINHZ));
                    fo.write(allAuidoBytes);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
