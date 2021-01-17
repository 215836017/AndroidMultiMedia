package com.cakes.democamera2.codec;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;

import com.cakes.democamera2.LogUtil;
import com.cakes.democamera2.Test3Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class AvcEncoder {
    private final static String TAG = "AvcEncoder";

    private final int TIME_OUT_USE = 12000;

    private MediaCodec mediaCodec;
    private final String MIME = "video/avc";
    private int mWidth;
    private int mHeight;
    private int mFrameRate;
    private int bitRate;
    private int ifi;
    public boolean isRunning = false;

    private static int YUV_QUEUE_SIZE = 10;
    //待解码视频缓冲队列，静态成员！
    private ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(YUV_QUEUE_SIZE);

    private OnEncodeDataListener onEncodeDataListener;

    public AvcEncoder() {
    }

    private FileOutputStream h264Fos;

    private void initFile() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testCamera2";

        File h264File = new File(dir + "/camera2_" + System.currentTimeMillis() + ".h264");
        if (null != h264File) {
            File parentFile = h264File.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            if (!h264File.exists()) {
                try {
                    h264File.createNewFile();
                    h264Fos = new FileOutputStream(h264File);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setVideoConfiguration(VideoConfiguration configuration) {
        mWidth = configuration.width;
        mHeight = configuration.height;
        mFrameRate = configuration.fps;
        bitRate = configuration.bps;
        ifi = configuration.ifi;
        initMediaCodec();
    }

    private void initMediaCodec() {
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME, mHeight, mWidth);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);

        LogUtil.d(TAG, "bitRate = " + bitRate);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, ifi);
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
//        mediaFormat.setInteger(MediaFormat.KEY_COMPLEXITY,MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR );
        // 不支持设置Profile和Level，而应该采用默认设置
//        mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
//        mediaFormat.setInteger("level", MediaCodecInfo.CodecProfileLevel.AVCLevel41); // Level 4.1
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //配置编码器参数
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //启动编码器
        mediaCodec.start();
    }

    public void inputYUVToQueue(byte[] data) {
        if (isRunning) {
            if (YUVQueue.size() <= YUV_QUEUE_SIZE) {
                YUVQueue.offer(data);
            }
        }
    }

    private void stopEncoder() {
        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
        }
        mediaCodec = null;
    }


    public void requestSyncFrame() {
        if (mediaCodec == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        mediaCodec.setParameters(bundle);
    }

    public void stopThread() {
        isRunning = false;
        stopEncoder();
    }

    public interface OnEncodeDataListener {
        void onEncodedData(byte[] data, int ptsSend);
    }

    public void setOnEncodeDataListener(OnEncodeDataListener onEncodeDataListener) {
        this.onEncodeDataListener = onEncodeDataListener;
    }

    public void startEncoderThread(final int cameraId) {
        Thread encoderThread = new Thread(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {

                initFile();

                isRunning = true;
                byte[] input = null;
                byte[] yuv420sp = new byte[mWidth * mHeight * 3 / 2];
                long pts = 0;
                long generateIndex = 0;

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int ptsSend;
                long mStartedTime = System.nanoTime() / 1000;

                while (isRunning) {
                    if (YUVQueue.size() > 0) {
                        //从缓冲队列中取出一帧
                        input = YUVQueue.poll();
                        if (cameraId == Test3Activity.CAMERA_ID_BACK) {
                            input = RotateUtil.rotate90(input, mWidth, mHeight);

                        } else if (cameraId == Test3Activity.CAMERA_ID_FRONT) {
                            input = RotateUtil.rotate270(input, mWidth, mHeight);
                        }
                        //把待编码的视频帧转换为YUV420格式
                        NV21ToNV12(input, yuv420sp, mWidth, mHeight);
                        input = yuv420sp;
                    }
                    if (input != null) {
                        try {
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
                                inputBuffer.put(input);
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                                generateIndex += 1;
                            }

                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIME_OUT_USE);
                            while (outputBufferIndex >= 0) {
                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
                                ptsSend = (int) (bufferInfo.presentationTimeUs - mStartedTime) / 1000;

                                h264Fos.write(outData); // 写入文件中

                                if (null != onEncodeDataListener) {
                                    onEncodeDataListener.onEncodedData(outData, ptsSend);
                                }
                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIME_OUT_USE);
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
        encoderThread.start();
    }

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