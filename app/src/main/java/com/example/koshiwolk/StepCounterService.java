package com.example.koshiwolk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

public class StepCounterService extends Service {
    private static final String TAG = "StepCounterService";
    private static final String CHANNEL_ID = "StepCounterChannel";
    private static final String WORK_TAG = "StepDataWorker";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "StepCounterService onCreate");

        // 通知チャンネルの設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Step Counter Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("歩数計測中")
                    .setContentText("仮のデータをFirestoreに保存しています")
                    .setSmallIcon(R.drawable.ic_walk)
                    .build();
            startForeground(1, notification);
        }

        // WorkManagerで1分ごとに仮データをFirestoreに保存
        PeriodicWorkRequest saveWorkRequest = new PeriodicWorkRequest.Builder(
                StepDataWorker.class, 1, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, saveWorkRequest);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "StepCounterService onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "StepCounterService onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

