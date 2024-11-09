package com.example.koshiwolk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment implements SensorEventListener {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private TextView stepCountTextView;
    private int stepCount = 0;

    private float thresholdX = 1.0f;
    private float thresholdY = 1.2f;
    private float thresholdZ = 1.5f;
    private long lastTimestamp = 0;
    private float[] lastValues = new float[3];
    private boolean isInPocket = false;
    private final int pocketThresholdY = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        stepCountTextView = view.findViewById(R.id.stepCountTextView);

        // SharedPreferencesから歩数を読み込む
        loadStepCount();

        // パーミッション確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.BODY_SENSORS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.BODY_SENSORS}, REQUEST_CODE_PERMISSION);
            }
        }

        // センサーの初期設定
        sensorManager = (SensorManager) getActivity().getSystemService(getContext().SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // FragmentがActivityにアタッチされているか確認
        if (getActivity() != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            sensorManager.unregisterListener(this);
        }

        // Fragmentが非表示になるタイミングで歩数を保存
        saveStepCount();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            isInPocket = Math.abs(y) > pocketThresholdY;

            // 持ち方に応じた閾値の調整
            if (isInPocket) {
                thresholdX = 1.0f;
                thresholdY = 1.2f;
                thresholdZ = 1.4f;
            } else {
                thresholdX = 0.8f;
                thresholdY = 1.0f;
                thresholdZ = 1.2f;
            }

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
                    stepCountTextView.setText(String.valueOf(stepCount));

                    // SharedPreferencesに歩数を保存
                    saveStepCount();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサーの精度が変化した際の処理
    }

    // 歩数をSharedPreferencesに保存
    private void saveStepCount() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StepCounterPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalStepCount", stepCount);
        editor.apply();
    }

    // SharedPreferencesから歩数を読み込む
    private void loadStepCount() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StepCounterPrefs", getContext().MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("totalStepCount", 0);
        stepCountTextView.setText(String.valueOf(stepCount));
    }

    // パーミッションリクエストの結果を処理
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが許可された場合
                Toast.makeText(getContext(), "パーミッションが許可されました", Toast.LENGTH_SHORT).show();
            } else {
                // パーミッションが拒否された場合
                Toast.makeText(getContext(), "パーミッションが拒否されました", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
