package com.example.koshiwolk;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class StepCounterService extends Service implements SensorEventListener {
    private static final String TAG = "StepCounterService";
    private static final String CHANNEL_ID = "StepCounterChannel";

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private int stepCount = 0;
    private long lastTimestamp = 0;
    private final float[] lastValues = new float[3];
    private final float thresholdX = 1.0f;
    private final float thresholdY = 1.2f;
    private final float thresholdZ = 1.5f;

    @SuppressLint("ForegroundServiceType")
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
                    .setContentText("歩数を計測しています")
                    .setSmallIcon(R.drawable.ic_walk)
                    .build();
            startForeground(1, notification);
        }

        // センサーの初期化
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }

        // SharedPreferencesから歩数をロード
        loadStepCount();

        // 次回の更新をスケジュール
        scheduleNextUpdate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "StepCounterService onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "StepCounterService onDestroy");
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        saveStepCount(); // Serviceが終了されるときに歩数を保存
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float deltaX = Math.abs(x - lastValues[0]);
            float deltaY = Math.abs(y - lastValues[1]);
            float deltaZ = Math.abs(z - lastValues[2]);

            lastValues[0] = x;
            lastValues[1] = y;
            lastValues[2] = z;

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTimestamp) > 300) {
                lastTimestamp = currentTime;

                if ((deltaX > thresholdX && deltaY > thresholdY) || (deltaZ > thresholdZ && deltaY > thresholdY)) {
                    stepCount++;
                    saveStepCount(); // 新しいステップがカウントされるたびに保存
                    broadcastStepCount(); // 歩数が更新されたことをブロードキャストで通知
                    Log.d(TAG, "歩数: " + stepCount);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 必要に応じて処理を追加
    }

    // SharedPreferencesに歩数を保存
    private void saveStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalStepCount", stepCount);
        editor.apply();
    }

    // SharedPreferencesから歩数をロード
    private void loadStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("totalStepCount", 0);
    }

    // 歩数が増えるたびに通知を送信
    private void broadcastStepCount() {
        Intent intent = new Intent("com.example.koshiwolk.STEP_COUNT_UPDATED");
        intent.putExtra("stepCount", stepCount);
        sendBroadcast(intent);
    }

    // 次回更新を15分後にスケジュール
    private void scheduleNextUpdate() {
        // 現在時刻を取得
        Calendar calendar = Calendar.getInstance();

        // 次回の15分刻みのタイミングを設定
        int minutes = calendar.get(Calendar.MINUTE);
        int nextMinutes = (minutes / 15 + 1) * 15;
        if (nextMinutes >= 60) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            nextMinutes = 0;
        }
        calendar.set(Calendar.MINUTE, nextMinutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Log.d(TAG, "次回更新時刻: " + calendar.getTime());

        // AlarmManager を使って次回更新をスケジュール
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StepDataUpdateReceiver.class);  // 更新処理を行う受信者を指定
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            // RTC_WAKEUP はデバイスがスリープしていてもアラームを実行
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
