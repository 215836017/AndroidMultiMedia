package com.cakes.demomediacodec.test3;

import android.media.MediaCodec;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoEncodeResult implements OnVideoEncodeListener {

    private final String TAG = "VideoEncodeResult";

    public static final String TEST_DIR_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "testCodec" + File.separator;
    public static final String FILE_H264 = "test.h264";

    private FileOutputStream fileOutputStream;

    public VideoEncodeResult() {
        initFile();
    }

    private void initFile() {
        File file = new File(TEST_DIR_PATH + FILE_H264);
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
    public void onVideoEncode(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        byte[] outData = new byte[info.size];
        buffer.get(outData);

        if (null != fileOutputStream) {
            try {
                fileOutputStream.write(outData, 0, outData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
