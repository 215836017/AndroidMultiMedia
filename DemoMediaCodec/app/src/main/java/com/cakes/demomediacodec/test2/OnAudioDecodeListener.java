package com.cakes.demomediacodec.test2;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface OnAudioDecodeListener {

    void onAudioDecode(ByteBuffer bb, MediaCodec.BufferInfo bi);
}
