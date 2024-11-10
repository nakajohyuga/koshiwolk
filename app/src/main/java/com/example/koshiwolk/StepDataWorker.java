package com.example.koshiwolk;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepDataWorker extends Worker {
    private static final String TAG = "StepDataWorker";

    public StepDataWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Saving step data");

        // FirebaseAuthを使って現在のユーザーIDを取得
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Log.e(TAG, "No authenticated user found.");
            return Result.failure();
        }

        // 仮の歩数データを生成
        List<Integer> hourlySteps = new ArrayList<>(24);
        List<Integer> dailySteps = new ArrayList<>(7);

        // 初期化（すべて0で埋める）
        for (int i = 0; i < 24; i++) hourlySteps.add(0);
        for (int i = 0; i < 7; i++) dailySteps.add(0);

        // 新しい歩数データを追加（ここでは仮に10歩追加）
        int hourOfDay = 12;  // 仮の時間
        int dayOfWeek = 2;   // 仮の曜日（例: 月曜日）

        hourlySteps.set(hourOfDay, 10);  // 時間ごとの歩数を追加
        dailySteps.set(dayOfWeek - 1, dailySteps.get(dayOfWeek - 1) + 10);  // 曜日ごとの歩数を追加

        // 新規データを保存
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("hourlySteps", hourlySteps);
        stepData.put("dailySteps", dailySteps);
        stepData.put("timestamp", System.currentTimeMillis());

        // Firestoreに保存
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId) // FirestoreにあるユーザーIDに置き換え
                .collection("stepsData")
                .document("steps")
                .set(stepData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing data", e));

        return Result.success();
    }
}
