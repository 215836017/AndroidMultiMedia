package com.test.demoaudio.player.audiotrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.test.demoaudio.R;
import com.test.demoaudio.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 1. 学习链接：
 * https://www.cnblogs.com/mfmdaoyou/p/7348969.html
 * <p>
 * 需要关注的用户 1. https://www.jianshu.com/p/1029be871bcf
 * 2. https://blog.csdn.net/itachi85/article/details/54695046/
 * <p>
 * 2. AudioTrack 属于更偏底层的音频播放，MediaPlayerService的内部就是使用了AudioTrack。
 * 3. AudioTrack 直接支持WAV和PCM，其他音频需要解码成PCM格式才能播放。(其他无损格式没有尝试，有兴趣可以使本文提供的例子测试一下)
 */
public class AudioTrackActivity extends AppCompatActivity implements View.OnClickListener {

    private final String tag = "AudioTrackActivity.java";

    private Button btnPCM1, btnPCM2;
    private Button btnWAV1, btnWAV2;
    private Button btnStop;

    /*** 在AudioRecordActivity中录制的 */
    private String myPcmFilePath = "";
    /*** 从网上下载的 */
    private String downloadPcmFilePath = "";
    /*** 在AudioRecordActivity中录制的 */
    private String myWavFilePath = "";
    /*** 从网上下载的 */
    private String downloadWavFilePath = "";

    private AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track);

        init();
        addListeners();
    }

    private void init() {
        btnPCM1 = findViewById(R.id.audioTrackAct_btn_playPCM_1);
        btnPCM2 = findViewById(R.id.audioTrackAct_btn_playPCM_2);
        btnWAV1 = findViewById(R.id.audioTrackAct_btn_playWAV_1);
        btnWAV2 = findViewById(R.id.audioTrackAct_btn_playWAV_2);
        btnStop = findViewById(R.id.audioTrackAct_btn_stop);

        /*
        AudioTrack audio = new AudioTrack(
     AudioRecordManager.STREAM_MUSIC, // 指定流的类型
     32000, // 设置音频数据的採样率 32k，假设是44.1k就是44100
     AudioFormat.CHANNEL_OUT_STEREO, // 设置输出声道为双声道立体声，而CHANNEL_OUT_MONO类型是单声道
     AudioFormat.ENCODING_PCM_16BIT, // 设置音频数据块是8位还是16位。这里设置为16位。
好像如今绝大多数的音频都是16位的了
     AudioTrack.MODE_STREAM // 设置模式类型，在这里设置为流类型，第二种MODE_STATIC貌似没有什么效果
     );

      AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat,
            int bufferSizeInBytes, int mode)
         */
        int minBufferSize = AudioTrack.getMinBufferSize(16 * 1000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16 * 1000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
    }

    private void addListeners() {

        btnPCM1.setOnClickListener(this);
        btnPCM2.setOnClickListener(this);
        btnWAV1.setOnClickListener(this);
        btnWAV2.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audioTrackAct_btn_playPCM_1:
                readPCMFile(myPcmFilePath);
                break;

            case R.id.audioTrackAct_btn_playPCM_2:
                readPCMFile(downloadPcmFilePath);
                break;

            case R.id.audioTrackAct_btn_playWAV_1:
                readWavFile(myWavFilePath);
                break;

            case R.id.audioTrackAct_btn_playWAV_2:
                readWavFile(downloadWavFilePath);
                break;

            case R.id.audioTrackAct_btn_stop:
                audioTrack.stop();
                break;

        }
    }

    /*
       代码参考：
       1. https://www.jianshu.com/p/632dce664c3d
       2. https://www.jianshu.com/p/c67fd0c2b379
     */
    private void readWavFile(String path) {
        File file = new File(path);

        if (!file.exists()) {
            showToast(getString(R.string.at_noFile));
        } else {
            getBytesFromFile(file);
        }
    }

    private void readPCMFile(final String filePath) {

        // final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordAudio/test.raw";
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    LogUtil.i(tag, "readPCMFile() --- 111111111111");
                    byte[] datas = new byte[3200];
                    FileInputStream fileInputStream = new FileInputStream(filePath);
                    int readCount = -1;
                    while (fileInputStream.read(datas) != -1) {
                        LogUtil.i(tag, "readPCMFile() --- 222222222222");
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                            continue;
                        }

                        if (readCount != 0) {
                            LogUtil.i(tag, "readPCMFile() --- 333333333");
                            audioTrack.play();
                            audioTrack.write(datas, 0, datas.length);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getBytesFromFile(File file) {
        byte[] bytes = new byte[12];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            audioTrack.write(bytes, 0, 0);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        LogUtil.i(tag, msg);
    }
}
