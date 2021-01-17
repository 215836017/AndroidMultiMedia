package com.test.demoaudio.player.soundpool;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.test.demoaudio.R;
import com.test.demoaudio.utils.LogUtil;

public class SoundPoolUtils {

    private static final String tag = "SoundPoolUtils";

    private int playedIdForRingSynthShree = -1;
    private int playedIdForHassium = -1;
    private int playedIdForUmbriel = -1;

    // SoundPool对象
    private SoundPool mSoundPlayer;
    private int loadRingSynthShree;
    private int loadHassium;
    private int loadUmbriel;

    public SoundPoolUtils(Context context) {

        init();
        // 加载音效
        loadRingSynthShree = mSoundPlayer.load(context, R.raw.ring_synth_three, 1);
        loadHassium = mSoundPlayer.load(context, R.raw.hassium, 1);
        loadUmbriel = mSoundPlayer.load(context, R.raw.umbriel, 1);
    }

    private void init() {
        if (null == mSoundPlayer) {
            mSoundPlayer = new SoundPool(10,
                    AudioManager.STREAM_SYSTEM, 5);
        }
    }

    /**
     * 播放ring_synth_three音效
     */
    public int playSoundForRingSynthShree() {
        init();
        playedIdForRingSynthShree = mSoundPlayer.play(loadRingSynthShree, 1, 1, 0, -1, 1);
        LogUtil.i(tag, "playSoundForRingSynthShree() -- playedIdForRingSynthShree = " + playedIdForRingSynthShree);
        return playedIdForRingSynthShree;
    }

    /**
     * 停止播放ring_synth_three音效
     */
    public void stopSoundForRingSynthShree() {
        mSoundPlayer.stop(playedIdForRingSynthShree);
    }

    /**
     * 播放hassium音效
     */
    public int playSoundForHassium() {
        init();
        playedIdForHassium = mSoundPlayer.play(loadHassium, 1, 1, 0, -1, 1);
        LogUtil.i(tag, "playSoundForHassium() -- playedIdForHassium = " + playedIdForHassium);
        return playedIdForHassium;
    }

    /**
     * 停止播放hassium音效
     */
    public void stopSoundForHassium() {
        mSoundPlayer.stop(playedIdForHassium);
    }

    /**
     * 播放umbriel音效
     */
    public int playSoundForUmbriel() {
        init();
        playedIdForUmbriel = mSoundPlayer.play(loadUmbriel, 1, 1, 0, -1, 1);
        LogUtil.i(tag, "playSoundForUmbriel() -- playedIdForUmbriel = " + playedIdForUmbriel);

        return playedIdForUmbriel;
    }

    public void playSound(int playId) {
        mSoundPlayer.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    //第一个参数soundID
                    //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
                    //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
                    //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
                    //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
                    //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
                    soundPool.play(sampleId, 1, 1, 1, 0, 1);
                }
            }
        });
        mSoundPlayer.play(loadUmbriel, 1, 1, 0, -1, 1);
    }

    /**
     * 停止播放umbriel音效
     */
    public void stopSoundForUmbriel() {
        mSoundPlayer.stop(playedIdForUmbriel);
    }

    /**
     * 停止当前音效的播放, 并释放SoundPool资源
     */
    public void stopSound() {
        if (null != mSoundPlayer) {
            mSoundPlayer.stop(playedIdForRingSynthShree);
            mSoundPlayer.stop(playedIdForHassium);
            mSoundPlayer.stop(playedIdForUmbriel);

            mSoundPlayer.release();
            mSoundPlayer = null;
        }
    }
}