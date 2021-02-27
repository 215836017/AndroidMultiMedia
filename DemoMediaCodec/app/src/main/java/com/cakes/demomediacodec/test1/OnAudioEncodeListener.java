package com.cakes.demomediacodec.test1;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface OnAudioEncodeListener {

    void onAudioEncode(ByteBuffer bb, MediaCodec.BufferInfo bi);

    void onAudioFormatChanged(MediaFormat format);

    void onAudioPCM(byte data[], int len);
}
