package com.cakes.demomediacodec.test4;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cakes.demomediacodec.R;
import com.cakes.utils.LogUtil;

import java.io.IOException;

public class VideoDecodeActivity extends AppCompatActivity {

    private final String TAG = "VideoDecodeActivity";

    private SurfaceView surfaceView;
    private Surface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_decode);

        surfaceView = findViewById(R.id.video_decode_surface);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surface = holder.getSurface();
                LogUtil.w(TAG, "surfaceCreated() --- 1111111111");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        findViewById(R.id.video_decode_btn_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAudioEncoder();
                prepareVideoEncoder();
                test();
            }
        });
    }

    private MediaCodec mediaCodecVD;
    private MediaFormat mediaFormatVD;

    private final int videoWidth = 480;
    private final int videoHeight = 480;

    private String mtkDecodeName = "OMX.MTK.VIDEO.DECODER.AVC";

    private void test() {

        mediaFormatVD = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
        //设置帧率
        mediaFormatVD.setInteger(MediaFormat.KEY_FRAME_RATE, 13);
        chooseCodec("");
        try {
            LogUtil.i(TAG, "视频解码器  -- 创建前");
//            mediaCodecVD = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodecVD = MediaCodec.createByCodecName(mtkDecodeName);
            LogUtil.i(TAG, "视频解码器  -- 创建后");

            mediaCodecVD.configure(mediaFormatVD, surface, null, 0);
            LogUtil.i(TAG, "视频解码器  -- start前");
            mediaCodecVD.start();
            LogUtil.i(TAG, "视频解码器  -- start后");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private MediaCodecInfo chooseCodec(String mimeType) {

        LogUtil.i(TAG, "chooseCodec() -- 1111111");
        int codecNum = MediaCodecList.getCodecCount();
        LogUtil.i(TAG, "chooseCodec() -- 2222222222");

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


    private MediaCodec audioEncoder;
    private MediaFormat audioEncoderFormat;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private final String MIME_AUDIO = MediaFormat.MIMETYPE_AUDIO_AAC;
    private final int SAMPLE_RATE = 16 * 1000;
    private final int DEFAULT_CHANNEL_COUNT = 1;

    private void prepareAudioEncoder() {
        LogUtil.w(TAG, " 音频编码器 -- 1111");
        try {
            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MIME_AUDIO, SAMPLE_RATE, DEFAULT_CHANNEL_COUNT);
            //  MediaFormat format = MediaFormat.createAudioFormat(configuration.mime, configuration.frequency, configuration.channelCount);
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 32000);
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
            int maxInputSize = 19200;
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);

            LogUtil.w(TAG, " 音频编码器 -- 创建前");
            audioEncoder = MediaCodec.createEncoderByType(MIME_AUDIO);
            LogUtil.w(TAG, " 音频编码器 -- 创建后");
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mediaCodec.setCallback(new MyMediaCodecCallback());
//            }
            audioEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            LogUtil.w(TAG, " 音频编码器 -- start前");
            audioEncoder.start();
            LogUtil.w(TAG, " 音频编码器 -- start后");
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogUtil.w(TAG, "音频编码器  -- 结束");
    }

    private void prepareVideoEncoder() {
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
            videoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            LogUtil.d(TAG, "视频h264编码器() -- start前");
            videoEncoder.start();
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
    }
}

