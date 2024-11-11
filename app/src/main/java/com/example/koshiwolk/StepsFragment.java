package com.example.koshiwolk;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class StepsFragment extends Fragment {

    private BarChart barChart;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private boolean isDailyMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps, container, false);

        barChart = view.findViewById(R.id.barChart);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupBarChart();
        loadStepData();

        Button toggleButton = view.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(v -> {
            isDailyMode = !isDailyMode;
            loadStepData();
        });

        // 更新ボタンをセットアップ
        Button refreshButton = view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> {
            // StepDataWorkerを一度だけ実行するリクエストを作成
            OneTimeWorkRequest stepDataUpdateRequest = new OneTimeWorkRequest.Builder(StepDataWorker.class).build();

            // WorkManagerでリクエストをキューに追加
            WorkManager.getInstance(requireContext()).enqueue(stepDataUpdateRequest);

            // Firestoreデータを再読み込みしてグラフを更新
            loadStepData();
        });

        return view;
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
    }

    private void loadStepData() {
        String userId = auth.getCurrentUser().getUid();
        List<BarEntry> entries = new ArrayList<>();

        firestore.collection("users").document(userId)
                .collection("stepsData")
                .document("steps")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Long> stepData = (List<Long>) documentSnapshot.get(isDailyMode ? "dailySteps" : "hourlySteps");
                        if (stepData != null && !stepData.isEmpty()) {
                            entries.clear();
                            for (int i = 0; i < stepData.size(); i++) {
                                entries.add(new BarEntry(i, stepData.get(i).intValue()));
                            }

                            BarDataSet dataSet = new BarDataSet(entries, isDailyMode ? "曜日ごとの歩数" : "一時間ごとの歩数");
                            dataSet.setColor(ContextCompat.getColor(getContext(), R.color.purple_500));
                            BarData barData = new BarData(dataSet);
                            barData.setBarWidth(0.9f);
                            barChart.setData(barData);
                            barChart.invalidate();
                        } else {
                            Log.d("StepsFragment", "データが存在しません");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("StepsFragment", "データの取得に失敗しました", e));
    }

}
