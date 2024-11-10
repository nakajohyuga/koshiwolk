package com.example.koshiwolk;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    private TextView stepCountTextView;
    private int stepCount = 0;

    private final BroadcastReceiver stepCountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.example.koshiwolk.STEP_COUNT_UPDATED")) {
                int updatedStepCount = intent.getIntExtra("stepCount", 0);
                updateStepCountWithAnimation(updatedStepCount);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        stepCountTextView = view.findViewById(R.id.stepCountTextView);

        // SharedPreferencesから歩数を読み込む
        loadStepCount();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // BroadcastReceiverの登録
        IntentFilter filter = new IntentFilter("com.example.koshiwolk.STEP_COUNT_UPDATED");
        getActivity().registerReceiver(stepCountReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        // BroadcastReceiverの登録解除
        getActivity().unregisterReceiver(stepCountReceiver);

        // Fragmentが非表示になるタイミングで歩数を保存
        saveStepCount();
    }

    // SharedPreferencesに歩数を保存
    private void saveStepCount() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalStepCount", stepCount);
        editor.apply();
    }

    // SharedPreferencesから歩数を読み込む
    private void loadStepCount() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("totalStepCount", 0);
        stepCountTextView.setText(String.valueOf(stepCount));
    }

    // 歩数の更新時にアニメーションを加える
    private void updateStepCountWithAnimation(int updatedStepCount) {
        // 数字がバウンドするアニメーション
        ValueAnimator bounceAnimator = ValueAnimator.ofFloat(1f, 1.5f, 1f); // 拡大縮小するアニメーション
        bounceAnimator.setDuration(500); // 500msで完了
        bounceAnimator.setInterpolator(new BounceInterpolator()); // バウンドインターポレータを使用
        bounceAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            stepCountTextView.setScaleX(animatedValue); // X軸方向の拡大縮小
            stepCountTextView.setScaleY(animatedValue); // Y軸方向の拡大縮小
        });

        // 歩数のテキストをアニメーションで変化
        ValueAnimator textAnimator = ValueAnimator.ofInt(stepCount, updatedStepCount);
        textAnimator.setDuration(500); // 歩数が増える間のアニメーション
        textAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            stepCountTextView.setText(String.valueOf(animatedValue));

            // 歩数に応じて色を変化させる
            if (animatedValue < 1000) {
                stepCountTextView.setTextColor(Color.BLUE);
            } else if (animatedValue < 5000) {
                stepCountTextView.setTextColor(Color.GREEN);
            } else {
                stepCountTextView.setTextColor(Color.RED);
            }
        });

        bounceAnimator.start();
        textAnimator.start();

        // 新しい歩数を反映
        stepCount = updatedStepCount;
    }
}
