package com.cakes.demomediacodec.test2;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;

import com.cakes.demomediacodec.test1.AudioEncodeResult;
import com.cakes.utils.LogUtil;

import java.io.File;

public class AudioDecodeProcessor2 extends Thread {

    private final String TAG = "AudioDecodeProcessor2";

    public static final int DEFAULT_FREQUENCY = 16000;
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_CHANNEL_COUNT = 1;
    public static final boolean DEFAULT_AEC = true;

    public static final int DEFAULT_BPS = 32000;
    public static final String DEFAULT_MIME = "audio/mp4a-latm";
    public static final int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    //文件路径
    private String filePath;

    private AudioHardDecoder2 audioHardDecoder2;
    private AudioDecodeResult audioDecodeResult;

    public AudioDecodeProcessor2() {

        filePath = AudioEncodeResult.TEST_DIR_PATH + AudioEncodeResult.FILE_AAC;
        LogUtil.i(TAG, "AudioDecodeProcessor() -- filePath = " + filePath);

    }

    private void initAudioDecoder() {
        if (null == audioDecodeResult) {
            audioDecodeResult = new AudioDecodeResult(DEFAULT_FREQUENCY, DEFAULT_CHANNEL_COUNT,
                    DEFAULT_AUDIO_ENCODING);
            LogUtil.i(TAG, "initAudioDecoder() -- 11111, init AudioDecodeResult end");
        }
        if (null == audioHardDecoder2) {
            audioHardDecoder2 = new AudioHardDecoder2(audioDecodeResult);
            audioHardDecoder2.prepareDecoder(filePath);
            LogUtil.i(TAG, "initAudioDecoder() -- 2222 audioHardDecoder.prepareDecoder() end");
        }
    }

    private void release() {
        LogUtil.d(TAG, "release() -- 11111111");
        if (null != audioHardDecoder2) {
            audioHardDecoder2.release();
        }
    }

    @Override
    public void run() {
        super.run();

        File file = new File(filePath);
        if (null == file || !file.exists()) {
            LogUtil.e(TAG, "aac file is no exist!!!");
            return;
        }

        LogUtil.d(TAG, "start to decode aac file..");
        initAudioDecoder();
        audioHardDecoder2.startDecoderAacFile();
    }

}
