package com.example.koshiwolk;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView stepCountTextView;
    private int stepCount = 0; // 歩数を保持する変数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        stepCountTextView = findViewById(R.id.stepCountTextView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Step Detectorセンサーの取得
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            // センサーが存在しない場合の処理
            stepCountTextView.setText("歩数センサーが見つかりません");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            // アクティビティが再開されたときにリスナーを再登録
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepCounterSensor != null) {
            // アクティビティが一時停止されたときにリスナーを解除
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // 歩数が検出されるたびにstepCountを増加
            stepCount++;
            // 現在の歩数を表示
            stepCountTextView.setText("歩数: " + stepCount);

            // デバッグ用ログ
            Log.d("StepCount", "歩数: " + stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサーの精度が変わったときの処理（ここでは特に実装しない）
    }
}
