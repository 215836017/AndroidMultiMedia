package com.cakes.demomediacodec.test1;

import android.media.MediaCodec;
import android.os.Build;

import com.cakes.demomediacodec.mediaCodec.MediaCodecHelper;
import com.cakes.utils.LogUtil;

import java.nio.ByteBuffer;

public class AudioHardEncoder {

    private static final int sProfile = 2;
    private static final int sFreqIdx = 8;
    private static final int sChanCfg = 1;

    private final String TAG = "AudioHardEncoder";

    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private ByteBuffer outputBuffer;

    public AudioHardEncoder() {

    }

    public void prepareEncoder(String mime, int frequency, int channelCount,
                               int aacProfile, int bps, int audioEncoding) {
        mMediaCodec = MediaCodecHelper.getAudioEncoder(mime, frequency, channelCount,
                aacProfile, bps, audioEncoding);
        mMediaCodec.start();
    }

    synchronized public void stop() {

        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    synchronized void offerEncoder(byte[] input) {
        if (mMediaCodec == null) {
            LogUtil.w(TAG, "offerEncoder() -- mMediaCodec = null");
            return;
        }
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(12000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = null;
            if (Build.VERSION.SDK_INT >= 21) {
                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = mMediaCodec.getInputBuffers()[inputBufferIndex];
            }

            inputBuffer.clear();
            inputBuffer.put(input);
            long pts = System.nanoTime() / 1000;
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
        }

        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
//        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED && mListener != null) {
//            mListener.onAudioFormatChanged(mMediaCodec.getOutputFormat());
//        }
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            LogUtil.d(TAG, "offerEncoder() -- Audio Format Changed");
        }
        while (outputBufferIndex >= 0) {
            if (Build.VERSION.SDK_INT >= 21) {
                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
            } else {
                outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
            }

            LogUtil.i(TAG, "编码音频完成...");
//            if (mListener != null) {
//                mListener.onAudioEncode(outputBuffer, mBufferInfo);
//            }
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
        }
    }

    public static void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((sProfile - 1) << 6) + (sFreqIdx << 2) + (sChanCfg >> 2));
        packet[3] = (byte) (((sChanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
