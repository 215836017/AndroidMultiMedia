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

        MediaCodec mediaCodec = null;
        try {
            mediaCodec = MediaCodec.createEncoderByType(mime);

            // TODO: 使用异步方式的话，必须在configure()方法之前设置回调。
            if (null != mCallback) {
                if (Build.VERSION.SDK_INT >= 23 && null != handler) {
                    LogUtil.d(TAG, "设置MediaCodec的异步方式：包含Handler");
                    mediaCodec.setCallback(mCallback, handler);
                } else if (Build.VERSION.SDK_INT >= 21) {
                    LogUtil.d(TAG, "设置MediaCodec的异步方式：不 不 不包含Handler");
                    mediaCodec.setCallback(mCallback);
                }
            }
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            LogUtil.e(TAG, "get audio encoder mediaCodec is error: " + e.getMessage());
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        }
        return mediaCodec;
    }

    private static int getRecordBufferSize(int frequency, int channelCount, int audioEncoding) {
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        if (channelCount == 2) {
            channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
        int size = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        return size;
    }

    public static MediaCodec getAudioDecoder() {
        return null;
    }

    public static MediaCodec getVideoEncoder(Surface surface) {
        LogUtil.d(TAG, "视频h264编码器() -- 1111");
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 480, 480);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 600 * 1000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 13);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 60);
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        //format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        MediaCodec mediaCodec = null;
        try {
            LogUtil.d(TAG, "视频h264编码器() -- 创建前");
//            mediaCodec = MediaCodec.createEncoderByType(videoConfiguration.mime);
            MediaCodec videoEncoder = MediaCodec.createByCodecName("OMX.MTK.VIDEO.ENCODER.AVC");
            LogUtil.d(TAG, "视频h264编码器() -- 创建后");
            videoEncoder.configure(format, surface, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            LogUtil.d(TAG, "视频h264编码器() -- start前");
            //videoEncoder.start();
            LogUtil.d(TAG, "视频h264编码器() -- start后");
        } catch (Exception e) {
            e.printStackTrace();
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        }

        LogUtil.d(TAG, "视频h264编码器() -- 结束");
        return null;
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
        LogUtil.i(TAG, "chooseCodec() -- 3333333333");
        return null;
    }
}
