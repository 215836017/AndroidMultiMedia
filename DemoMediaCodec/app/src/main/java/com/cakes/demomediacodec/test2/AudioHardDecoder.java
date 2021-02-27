package com.cakes.demomediacodec.test2;

import android.media.MediaCodec;
import android.os.Build;

import com.cakes.demomediacodec.mediaCodec.MediaCodecHelper;
import com.cakes.utils.LogUtil;

import java.nio.ByteBuffer;

public class AudioHardDecoder {

    private final String TAG = "AudioHardDecoder";

    private MediaCodec mediaCodec;
    private OnAudioDecodeListener onAudioDecodeListener;

    private ByteBuffer inputBuffer = null;
    private ByteBuffer outputBuffer = null;
    private int inputBufferIndex;
    private int outputBufferIndex;
    private MediaCodec.BufferInfo bufferInfo;

    public AudioHardDecoder() {
    }

    public AudioHardDecoder(OnAudioDecodeListener onAudioDecodeListener) {
        this.onAudioDecodeListener = onAudioDecodeListener;
    }

    public void prepareDecoder(String mine, int channelCount, int sampleRate) {
        mediaCodec = MediaCodecHelper.getAudioDecoder(mine, channelCount, sampleRate);
        if (null == mediaCodec) {
            LogUtil.e(TAG, "prepareDecoder() -- error: null == mediaCodec");
            return;
        }
        mediaCodec.start();
        LogUtil.i(TAG, "prepareDecoder() -- call mediaCodec.start() success");
    }

    synchronized public void stop() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
    }

    synchronized void offerEncoder(byte[] input, int offset, int length) {
        if (null == mediaCodec) {
            return;
        }

        inputBufferIndex = mediaCodec.dequeueInputBuffer(12000);
        if (inputBufferIndex >= 0) {
            if (Build.VERSION.SDK_INT >= 21) {
                inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
            }

            inputBuffer.clear();
            inputBuffer.put(input, offset, length);
            long pts = System.nanoTime() / 1000;
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
        }

        outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1200);
        LogUtil.w(TAG, "onOutputBufferAvailable() -- 1111111, index = " + outputBufferIndex
                + ", info.size = " + bufferInfo.size + ", info.flags = " + bufferInfo.flags
                + ", info.presentationTimeUs = " + bufferInfo.presentationTimeUs
                + ", info.offset = " + bufferInfo.offset);
        while (outputBufferIndex >= 0) {
            if (Build.VERSION.SDK_INT >= 21) {
                inputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            } else {
                inputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];
            }
            LogUtil.i(TAG, "音频解码完成...");
            if (onAudioDecodeListener != null) {
                onAudioDecodeListener.onAudioDecode(outputBuffer, bufferInfo);
            }

            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

}
