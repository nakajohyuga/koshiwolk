package com.example.koshiwolk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class StepDataUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "StepDataUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "StepDataUpdateReceiver triggered");

        // StepDataWorker を使って歩数データの更新処理を15分ごとに実行
        PeriodicWorkRequest stepDataRequest = new PeriodicWorkRequest.Builder(StepDataWorker.class, 15, TimeUnit.MINUTES)
                .build();

        // WorkManager で定期的に実行するようにスケジュール
        WorkManager.getInstance(context).enqueue(stepDataRequest);
    }
}

