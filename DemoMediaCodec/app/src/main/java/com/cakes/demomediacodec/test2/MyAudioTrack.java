package com.cakes.demomediacodec.test2;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.cakes.utils.LogUtil;

public class MyAudioTrack {

    private final String TAG = "MyAudioTrack";

    private static final int mSampleRateInHz = 44100;
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack mAudioTrack;

    public MyAudioTrack(int frequency, int channel, int audioFormat) {

        init();

    }

    /**
     * 初始化
     */
    public void init() {
        if (mAudioTrack != null) {
            release();
        }
        // 获得构建对象的最小缓冲区大小
        int mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
        LogUtil.d(TAG, "initData() -- mMinBufferSize = " + mMinBufferSize);
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRateInHz, mChannelConfig,
                mAudioFormat, mMinBufferSize, AudioTrack.MODE_STREAM);

        mAudioTrack.play();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

    public void playAudioTrack(byte[] data) {
        playAudioTrack(data, 0, data.length);
    }

    /**
     * 将解码后的pcm数据写入audioTrack播放
     *
     * @param data   数据
     * @param offset 偏移
     * @param length 需要播放的长度
     */
    public void playAudioTrack(byte[] data, int offset, int length) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            mAudioTrack.write(data, offset, length);
        } catch (Exception e) {
            LogUtil.e(TAG, "AudioTrack Exception : " + e.toString());
        }
    }

}