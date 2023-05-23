package com.example.tictactoe;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

// getApplicationContext() useful for Services. Check Google if forgotten.

public class BackgroundMusicService extends Service {
    private static MediaPlayer backgroundMusic;
    private static boolean isDisabled;

    static void pauseBgm() {
        if(backgroundMusic != null) backgroundMusic.pause();
        System.out.println("backgroundMusic is paused\n\n\n");
    }

    static void resumeBgm() {
        if(backgroundMusic != null && !backgroundMusic.isPlaying()) backgroundMusic.start();
        System.out.println("backgroundMusic is resumed\n\n\n");
    }

    static boolean isDisabled() {
        return isDisabled;
    }

    static void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    @Override
    public void onCreate() {
        if (backgroundMusic == null) {
            final float volume = (float) (1 - (Math.log(100 - 43) / Math.log(100)));
            backgroundMusic = MediaPlayer.create(this, R.raw.my_theme);
            backgroundMusic.setVolume(volume, volume);
            backgroundMusic.setLooping(true);
            backgroundMusic.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand() in the BgmService is called\n\n\n");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        System.out.println("backgroundMusic is released\n\n\n");
        if(backgroundMusic != null) {
            if(backgroundMusic.isPlaying()) backgroundMusic.stop();
            backgroundMusic.reset();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }
}
