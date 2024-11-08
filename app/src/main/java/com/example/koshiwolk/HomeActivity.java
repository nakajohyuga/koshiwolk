package com.example.koshiwolk;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        barChart = findViewById(R.id.barChart);
        setupBarChart();
        loadStepData();
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
    }

    private void loadStepData() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        // 仮のデータ（48のバーエントリ、30分ごとの歩数データ）
        for (int i = 0; i < 48; i++) {
            float steps = (float) (Math.random() * 500); // 0～500歩のランダムな歩数データ
            entries.add(new BarEntry(i, steps));
        }

        BarDataSet dataSet = new BarDataSet(entries, "歩数");
        dataSet.setColor(ContextCompat.getColor(this, R.color.purple_500));
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // バーの幅を調整

        barChart.setData(barData);
        barChart.invalidate(); // グラフを再描画
    }
}
