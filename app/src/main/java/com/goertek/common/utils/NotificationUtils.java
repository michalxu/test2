package com.goertek.common.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.goertek.rox2.R;

public class NotificationUtils {
    private static final String TAG = "NotificationUtils";
    public static final String CHANNEL_DEFAULT_ID = "001";
    public static final int NOTIFICATION_ID = 1;

    private static final String CHANNEL_DEFAULT_NAME = "CHANNEL_1";

    /**
     * 创建默认的NotificationChannel
     * 不在桌面显示小红点
     * 在久按桌面图标时不显示此渠道的通知
     *
     * @param context 上下文
     */
    public static void createDefaultNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (notificationManager.getNotificationChannel(CHANNEL_DEFAULT_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_DEFAULT_ID, CHANNEL_DEFAULT_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }
    }

    /**
     * 获取Service前台服务显示通知
     *
     * @param context 上下文
     * @return Notification
     */
    public static Notification getForegroundServiceNotification(Context context) {
        return new NotificationCompat.Builder(context, NotificationUtils.CHANNEL_DEFAULT_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setOngoing(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setNumber(0)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .build();
    }
}
