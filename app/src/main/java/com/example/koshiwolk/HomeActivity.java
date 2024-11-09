package com.example.koshiwolk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BarChart barChart;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private boolean isDailyMode = true; // 表示モード

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        barChart = findViewById(R.id.barChart);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupBarChart();
        loadStepData();

        Button toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDailyMode = !isDailyMode;
                loadStepData();
            }
        });
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
                            dataSet.setColor(ContextCompat.getColor(this, R.color.purple_500));
                            BarData barData = new BarData(dataSet);
                            barData.setBarWidth(0.9f);
                            barChart.setData(barData);
                            barChart.invalidate();
                        } else {
                            Log.d("HomeActivity", "データが存在しません");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeActivity", "データの取得に失敗しました", e));
    }}

