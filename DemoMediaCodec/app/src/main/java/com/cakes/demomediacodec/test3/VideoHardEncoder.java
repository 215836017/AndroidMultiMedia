package com.cakes.demomediacodec.test3;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.cakes.demomediacodec.mediaCodec.MediaCodecHelper;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class VideoHardEncoder {
    private final static String TAG = "VideoHardEncoder";

    private int TIMEOUT_USEC = 12000;

    private MediaCodec mediaCodec;
    int mWidth;
    int mHeight;
    int mFrameRate;
    public boolean isRunning = false;

    private static int YUV_QUEUE_SIZE = 10;
    //待解码视频缓冲队列，静态成员！
    private ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(YUV_QUEUE_SIZE);

    private OnVideoEncodeListener onVideoEncodeListener;

    public VideoHardEncoder(int width, int height, int frameRate) {

        mWidth = width;
        mHeight = height;
        mFrameRate = frameRate;

        initMediaCodec();
        initEncodeListener();
    }

    private void initMediaCodec() {
        mediaCodec = MediaCodecHelper.getVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC,
                mWidth, mHeight, mFrameRate);
        if (null != mediaCodec) {
            //启动编码器
            mediaCodec.start();
        }
    }

    private void initEncodeListener() {
        onVideoEncodeListener = new VideoEncodeResult();
    }

    public void inputYUVToQueue(byte[] data) {
        if (!isRunning) {
            return;
        }
        if (YUVQueue.size() > YUV_QUEUE_SIZE) {
            YUVQueue.poll();
        }
        YUVQueue.offer(data);
    }

    private void stopEncoder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        isRunning = false;
        stopEncoder();
    }

    public void startEncoderThread() {
        Thread EncoderThread = new Thread(new Runnable() {

            @Override
            public void run() {
                isRunning = true;
                byte[] input = null;
                long pts = 0;
                long generateIndex = 0;
                byte[] rotatedData = null;
                byte[] yuv420sp = new byte[mWidth * mHeight * 3 / 2];
                ByteBuffer outputBuffer = null;
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                while (isRunning) {
                    if (YUVQueue.size() > 0) {
                        // 从缓冲队列中取出一帧
                        input = YUVQueue.poll();

                        // 把待编码的视频帧转换为YUV420格式
                        NV21ToNV12(input, yuv420sp, mWidth, mHeight);
                        rotatedData = RotateUtil.rotateNV290(yuv420sp, mWidth, mHeight);
                    }
                    if (rotatedData != null) {
                        try {
                            long startMs = System.currentTimeMillis();
                            //编码器输入缓冲区
                            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                            //编码器输出缓冲区
                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                pts = computePresentationTime(generateIndex);
                                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                //把转换后的YUV420格式的视频帧放到编码器输入缓冲区中
                                inputBuffer.put(rotatedData);
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, rotatedData.length, pts, 0);
                                generateIndex += 1;
                            }

                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            while (outputBufferIndex >= 0) {
                                //Log.i("VideoHardEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
                                outputBuffer = outputBuffers[outputBufferIndex];
//                                byte[] outData = new byte[bufferInfo.size];
//                                outputBuffer.get(outData);

                                if (null != onVideoEncodeListener) {
                                    onVideoEncodeListener.onVideoEncode(outputBuffer, bufferInfo);
                                }
                                //   outputStream.write(outData, 0, outData.length);
                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            }

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        EncoderThread.start();
    }

    /**
     * 因为从MediaCodec不支持NV21的数据编码，所以需要先把NV21的数据转码为NV12
     */
    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / mFrameRate;
    }
}