package com.cakes.demomediacodec.mediaCodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.view.Surface;

import com.cakes.utils.LogUtil;

import java.io.IOException;

public class VideoMediaCodec {

    private static final String TAG = "VideoMediaCodec";

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
