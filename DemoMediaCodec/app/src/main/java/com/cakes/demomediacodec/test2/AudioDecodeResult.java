package com.cakes.demomediacodec.test2;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class AudioDecodeResult implements OnAudioDecodeListener {

    private final String TAG = "AudioDecodeResult";

    private MyAudioTrack myAudioTrack;
    private byte[] dataToPlay;

    public AudioDecodeResult(int frequency, int channel, int sampleBate) {
        myAudioTrack = new MyAudioTrack(frequency, channel, sampleBate);
    }

    @Override
    public void onAudioDecode(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        if (null == buffer) {
            return;
        }
        dataToPlay = new byte[bufferInfo.size];
        buffer.get(dataToPlay);
        buffer.clear();

        myAudioTrack.playAudioTrack(dataToPlay);

    }
}
