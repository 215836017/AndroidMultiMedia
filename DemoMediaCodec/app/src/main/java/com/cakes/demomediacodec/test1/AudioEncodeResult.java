package com.cakes.demomediacodec.test1;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;

import com.cakes.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * https://blog.csdn.net/zjfengdou30/article/details/81276154
 */
public class AudioEncodeResult implements OnAudioEncodeListener {

    private final String TAG = "AudioEncodeResult";

    private final String TEST_DIR_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "testCodec" + File.separator;
    private final String FILE_AAC = "test.aac";
    private final String FILE_PCM = "test.pcm";

    private FileOutputStream fileOutputStream;
    private volatile long mStartedTime = 0;

    private final int ADTSSize = 7;
    private int outPacketSize;
    private byte[] outData;
    private int pts;

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
                fileOutputStream = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAudioEncode(ByteBuffer buffer, MediaCodec.BufferInfo info) {

        if (null == fileOutputStream) {
            return;
        }

        outPacketSize = info.size + ADTSSize;
        outData = new byte[outPacketSize];
        buffer.position(info.offset);
        buffer.limit(info.offset + info.size);
        AudioHardEncoder.addADTStoPacket(outData, outPacketSize);
        buffer.get(outData, ADTSSize, info.size);

        if (MediaCodec.BUFFER_FLAG_CODEC_CONFIG != info.flags
                && info.presentationTimeUs > mStartedTime) {
            pts = (int) (info.presentationTimeUs - mStartedTime) / 1000;
//            LogUtil.i(TAG, "audio pts = " + pts + ",mStartedTime = " + mStartedTime
//                    + ",ms = " + info.presentationTimeUs + ",size = " + info.size
//                    + ",flag = " + info.flags);
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
