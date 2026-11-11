package com.anubhav.commonutility;

import static android.content.Context.AUDIO_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class RingtonePlayManager {

    private final Context context;
    private final AudioManager audioManager;
    private Vibrator vibrator;
    private Ringtone ringtone;

    private final int DELAY = 0, VIBRATE = 1000, SLEEP = 1000, START = 0;
    private final long[] vibratePattern = {DELAY, VIBRATE, SLEEP};

    /**
     * For Singleton Instantiation
     */
    private static final Object LOCK = new Object();
    private static RingtonePlayManager mInstance;

    public static RingtonePlayManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new RingtonePlayManager(context);
            }
        }
        return mInstance;
    }

    private RingtonePlayManager(Context ctx) {
        this.context = ctx;
        this.audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_RING);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        float volume = actualVolume / maxVolume;
    }

    @SuppressLint({"MissingPermission", "WrongConstant"})
    public void playRinging() {
        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect vibe = VibrationEffect.createWaveform(vibratePattern, START);
            vibrator.vibrate(vibe);
        } else {
            vibrator.vibrate(vibratePattern, START);
        }

        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }

        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
        ringtone.play();
    }

    @SuppressLint("MissingPermission")
    public void stopRinging() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }

        if (ringtone != null) ringtone.stop();
    }

    public boolean silent() {
        return audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }

    public void silent(boolean enable) {
        int volume = (enable ? 0 : audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        audioManager.setRingerMode(enable ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
    }

}