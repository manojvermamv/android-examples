package com.anubhav.commonutility;

import static android.content.Context.AUDIO_SERVICE;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import androidx.annotation.RawRes;

public class SoundPoolManager {

    private boolean playing = false;
    private boolean loaded = false;
    private boolean playingCalled = false;
    private float volume;
    private int ringingSoundId;
    private int ringingStreamId;
    private int disconnectSoundId;
    private SoundPool soundPool;

    /**
     * For Singleton Instantiation
     */
    private static final Object LOCK = new Object();
    private static SoundPoolManager mInstance;

    public static SoundPoolManager getInstance(Context context, @RawRes int incomingRes, @RawRes int disconnectRes) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new SoundPoolManager(context, incomingRes, disconnectRes);
            }
        }
        return mInstance;
    }

    private SoundPoolManager(Context context, @RawRes int incomingRes, @RawRes int disconnectRes) {
        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actualVolume / maxVolume;

        // Load the sounds
        int maxStreams = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            loaded = true;
            if (playingCalled) {
                playRinging();
                playingCalled = false;
            }
        });

        ringingSoundId = soundPool.load(context, incomingRes, 1);
        disconnectSoundId = soundPool.load(context, disconnectRes, 1);
    }

    public void playRinging() {
        if (loaded && !playing) {
            ringingStreamId = soundPool.play(ringingSoundId, volume, volume, 1, -1, 1f);
            playing = true;
        } else {
            playingCalled = true;
        }
    }

    public void stopRinging() {
        if (playing) {
            soundPool.stop(ringingStreamId);
            playing = false;
        }
    }

    public void playDisconnect() {
        if (loaded && !playing) {
            soundPool.play(disconnectSoundId, volume, volume, 1, 0, 1f);
            playing = false;
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.unload(ringingSoundId);
            soundPool.unload(disconnectSoundId);
            soundPool.release();
            soundPool = null;
        }
        mInstance = null;
    }

}