package com.test.demoaudio.player.mediaplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.test.demoaudio.utils.LogUtil;

import java.io.IOException;

public class MediaPlayerManager {
    private static final String TAG = "MediaPlayerManager";

    private MediaPlayer mMediaPlayer;
    private OnMusicPlayerListener musicPlayerListener;

    private final int DELAY_PREPARE_FAILED = 10 * 1000;

    private final int MSG_PREPARE_SUCC = 1;
    private final int MSG_PREPARE_FAILED = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_PREPARE_SUCC:
                    mMediaPlayer.start();
                    if (musicPlayerListener != null) {
                        musicPlayerListener.onStart();
                    }
                    break;

                case MSG_PREPARE_FAILED:
                    if (null != mMediaPlayer) {
                        mMediaPlayer.reset();
                    }
                    if (musicPlayerListener != null) {
                        musicPlayerListener.onError(1);
                    }
                    break;
            }
        }
    };

    public MediaPlayerManager(OnMusicPlayerListener listener) {
        mMediaPlayer = new MediaPlayer();
        this.musicPlayerListener = listener;

        setMediaplayerListeners();
    }

    private void setMediaplayerListeners() {
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void play(String musicPath) {

        if (TextUtils.isEmpty(musicPath)) {
            musicPlayerListener.onError(0);
            return;
        }
        prepare(musicPath);
    }

    private void prepare(String url) {
        LogUtil.i(TAG, "prepare() --- start");
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            handler.sendEmptyMessageDelayed(MSG_PREPARE_FAILED, DELAY_PREPARE_FAILED);

        } catch (IOException e1) {
            LogUtil.d(TAG, "set datasource failed");
            e1.printStackTrace();
        }
    }

    public void pausePlay() {
//        if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.pause();
//        }
    }

    public void resumePlay() {
//        if(!mMediaPlayer.isPlaying()){

        mMediaPlayer.start();
//        }
    }

    public void stop() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();

            if (musicPlayerListener != null) {
                musicPlayerListener.onStop();
            }
        }
    }

    public void release() {
        if (null != mMediaPlayer) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (null != handler) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onPrepared");
            handler.removeMessages(MSG_PREPARE_FAILED);
            handler.sendEmptyMessage(MSG_PREPARE_SUCC);
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onError = " + what);
            if (musicPlayerListener != null) {
                musicPlayerListener.onError(what);
            }
            return false;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (musicPlayerListener != null) {
                musicPlayerListener.onComplete();
            }
        }
    };

    public interface OnMusicPlayerListener {

        void onStart();

        void onStop();

        void onComplete();

        void onError(int errorCode);
    }
}
