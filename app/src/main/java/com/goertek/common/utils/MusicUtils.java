package com.goertek.common.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;

import java.io.IOException;

/**
 * 文件名：MusicUtils
 * 描述：
 * 创建时间：2019/9/9
 * @author jochen.zhang
 */
public class MusicUtils {
    private static final String TAG = "MusicUtils";
    private static MediaPlayer mediaPlayer;
    private static boolean isPause = false;

    public static boolean play(String music) {
        if (mediaPlayer != null) {
            stop();
        }
        mediaPlayer = getMediaPlayer(music, false);
        if (mediaPlayer == null) {
            return false;
        }
        mediaPlayer.start();
        return true;
    }

    public static boolean play(String music, MediaPlayer.OnCompletionListener listener) {
        if (mediaPlayer != null) {
            stop();
        }
        mediaPlayer = getMediaPlayer(music, false);
        if (mediaPlayer == null) {
            return false;
        }
        mediaPlayer.setOnCompletionListener(listener);
        mediaPlayer.start();
        return true;
    }

    public static boolean play(String music, Boolean onlyLeft) {
        if (mediaPlayer != null) {
            stop();
        }
        mediaPlayer = getMediaPlayer(music, true);
        if (mediaPlayer == null) {
            return false;
        }
        if (onlyLeft != null) {
            if (onlyLeft) {
                mediaPlayer.setVolume(1, 0);
            } else {
                mediaPlayer.setVolume(0, 1);
            }
        }
        mediaPlayer.start();
        return true;
    }

    public static void stop() {
        if (mediaPlayer != null) {
            isPause = false;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /** 暂停播放 */
    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            isPause = true;
            mediaPlayer.pause();
        }
    }

    /** 继续播放 */
    public static void resume() {
        if (mediaPlayer != null && isPause) {
            mediaPlayer.start();
            isPause = false;
        }
    }

    private static MediaPlayer getMediaPlayer(String music, boolean isLooping) {
        MediaPlayer player = null;
        try {
            AssetFileDescriptor fd = Utils.getContext().getAssets().openFd(music);
            player = new MediaPlayer();
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            player.setLooping(isLooping);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return player;
    }

    private static AudioManager audioManager;
    public static AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) Utils.getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    /**
     * 音乐是否在播放
     *
     * @return true 正在播放
     */
    public static boolean isMusicActive() {
        return getAudioManager().isMusicActive();
    }

    /**
     * 设置50%音量
     */
    public static void halfVolume() {
        try {
            getAudioManager().setStreamVolume(AudioManager.STREAM_NOTIFICATION, getAudioManager().getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 2, 0);
            getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
            getAudioManager().setStreamVolume(AudioManager.STREAM_ALARM, getAudioManager().getStreamMaxVolume(AudioManager.STREAM_ALARM) / 2, 0);
        } catch (SecurityException e) {
            LogUtils_goertek.e(TAG, "SecurityException " + e);
        }
    }

    /**
     * 权限检测
     *
     * @param context 上下文
     * @return 权限检验记过
     */
    public static boolean checkPermission(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return false;
        }
        return true;
    }
}
