package com.cakes.demomediacodec.test2;

import android.media.AudioManager;
import android.media.AudioTrack;

import com.cakes.utils.LogUtil;

public class MyAudioTrack {

    private final String TAG = "MyAudioTrack";

    private int mFrequency;// 采样率
    private int mChannel;// 声道
    private int audioFormat;// 采样精度
    private AudioTrack mAudioTrack;

    public MyAudioTrack(int frequency, int channel, int audioFormat) {
        this.mFrequency = frequency;
        this.mChannel = channel;
        this.audioFormat = audioFormat;
    }

    /**
     * 初始化
     */
    public void init() {
        if (mAudioTrack != null) {
            release();
        }
        // 获得构建对象的最小缓冲区大小
        int minBufSize = getMinBufferSize();
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mFrequency, mChannel, audioFormat, minBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    private int getMinBufferSize() {
        return AudioTrack.getMinBufferSize(mFrequency,
                mChannel, audioFormat);
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