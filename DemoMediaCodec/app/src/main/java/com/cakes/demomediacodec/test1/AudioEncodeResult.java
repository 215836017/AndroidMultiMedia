package com.cakes.demomediacodec.test1;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;

import com.cakes.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * https://blog.csdn.net/zjfengdou30/article/details/81276154
 */
public class AudioEncodeResult implements OnAudioEncodeListener {

    private final String TAG = "AudioEncodeResult";

    public static final String TEST_DIR_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "testCodec" + File.separator;
    public static final String FILE_AAC = "test.aac";
    public static final String FILE_PCM = "test.pcm";

    private FileOutputStream fileOutputStream;
    private volatile long mStartedTime = 0;

    private final int ADTSSize = 7;
    private int outPacketSize;
    private byte[] outData;

    public AudioEncodeResult() {
        initFile();

        mStartedTime = System.nanoTime() / 1000;
    }

    private void initFile() {
        File file = new File(TEST_DIR_PATH + FILE_AAC);
        if (null == file) {
            return;
        }
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAudioEncode(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        LogUtil.d(TAG, "onAudioEncode() -- 11111111111");
        if (null == fileOutputStream) {
            return;
        }

        outPacketSize = info.size + ADTSSize;
        outData = new byte[outPacketSize];
        buffer.position(info.offset);
        buffer.limit(info.offset + info.size);
        AudioHardEncoder.addADTStoPacket(outData, outPacketSize);
        buffer.get(outData, ADTSSize, info.size);
        LogUtil.d(TAG, "onAudioEncode() -- 222222222, info.flags = " + info.flags
                + ", info.presentationTimeUs = " + info.presentationTimeUs
                + ", info.size = " + info.size
                + ", info.offset = " + info.offset
                + ", mStartedTime = " + mStartedTime);

        // MediaCodec同步方式
        if (MediaCodec.BUFFER_FLAG_CODEC_CONFIG != info.flags
                && info.presentationTimeUs > mStartedTime) {

            // MediaCodec异步方式
//        if (MediaCodec.BUFFER_FLAG_CODEC_CONFIG != info.flags) {
            try {
                LogUtil.d(TAG, "onAudioEncode() -- 把编码的数据写入文件");
                fileOutputStream.write(outData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAudioFormatChanged(MediaFormat format) {
    }

    @Override
    public void onAudioPCM(byte[] data, int len) {
    }

    public void release() {
        if (null != fileOutputStream) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fileOutputStream = null;
            }
        }
    }
}
