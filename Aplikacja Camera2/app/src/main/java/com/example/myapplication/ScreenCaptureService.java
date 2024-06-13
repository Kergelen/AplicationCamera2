package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class ScreenCaptureService extends Service {
    private static final String CHANNEL_ID = "ScreenCaptureService";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification.Builder(this, CHANNEL_ID).build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Capture Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(serviceChannel);
        }
    }
}