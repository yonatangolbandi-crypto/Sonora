package com.yonatan.sonora.utils;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

/**
 * מנהל השמעת תצוגה מקדימה של שירים (30-Second Audio Preview Player) באמצעות {@link MediaPlayer}.
 * מאפשר למשתמשים להאזין לדגימת שמע תוך כדי גלישה באפליקציה או מעמוד הפריט.
 *
 * Singleton wrapper around Android MediaPlayer.
 */
public class AudioPreviewPlayer {

    private static AudioPreviewPlayer instance;
    private MediaPlayer mediaPlayer;
    private String currentPlayingUrl = "";
    private String currentTrackTitle = "";
    private PlaybackListener listener;
    private final Handler mainHandler;

    public interface PlaybackListener {
        void onPlaybackStateChanged(boolean isPlaying, String trackTitle);
        void onPlaybackCompleted();
        void onPlaybackError(String error);
    }

    public static synchronized AudioPreviewPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPreviewPlayer();
        }
        return instance;
    }

    private AudioPreviewPlayer() {
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(PlaybackListener listener) {
        this.listener = listener;
    }

    /**
     * הפעלה או עצירה של שיר לפי כתובת תצוגה מקדימה
     */
    public void playOrPause(String previewUrl, String trackTitle) {
        if (previewUrl == null || previewUrl.trim().isEmpty()) {
            if (listener != null) {
                listener.onPlaybackError("אין קובץ שמע זמין לשיר זה");
            }
            return;
        }

        if (mediaPlayer != null && currentPlayingUrl.equals(previewUrl)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                notifyState(false, trackTitle);
            } else {
                mediaPlayer.start();
                notifyState(true, trackTitle);
            }
            return;
        }

        stop();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(previewUrl);
            currentPlayingUrl = previewUrl;
            currentTrackTitle = trackTitle;

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                notifyState(true, trackTitle);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                notifyState(false, trackTitle);
                if (listener != null) {
                    listener.onPlaybackCompleted();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                notifyState(false, trackTitle);
                if (listener != null) {
                    listener.onPlaybackError("שגיאה בטעינת קובץ השמע");
                }
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            notifyState(false, trackTitle);
            if (listener != null) {
                listener.onPlaybackError("שגיאה בניגון: " + e.getMessage());
            }
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public String getCurrentPlayingUrl() {
        return currentPlayingUrl;
    }

    public String getCurrentTrackTitle() {
        return currentTrackTitle;
    }

    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        String oldTitle = currentTrackTitle;
        currentPlayingUrl = "";
        currentTrackTitle = "";
        notifyState(false, oldTitle);
    }

    private void notifyState(boolean isPlaying, String title) {
        if (listener != null) {
            mainHandler.post(() -> listener.onPlaybackStateChanged(isPlaying, title));
        }
    }
}
