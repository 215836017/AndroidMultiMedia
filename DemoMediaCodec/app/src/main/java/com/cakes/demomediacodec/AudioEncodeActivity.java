package com.cakes.demomediacodec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cakes.demomediacodec.audio.PcmToWavUtil;
import com.cakes.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioEncodeActivity extends AppCompatActivity {

    private final String TAG = "AudioEncodeActivity";

    private Button btnEncodeAudio;

    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private final String MIME = MediaFormat.MIMETYPE_AUDIO_AAC;
    private final int SAMPLE_RATE = 16 * 1000;
    private final int CHANNEL_COUNT = AudioFormat.CHANNEL_IN_MONO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;  // 16bits, 32bits
    private final int DEFAULT_CHANNEL_COUNT = 1;

    private AudioRecord audioRecord;
    private int recordBuffSize = 0;
    private byte[] recordData;
    private boolean isStartEncode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_encode);

        btnEncodeAudio = findViewById(R.id.en_audio_start);
        btnEncodeAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrStopEncode();
            }
        });
    }

    private void initAudio() {

        recordBuffSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_COUNT, AUDIO_FORMAT);
        recordData = new byte[recordBuffSize];
        LogUtil.i(TAG, "initAudio() -- recordBuffSize = " + recordBuffSize);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                CHANNEL_COUNT, AUDIO_FORMAT, recordBuffSize);
    }

    private void prepareMediaCodec() {
        try {
            mediaFormat = MediaFormat.createAudioFormat(MIME, SAMPLE_RATE, DEFAULT_CHANNEL_COUNT);
            //  MediaFormat format = MediaFormat.createAudioFormat(configuration.mime, configuration.frequency, configuration.channelCount);
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 32000);
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
            int maxInputSize = recordBuffSize;
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);

            mediaCodec = MediaCodec.createEncoderByType(MIME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaCodec.setCallback(new MyMediaCodecCallback());
            }
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File fileWav;
    private FileOutputStream fisWav;
    private ByteArrayOutputStream baos = null;
    private File fileAac;
    private FileOutputStream fisAac;

    private void initFileWav() {
        fileWav = new File("/sdcard/testWav.wav");
        if (null == fileWav) {
            return;
        }

        File parentFile = fileWav.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            if (!fileWav.exists()) {
                fileWav.createNewFile();
            }
            fisWav = new FileOutputStream(fileWav);

        } catch (Exception e) {
        }
    }

    private void initFileAac() {
        fileAac = new File("/sdcard/test.aac");
        if (null == fileAac) {
            return;
        }

        File parentFile = fileAac.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            if (!fileAac.exists()) {
                fileAac.createNewFile();
            }
            fisAac = new FileOutputStream(fileAac);

        } catch (Exception e) {
        }
    }

    private void stopAndReleaseAll() {
        if (null != audioRecord && audioRecord.getState() != AudioRecord.RECORDSTATE_STOPPED) {
            audioRecord.stop();
            audioRecord.release();
        }
        audioRecord = null;

        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
        }
        mediaCodec = null;

    }

    private void startOrStopEncode() {
        if (isStartEncode) {
            isStartEncode = false;
            btnEncodeAudio.setText("开始音频编码");
            stopAndReleaseAll();

        } else {
            isStartEncode = true;
            btnEncodeAudio.setText("结束音频编码");

            initAudio();
            prepareMediaCodec();
            initFileWav();
            initFileAac();
            startRecordThread();
        }

    }

    private void startRecordThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                audioRecord.startRecording();
                mediaCodec.start();

                int audioRecordState = audioRecord.getState();
                int len;

                ByteArrayOutputStream baos = null;

                baos = new ByteArrayOutputStream();
                while (isStartEncode) {
                    len = audioRecord.read(recordData, 0, recordBuffSize);
                    LogUtil.i(TAG, "len = " + len);
                    if (AudioRecord.ERROR_INVALID_OPERATION != len) {
                        LogUtil.i(TAG, "recordDatas.len = " + recordData.length);

                        // offerEncoder(recordData);

                        baos.write(recordData, 0, len);
                    }
                }
                if (null != audioRecord && audioRecord.getState() != AudioRecord.RECORDSTATE_STOPPED) {
                    audioRecord.stop();
                }

                try {
                    byte[] allAuidoBytes = baos.toByteArray();
                    fisWav.write(PcmToWavUtil.getWavHeader(CHANNEL_COUNT, allAuidoBytes.length, SAMPLE_RATE));
                    fisWav.write(allAuidoBytes);
                } catch (Exception e) {
                }
            }
        }.start();
    }

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private void offerEncoder(byte[] input) {

        // 送入编码器
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(12000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = null;
            if (Build.VERSION.SDK_INT >= 21) {
                inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
            }

            inputBuffer.clear();
            inputBuffer.put(input);
            long pts = System.nanoTime() / 1000;
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
        }

        // 从编码器获取
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
        // if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED && mListener != null) {
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            //  mListener.onAudioFormatChanged(mMediaCodec.getOutputFormat());
        }
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer;
            if (Build.VERSION.SDK_INT >= 21) {
                outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            } else {
                outputBuffer = mediaCodec.getOutputBuffers()[outputBufferIndex];
            }


            saveToFile(outputBuffer, mBufferInfo);
//            if (mListener != null) {
//                mListener.onAudioEncode(outputBuffer, mBufferInfo);
//            }
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
        }
    }


    private void saveToFile(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        LogUtil.i(TAG, "saveToFile() -- 1111111");
        int ADTSSize = 7;
        final int outPacketSize = info.size + ADTSSize;
        final byte[] outData = new byte[outPacketSize];
        buffer.position(info.offset);
        buffer.limit(info.offset + info.size);
        addADTStoPacket(outData, outPacketSize);
        if (null != fisAac) {
            try {
                fisAac.write(outData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LogUtil.i(TAG, "saveToFile() -- end end end ");
    }

    static final int sProfile = 2;
    static final int sFreqIdx = 8;
    static final int sChanCfg = 1;

    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((sProfile - 1) << 6) + (sFreqIdx << 2) + (sChanCfg >> 2));
        packet[3] = (byte) (((sChanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

//    private MediaCodec.Callback mediaCodecCallback = new MediaCodec.Callback() {
//        @Override
//        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
//
//        }
//
//        @Override
//        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
//
//        }
//
//        @Override
//        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
//
//        }
//
//        @Override
//        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
//
//        }
//    };

}