package com.cakes.demomediacodec.test2;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;

import com.cakes.demomediacodec.test1.AudioEncodeResult;
import com.cakes.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class AudioDecodeProcessor extends Thread {

    private final String TAG = "AudioDecodeProcessor";

    public static final int DEFAULT_FREQUENCY = 16000;
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_CHANNEL_COUNT = 1;
    public static final boolean DEFAULT_AEC = true;

    public static final int DEFAULT_BPS = 32000;
    public static final String DEFAULT_MIME = "audio/mp4a-latm";
    public static final int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    //文件路径
    private String filePath;
    //文件读取完成标识
    private boolean isFinish = false;
    //这个值用于找到第一个帧头后，继续寻找第二个帧头，如果解码失败可以尝试缩小这个值
    private int FRAME_MIN_LEN = 50;
    //一般AAC帧大小不超过200k,如果解码失败可以尝试增大这个值
    private static int FRAME_MAX_LEN = 100 * 1024;
    //根据帧率获取的解码每帧需要休眠的时间,根据实际帧率进行操作
    private int PRE_FRAME_TIME = 1000 / 50;
    //记录获取的帧数
    private int count = 0;

    private AudioHardDecoder audioHardDecoder;

    public AudioDecodeProcessor() {
        initAudioDecoder();

        filePath = AudioEncodeResult.TEST_DIR_PATH + AudioEncodeResult.FILE_AAC;
    }

    private void initAudioDecoder() {
        if (null == audioHardDecoder) {
            audioHardDecoder = new AudioHardDecoder();
            audioHardDecoder.initDecoder(DEFAULT_MIME, DEFAULT_CHANNEL_COUNT, DEFAULT_FREQUENCY);
        }
    }

    private void release() {

    }

    public void stopDecode() {

    }


    @Override
    public void run() {
        super.run();

        File file = new File(filePath);
        if (null == file || !file.exists()) {
            LogUtil.e(TAG, "aac file is no exist!!!");
            return;
        }

        //保存完整数据帧
        byte[] frame = new byte[FRAME_MAX_LEN];
        //当前帧长度
        int frameLen = 0;
        //每次从文件读取的数据
        byte[] readData = new byte[10 * 1024];
        //开始时间
        long startTime = System.currentTimeMillis();
        int readLen;
        try {
            FileInputStream fis = new FileInputStream(file);
            while (!isFinish) {
                if (fis.available() > 0) {
                    readLen = fis.read(readData);
                    //当前长度小于最大值
                    if (frameLen + readLen < FRAME_MAX_LEN) {
                        //将readData拷贝到frame
                        System.arraycopy(readData, 0, frame, frameLen, readLen);
                        //修改frameLen
                        frameLen += readLen;
                        //寻找第一个帧头
                        int headFirstIndex = findHead(frame, 0, frameLen);
                        while (headFirstIndex >= 0 && isHead(frame, headFirstIndex)) {
                            //寻找第二个帧头
                            int headSecondIndex = findHead(frame, headFirstIndex + FRAME_MIN_LEN, frameLen);
                            //如果第二个帧头存在，则两个帧头之间的就是一帧完整的数据
                            if (headSecondIndex > 0 && isHead(frame, headSecondIndex)) {
                                //视频解码
                                count++;
                                LogUtil.e(TAG, "Length : " + (headSecondIndex - headFirstIndex));

                                // todo
//                                audioUtil.decode(frame, headFirstIndex, headSecondIndex - headFirstIndex);
                                //截取headSecondIndex之后到frame的有效数据,并放到frame最前面
                                byte[] temp = Arrays.copyOfRange(frame, headSecondIndex, frameLen);
                                System.arraycopy(temp, 0, frame, 0, temp.length);
                                //修改frameLen的值
                                frameLen = temp.length;
                                //线程休眠
                                sleepThread(startTime, System.currentTimeMillis());
                                //重置开始时间
                                startTime = System.currentTimeMillis();
                                //继续寻找数据帧
                                headFirstIndex = findHead(frame, 0, frameLen);
                            } else {
                                //找不到第二个帧头
                                headFirstIndex = -1;
                            }
                        }
                    } else {
                        //如果长度超过最大值，frameLen置0
                        frameLen = 0;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 寻找指定buffer中AAC帧头的开始位置
     *
     * @param startIndex 开始的位置
     * @param data       数据
     * @param max        需要检测的最大值
     * @return
     */
    private int findHead(byte[] data, int startIndex, int max) {
        int i;
        for (i = startIndex; i <= max; i++) {
            //发现帧头
            if (isHead(data, i))
                break;
        }
        //检测到最大值，未发现帧头
        if (i == max) {
            i = -1;
        }
        return i;
    }

    /**
     * 判断aac帧头
     */
    private boolean isHead(byte[] data, int offset) {
        boolean result = false;
        if (data[offset] == (byte) 0xFF && data[offset + 1] == (byte) 0xF1
                && data[offset + 3] == (byte) 0x80) {
            result = true;
        }
        return result;
    }

    //修眠
    private void sleepThread(long startTime, long endTime) {
        //根据读文件和解码耗时，计算需要休眠的时间
        long time = PRE_FRAME_TIME - (endTime - startTime);
        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
