package com.test.demoaudio.player.soundpool;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.test.demoaudio.R;
import com.test.demoaudio.utils.LogUtil;

public class SoundPoolUtil {

    private static final String tag = "SoundPoolUtil";

    // SoundPool对象
    private SoundPool mSoundPlayer;
    private int loadIdOne;
    private int loadIdTwo;

    public int playIdOne;
    public int playIdTwo;
    public int playIdThree;

    public SoundPoolUtil(Context context) {

        init();
        // 加载音效
        loadIdOne = mSoundPlayer.load(context, R.raw.sound_one, 1);
        loadIdTwo = mSoundPlayer.load(context, R.raw.sound_two, 1);
    }

    private void init() {
        if (null == mSoundPlayer) {
            mSoundPlayer = new SoundPool(10,
                    AudioManager.STREAM_SYSTEM, 5);
        }
    }

    public void playSoundOne() {
        init();
        //第一个参数soundID
        //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
        //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
        //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
        //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
        //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
        playIdOne = mSoundPlayer.play(loadIdOne,
                1, 1, 0, -1, 1);
        LogUtil.i(tag, "playSoundOne() -- playIdOne = " + playIdOne);

    }

    public void playSoundTwo() {
        init();
        playIdTwo = mSoundPlayer.play(loadIdTwo, 1, 1, 0, -1, 1);
        LogUtil.i(tag, "playSoundForHassium() -- playedIdForHassium = " + playIdTwo);
    }

    public void playSoundThree(Context context) {
        init();
        mSoundPlayer.load(context, R.raw.sound_two, 1);
        mSoundPlayer.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    playIdThree = soundPool.play(sampleId,
                            1, 1, 1, -1, 1);
                }
            }
        });
    }

    public void stopSound(int playId) {
        if (null != mSoundPlayer) {
            mSoundPlayer.stop(playId);
        }
    }

    /**
     * 停止所有音效的播放, 并释放SoundPool资源
     */
    public void stopSound() {
        if (null != mSoundPlayer) {
            mSoundPlayer.stop(playIdOne);
            mSoundPlayer.stop(playIdTwo);
            mSoundPlayer.stop(playIdThree);

            mSoundPlayer.release();
            mSoundPlayer = null;
        }
    }
}