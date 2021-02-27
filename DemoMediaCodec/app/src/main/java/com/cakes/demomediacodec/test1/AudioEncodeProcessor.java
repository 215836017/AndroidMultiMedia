package com.cakes.demomediacodec.test1;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodecInfo;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.os.Build;
import android.os.Process;

import androidx.annotation.RequiresApi;

import com.cakes.utils.LogUtil;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AudioEncodeProcessor extends Thread {

    private final String TAG = "AudioEncodeProcessor";

    public static final int DEFAULT_FREQUENCY = 16000;
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_CHANNEL_COUNT = 1;
    public static final boolean DEFAULT_AEC = true;

    public static final int DEFAULT_BPS = 32000;
    public static final String DEFAULT_MIME = "audio/mp4a-latm";
    public static final int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    private AudioRecord audioRecord;
    private static AcousticEchoCanceler mAec;
    private static AutomaticGainControl mAgc;
    private int recordBufferSize;
    private byte[] mRecordBuffer;

    private AudioHardEncoder audioHardEncoder;
    private AudioHardEncoder2 audioHardEncoder2;

    private boolean isAudioEncoding;
    /*** 是否静音 */
    private boolean isMute;

    public AudioEncodeProcessor() {
        this(null);
    }

    public AudioEncodeProcessor(OnAudioEncodeListener onAudioEncodeListener) {
        initAudioRecord();
        initAudioEncoder(onAudioEncodeListener);

        isMute = false;
    }

    private void initAudioRecord() {
        int audioSource = MediaRecorder.AudioSource.MIC;
        if (DEFAULT_AEC) {
            audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        }
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        if (DEFAULT_CHANNEL_COUNT == 2) {
            channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        }
        recordBufferSize = getRecordBufferSize();
        audioRecord = new AudioRecord(audioSource, DEFAULT_FREQUENCY,
                channelConfiguration, DEFAULT_AUDIO_ENCODING, recordBufferSize);
        mRecordBuffer = new byte[recordBufferSize];

        if (AcousticEchoCanceler.isAvailable()) {
            mAec = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            if (mAec != null) {
                mAec.setEnabled(true);
                LogUtil.i(TAG, "mAec.setEnabled(true)");
            }
        }

        if (AutomaticGainControl.isAvailable()) {
            mAgc = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            if (mAgc != null) {
                mAgc.setEnabled(true);
                LogUtil.i(TAG, "mAgc.setEnabled(true)");
            }
        }
    }

    private int getRecordBufferSize() {
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        if (DEFAULT_CHANNEL_COUNT == 2) {
            channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
        int size = AudioRecord.getMinBufferSize(DEFAULT_FREQUENCY, channelConfiguration, DEFAULT_AUDIO_ENCODING);
        return size;
    }

    private void initAudioEncoder(OnAudioEncodeListener onAudioEncodeListener) {
        if (null != onAudioEncodeListener) {
//            audioHardEncoder = new AudioHardEncoder(onAudioEncodeListener);
            audioHardEncoder2 = new AudioHardEncoder2(onAudioEncodeListener);
        } else {
//            audioHardEncoder = new AudioHardEncoder();
            audioHardEncoder2 = new AudioHardEncoder2();
        }


//        audioHardEncoder.prepareEncoder(DEFAULT_MIME, DEFAULT_FREQUENCY, DEFAULT_CHANNEL_COUNT,
//                DEFAULT_AAC_PROFILE, DEFAULT_BPS, DEFAULT_AUDIO_ENCODING);
        audioHardEncoder2.prepareEncoder(DEFAULT_MIME, DEFAULT_FREQUENCY, DEFAULT_CHANNEL_COUNT,
                DEFAULT_AAC_PROFILE, DEFAULT_BPS, DEFAULT_AUDIO_ENCODING);
    }

    public int getSessionId() {
        if (audioRecord != null) {
            int audioSessionId = audioRecord.getAudioSessionId();
            LogUtil.i(TAG, "mAudioRecord.getAudioSessionId() = "
                    + audioSessionId);
            return audioSessionId;
        } else {
            return -1;
        }
    }

    public boolean isAudioEncoding() {
        return isAudioEncoding;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean isMute) {
        this.isMute = isMute;
    }

    public void stopEncode() {
        isAudioEncoding = false;
    }

    private void releaseAudioRecord() {
        if (audioRecord != null) {
            LogUtil.d(TAG, "releaseAudioRecord() -- stop and release audioRecord");
            audioRecord.stop();
            audioRecord.release();
        }
        audioRecord = null;

        if (null != audioHardEncoder) {
            audioHardEncoder.stop();
        }
        if (null != audioHardEncoder2) {
            audioHardEncoder2.stop();
        }
    }

    private void startRecord() {
        if (null == audioRecord) {
            LogUtil.d(TAG, "startRecord() -- init AudioRecord instance and startRecording... ");
            initAudioRecord();
        }
        audioRecord.startRecording();
    }

    @Override
    public void run() {
        super.run();

        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        startRecord();

        isAudioEncoding = true;
        int readLen;
        while (isAudioEncoding) {
            readLen = audioRecord.read(mRecordBuffer, 0, recordBufferSize);
            if (readLen < 0) {
                LogUtil.e(TAG, "length of audioRecord.read() < 0");
                isAudioEncoding = false;
                break;
            }

            if (isMute) {
                byte clearM = 0;
                Arrays.fill(mRecordBuffer, clearM);
            }

            if (null != audioHardEncoder) {
                audioHardEncoder.offerEncoder(mRecordBuffer);
            }
            if (null != audioHardEncoder2) {
                audioHardEncoder2.offerEncoder(mRecordBuffer);
            }
        }

        releaseAudioRecord();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mAgc != null) {
            mAgc.setEnabled(false);
            mAgc.release();
            mAgc = null;
        }

        if (mAgc != null) {
            mAgc.setEnabled(false);
            mAgc.release();
            mAgc = null;
        }
    }
}
