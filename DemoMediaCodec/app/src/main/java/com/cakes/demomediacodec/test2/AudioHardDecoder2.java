package com.cakes.demomediacodec.test2;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.text.TextUtils;

import com.cakes.demomediacodec.mediaCodec.MediaCodecHelper;
import com.cakes.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioHardDecoder2 {

    private final String TAG = "AudioHardDecoder2";

    private MediaCodec mediaCodec;
    private OnAudioDecodeListener onAudioDecodeListener;

    private ByteBuffer inputBuffer = null;
    private ByteBuffer outputBuffer = null;
    private int inputBufferIndex;
    private int outputBufferIndex;
    private MediaCodec.BufferInfo bufferInfo;
    private MediaExtractor mediaExtractor;

    private boolean sawInputEOS = false;
    private boolean sawOutputEOS = false;
    private int sampleSize;

    public AudioHardDecoder2() {
    }

    public AudioHardDecoder2(OnAudioDecodeListener onAudioDecodeListener) {
        this.onAudioDecodeListener = onAudioDecodeListener;

    }

    public void prepareDecoder(String aacFilePath) {
        if (TextUtils.isEmpty(aacFilePath)) {
            LogUtil.e(TAG, "prepareDecoder() -- error: aacFilePath is null or empty!");
            return;
        }
        mediaExtractor = new MediaExtractor();
        MediaFormat mediaFormat = null;
        try {
            mediaExtractor.setDataSource(aacFilePath);
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (TextUtils.isEmpty(mime) && mime.startsWith("audio/")) {
                    LogUtil.i(TAG, "prepareDecoder() -- i = " + i + ", mime = " + mime);
                    mediaExtractor.selectTrack(i);
                    mediaFormat = format;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaCodec = MediaCodecHelper.getAudioDecoder(mediaFormat);
        if (null == mediaCodec) {
            LogUtil.e(TAG, "prepareDecoder() -- error: null == mediaCodec");
            return;
        }
        mediaCodec.start();
        LogUtil.i(TAG, "prepareDecoder() -- call mediaCodec.start() success");
    }

    synchronized public void release() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if (null != mediaExtractor) {
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }

    synchronized void startDecoderAacFile() {
        if (null == mediaCodec) {
            return;
        }

        try {
            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    inputBufferIndex = mediaCodec.dequeueInputBuffer(12000);
                    LogUtil.w(TAG, "onOutputBufferAvailable() -- 1111111, inputBufferIndex = " + inputBufferIndex);
                    if (inputBufferIndex >= 0) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
                        }

                        sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            LogUtil.i(TAG, "saw input EOS.");
                            sawInputEOS = true;
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                            mediaExtractor.advance();
                        }
                    }
                }

                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1200);
                LogUtil.w(TAG, "onOutputBufferAvailable() -- 2222, index = " + outputBufferIndex
                        + ", info.size = " + bufferInfo.size + ", info.flags = " + bufferInfo.flags
                        + ", info.presentationTimeUs = " + bufferInfo.presentationTimeUs
                        + ", info.offset = " + bufferInfo.offset);
                if (outputBufferIndex >= 0) {
                    // Simply ignore codec config buffers.
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        LogUtil.i(TAG, "audio encoder: codec config buffer");
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        continue;
                    }

                    if (bufferInfo.size != 0) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                        } else {
                            outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];
                        }
//                        ByteBuffer outBuf = codecOutputBuffers[outputBufferIndex];
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        byte[] data = new byte[bufferInfo.size];
                        outputBuffer.get(data);
                    }

                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        LogUtil.i(TAG, "saw output EOS.");
                        sawOutputEOS = true;
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    LogUtil.i(TAG, "output buffers have changed.");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat outputFormatChange = mediaCodec.getOutputFormat();
                    LogUtil.i(TAG, "output format has changed to " + outputFormatChange);
                }
            }
        } finally {
            LogUtil.i(TAG, "decodeAacToPcm finish");
            release();
        }
    }

}
