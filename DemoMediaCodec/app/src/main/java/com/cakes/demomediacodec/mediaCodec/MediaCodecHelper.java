package com.cakes.demomediacodec.mediaCodec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.view.Surface;

import com.cakes.utils.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaCodecHelper {

    private static final String TAG = "MediaCodecHelper";
    private static final String AUDIO_DEFAULT_MIME = "audio/mp4a-latm";

    public static MediaCodec getAudioEncoder(String mime, int frequency, int channelCount,
                                             int aacProfile, int bps, int audioEncoding,
                                             MediaCodec.Callback mCallback, Handler handler) {
        LogUtil.d(TAG, "getAudioMediaCodec() -- mime = " + mime);
        MediaFormat format = MediaFormat.createAudioFormat(mime,
                frequency, channelCount);

        if (mime.equals(AUDIO_DEFAULT_MIME)) {
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile);
        }
        format.setInteger(MediaFormat.KEY_BIT_RATE, bps);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, frequency);
        int maxInputSize = getRecordBufferSize(frequency, channelCount, audioEncoding);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);

        MediaCodec audioEncode = null;
        try {
            audioEncode = MediaCodec.createEncoderByType(mime);

            // TODO: 使用异步方式的话，必须在configure()方法之前设置回调。
            if (null != mCallback) {
                if (Build.VERSION.SDK_INT >= 23 && null != handler) {
                    LogUtil.d(TAG, "设置MediaCodec的异步方式：包含Handler");
                    audioEncode.setCallback(mCallback, handler);
                } else if (Build.VERSION.SDK_INT >= 21) {
                    LogUtil.d(TAG, "设置MediaCodec的异步方式：不 不 不包含Handler");
                    audioEncode.setCallback(mCallback);
                }
            }
            audioEncode.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            LogUtil.e(TAG, "get audio encoder audioEncode is error: " + e.getMessage());
            if (audioEncode != null) {
                audioEncode.stop();
                audioEncode.release();
                audioEncode = null;
            }
        }
        return audioEncode;
    }

    private static int getRecordBufferSize(int frequency, int channelCount, int audioEncoding) {
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        if (channelCount == 2) {
            channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
        int size = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        return size;
    }

    public static MediaCodec getAudioDecoder(String mine, int channelCount, int sampleRate) {
        MediaCodec mDecoder = null;
        try {
            //需要解码数据的类型
//            String mine = "audio/mp4a-latm";
            //初始化解码器
            mDecoder = MediaCodec.createDecoderByType(mine);
            //MediaFormat用于描述音视频数据的相关参数
            MediaFormat mediaFormat = new MediaFormat();
            //数据类型
            mediaFormat.setString(MediaFormat.KEY_MIME, mine);
            //声道个数
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
            //采样率
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
            //比特率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
            //用来标记AAC是否有adts头，1->有
            mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
            //用来标记aac的类型
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //ByteBuffer key（暂时不了解该参数的含义，但必须设置）
            byte[] data = new byte[]{(byte) 0x11, (byte) 0x90};
            ByteBuffer csd_0 = ByteBuffer.wrap(data);
            mediaFormat.setByteBuffer("csd-0", csd_0);
            //解码器配置
            mDecoder.configure(mediaFormat, null, null, 0);
        } catch (Exception e) {
            mDecoder = null;
        }

        return mDecoder;
    }

    public static MediaCodec getAudioDecoder(MediaFormat mediaFormat) {
        if (null == mediaFormat) {
            return null;
        }

        MediaCodec mDecoder = null;
        try {
            //初始化解码器
            String mediaMime = mediaFormat.getString(MediaFormat.KEY_MIME);
            LogUtil.i(TAG, "getAudioDecoder() -- mediaMime = " + mediaMime);
            mDecoder = MediaCodec.createDecoderByType(mediaMime);
            //解码器配置
            mDecoder.configure(mediaFormat, null, null, 0);
        } catch (Exception e) {
            mDecoder = null;
        }

        return mDecoder;
    }

    public static MediaCodec getVideoEncoder(String mime, int width, int height, int framerRate) {
        LogUtil.d(TAG, "视频h264编码器() -- 1111");

        MediaFormat format = MediaFormat.createVideoFormat(mime, height, width);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, framerRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);

//        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 480, 480);
//        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        format.setInteger(MediaFormat.KEY_BIT_RATE, 600 * 1000);
//        format.setInteger(MediaFormat.KEY_FRAME_RATE, 13);
//        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 60);
//        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        //format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        MediaCodec videoEncoder = null;
        try {
            videoEncoder = MediaCodec.createEncoderByType(mime);
//            videoEncoder = MediaCodec.createByCodecName("xxx");
            videoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            if (videoEncoder != null) {
                videoEncoder.stop();
                videoEncoder.release();
                videoEncoder = null;
            }
        }

        return videoEncoder;
    }


    public static MediaCodec getVideoDecoder(Surface surface) {
        MediaFormat mediaFormatVD = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 100, 100);
        //设置帧率
        mediaFormatVD.setInteger(MediaFormat.KEY_FRAME_RATE, 13);
        chooseCodec("");
        try {
            LogUtil.i(TAG, "视频解码器  -- 创建前");
            MediaCodec mediaCodecVD = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
//            String mtkDecodeName = "";
//            MediaCodec mediaCodecVD = MediaCodec.createByCodecName(mtkDecodeName);
            LogUtil.i(TAG, "视频解码器  -- 创建后");

            mediaCodecVD.configure(mediaFormatVD, surface, null, 0);
            LogUtil.i(TAG, "视频解码器  -- start前");
            mediaCodecVD.start();
            LogUtil.i(TAG, "视频解码器  -- start后");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static MediaCodecInfo chooseCodec(String mimeType) {

        int codecNum = MediaCodecList.getCodecCount();

        for (int i = 0; i < codecNum; ++i) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                String[] type = codecInfo.getSupportedTypes();

                for (int j = 0; j < type.length; ++j) {
                    if (type[j].equalsIgnoreCase(mimeType)) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }
}
