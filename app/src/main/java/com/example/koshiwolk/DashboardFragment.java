package com.example.koshiwolk;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.TextView;

public class DashboardFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private TextView stepCountTextView;
    private int stepCount = 0;

    // 持ち方による閾値の初期設定
    private float thresholdX = 1.0f;  // X軸の閾値
    private float thresholdY = 1.2f;  // Y軸の閾値
    private float thresholdZ = 1.5f;  // Z軸の閾値

    private long lastTimestamp = 0;
    private float[] lastValues = new float[3]; // X, Y, Z軸の前回の加速度値

    // ポケットに入れた状態の判別に使用
    private boolean isInPocket = false;
    private final int pocketThresholdY = 5; // ポケット内かどうかを判定するY軸の閾値

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        stepCountTextView = view.findViewById(R.id.stepCountTextView);

        sensorManager = (SensorManager) getActivity().getSystemService(getContext().SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
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
                thresholdX = 0.5f; // より敏感にするために閾値を下げる
                thresholdY = 0.6f;
                thresholdZ = 0.8f;
            }

            // 加速度の変化量を計算して、微小な振動を無視
            float deltaX = Math.abs(x - lastValues[0]);
            float deltaY = Math.abs(y - lastValues[1]);
            float deltaZ = Math.abs(z - lastValues[2]);
            lastValues[0] = x;
            lastValues[1] = y;
            lastValues[2] = z;

            // タイミングの検出（200msの間隔で検出）
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTimestamp) > 300) {
                lastTimestamp = currentTime;

                // 連続した変化を確認してカウント
                if ((deltaX > thresholdX && deltaY > thresholdY) || (deltaZ > thresholdZ && deltaY > thresholdY)) {
                    stepCount++;
                    stepCountTextView.setText(String.valueOf(stepCount));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサーの精度が変化した際の処理（特に必要な場合以外は無処理）
    }
}
