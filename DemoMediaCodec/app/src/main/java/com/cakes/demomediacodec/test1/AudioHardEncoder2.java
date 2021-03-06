package com.cakes.demomediacodec.test1;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.cakes.demomediacodec.mediaCodec.MediaCodecHelper;
import com.cakes.utils.LogUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * MediaCodec 的异步方式
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AudioHardEncoder2 {

    private final String TAG = "AudioHardEncoder2";

    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private OnAudioEncodeListener onAudioEncodeListener;

    private Handler audioEncoderHandler;
    private HandlerThread audioEncoderHandlerThread = new HandlerThread("AudioEncoder");

    private final int CACHE_BUFFER_SIZE = 10;
    private final ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(CACHE_BUFFER_SIZE);
    private byte[] dataSources;

    public AudioHardEncoder2() {
        this(null);
    }

    public AudioHardEncoder2(OnAudioEncodeListener onAudioEncodeListener) {
        this.onAudioEncodeListener = onAudioEncodeListener;

        initWorkHandler();
    }

    private void initWorkHandler() {
        audioEncoderHandlerThread.start();
        audioEncoderHandler = new Handler(audioEncoderHandlerThread.getLooper());
    }

    public void prepareEncoder(String mime, int frequency, int channelCount,
                               int aacProfile, int bps, int audioEncoding) {
        mMediaCodec = MediaCodecHelper.getAudioEncoder(mime, frequency, channelCount,
                aacProfile, bps, audioEncoding, mCallback, audioEncoderHandler);
        mMediaCodec.start();
        LogUtil.d(TAG, "prepareEncoder() -- 编码器开启成功...");
    }

    synchronized public void stop() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    synchronized void offerEncoder(byte[] input) {
        LogUtil.i(TAG, "把录音数据存入列表中...");
        mInputDatasQueue.offer(input);
    }

    int len = 0;
    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            LogUtil.d(TAG, "onInputBufferAvailable() -- 1111111, index = " + index);

            while (dataSources == null) {
                try {
                    dataSources = mInputDatasQueue.poll(20, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                    LogUtil.d(TAG, "onInputBufferAvailable() -- " + e.getMessage());
                    dataSources = null;
                }
            }

            if (index < 0) {
                return;
            }

            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            inputBuffer.put(dataSources);
            len = dataSources.length;
            mMediaCodec.queueInputBuffer(index, 0, len, 0, 0);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                            @NonNull MediaCodec.BufferInfo info) {
//            LogUtil.w(TAG, "onOutputBufferAvailable() -- 1111111, index = " + index);
            if (index < 0 || null == mMediaCodec) {
                return;
            }
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);
            if (null != onAudioEncodeListener) {
//                LogUtil.i(TAG, "onOutputBufferAvailable() -- 222222");
                onAudioEncodeListener.onAudioEncode(outputBuffer, info);
            }
            mMediaCodec.releaseOutputBuffer(index, true);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            LogUtil.e(TAG, "onError() -- " + e.getMessage());
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            LogUtil.d(TAG, "onOutputFormatChanged() -- 11111111");
        }
    };
}
