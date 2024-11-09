package com.example.koshiwolk;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.content.SharedPreferences;

public class StepCounterService extends Service implements SensorEventListener {
    private static final String CHANNEL_ID = "StepCounterChannel";
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private int stepCount = 0; // 総歩数

    private float thresholdX = 0.8f;
    private float thresholdY = 1.0f;
    private float thresholdZ = 1.2f;
    private long lastTimestamp = 0;
    private float[] lastValues = new float[3];

    // ポケットに入れているかどうかを判定するフラグ
    private boolean isInPocket = false;
    // ポケット内判定のためのY軸の閾値
    private final int pocketThresholdY = 5;

    private SharedPreferences sharedPreferences;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // SharedPreferencesの設定
        sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("totalStepCount", 0); // 保存された総歩数を取得

        // Notificationを表示してサービスが実行中であることを示す
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Step Counter Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            @SuppressLint("ForegroundServiceType") Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("歩数計測中")
                    .setContentText("歩数をカウントしています")
                    .setSmallIcon(R.drawable.ic_walk) // アイコンは適宜変更
                    .build();
            startForeground(1, notification);
        }
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Y軸の値でポケットに入っているかどうかを判別
            isInPocket = Math.abs(y) > pocketThresholdY;

            // 持ち方に応じた閾値の調整
            if (isInPocket) { // ポケットに入れている場合
                thresholdX = 1.0f;
                thresholdY = 1.2f;
                thresholdZ = 1.4f;
            } else { // 水平に持っている場合
                thresholdX = 0.8f;
                thresholdY = 1.0f;
                thresholdZ = 1.2f;
            }

            // 加速度の変化量を計算して、微小な振動を無視
            float deltaX = Math.abs(x - lastValues[0]);
            float deltaY = Math.abs(y - lastValues[1]);
            float deltaZ = Math.abs(z - lastValues[2]);
            lastValues[0] = x;
            lastValues[1] = y;
            lastValues[2] = z;

            // タイミングの検出（300msの間隔で検出）
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTimestamp) > 300) {
                lastTimestamp = currentTime;

                // 連続した変化を確認してカウント
                if ((deltaX > thresholdX && deltaY > thresholdY) || (deltaZ > thresholdZ && deltaY > thresholdY)) {
                    stepCount++;

                    // SharedPreferencesに歩数を保存
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("totalStepCount", stepCount);
                    editor.apply();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサー精度の変更に対応する処理
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


