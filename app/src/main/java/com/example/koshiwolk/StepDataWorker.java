package com.example.koshiwolk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepDataWorker extends Worker {
    private static final String TAG = "StepDataWorker";
    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String PREF_TOTAL_STEPS = "totalStepCount";
    private static final String PREF_LAST_TIMESTAMP = "lastTimestamp";  // 最後に保存した日時を保持
    private static final String PREF_LAST_SAVED_STEPS = "lastSavedSteps"; // 前回保存した総歩数

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

        // SharedPreferencesから保存されている歩数を読み込む
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int totalSteps = sharedPreferences.getInt(PREF_TOTAL_STEPS, 0);
        int lastSavedSteps = sharedPreferences.getInt(PREF_LAST_SAVED_STEPS, 0);  // 前回保存した歩数
        long lastTimestamp = sharedPreferences.getLong(PREF_LAST_TIMESTAMP, 0);

        // 増加分の歩数を計算
        int newSteps = totalSteps - lastSavedSteps;
        if (newSteps <= 0) {
            Log.d(TAG, "No new steps to save.");
            return Result.success();
        }

        // 現在の時間を取得
        long currentTimestamp = System.currentTimeMillis();

        // Firestoreから現在の歩数データを取得して、増加分だけを足す
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .collection("stepsData")
                .document("steps")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Firestoreのデータが存在する場合
                        List<Long> hourlyStepsLong = (List<Long>) documentSnapshot.get("hourlySteps");
                        List<Long> dailyStepsLong = (List<Long>) documentSnapshot.get("dailySteps");

                        // Long型のリストをInteger型に変換
                        List<Integer> hourlySteps = new ArrayList<>();
                        for (Long step : hourlyStepsLong) {
                            hourlySteps.add(Math.toIntExact(step)); // 安全にintに変換
                        }

                        List<Integer> dailySteps = new ArrayList<>();
                        for (Long step : dailyStepsLong) {
                            dailySteps.add(Math.toIntExact(step)); // 安全にintに変換
                        }

                        // 現在の時間と曜日を取得
                        Calendar calendar = Calendar.getInstance();
                        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY); // 0〜23の値
                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1=日曜日, 2=月曜日, ..., 7=土曜日

                        // 増加した歩数を各時間帯と曜日に追加
                        hourlySteps.set(hourOfDay, hourlySteps.get(hourOfDay) + newSteps);
                        dailySteps.set(dayOfWeek - 1, dailySteps.get(dayOfWeek - 1) + newSteps);

                        // Firestoreにデータを保存
                        Map<String, Object> stepData = new HashMap<>();
                        stepData.put("hourlySteps", hourlySteps);
                        stepData.put("dailySteps", dailySteps);
                        stepData.put("timestamp", currentTimestamp);

                        // Firestoreに保存
                        db.collection("users").document(userId)
                                .collection("stepsData")
                                .document("steps")
                                .set(stepData)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data successfully written!"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error writing data", e));

                        // 日付や週が変わった場合の処理
                        if (isNewDayOrWeek(calendar)) {
                            for (int i = 0; i < 24; i++) hourlySteps.set(i, 0);
                            for (int i = 0; i < 7; i++) dailySteps.set(i, 0);
                        }

                    } else {
                        // 初回登録時（データがない場合）
                        initializeStepData(userId, totalSteps, currentTimestamp);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving step data", e));

        // SharedPreferencesに前回の歩数とタイムスタンプを保存
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_LAST_TIMESTAMP, currentTimestamp);
        editor.putInt(PREF_LAST_SAVED_STEPS, totalSteps);  // 前回保存した歩数を更新
        editor.apply();

        return Result.success();
    }

    // 初回のデータ登録（FirestoreにhourlyStepsとdailyStepsを初期化して保存）
    private void initializeStepData(String userId, int totalSteps, long timestamp) {
        List<Integer> hourlySteps = new ArrayList<>(24);
        List<Integer> dailySteps = new ArrayList<>(7);

        for (int i = 0; i < 24; i++) hourlySteps.add(0);
        for (int i = 0; i < 7; i++) dailySteps.add(0);

        Map<String, Object> stepData = new HashMap<>();
        stepData.put("hourlySteps", hourlySteps);
        stepData.put("dailySteps", dailySteps);
        stepData.put("timestamp", timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .collection("stepsData")
                .document("steps")
                .set(stepData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Initial data successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing initial data", e));
    }

    // 日付や週が変わったかを判定するメソッド
    private boolean isNewDayOrWeek(Calendar calendar) {
        int currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int currentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int lastDayOfYear = sharedPreferences.getInt("lastDayOfYear", -1);
        int lastWeekOfYear = sharedPreferences.getInt("lastWeekOfYear", -1);

        boolean isNewDay = currentDayOfYear != lastDayOfYear;
        boolean isNewWeek = currentWeekOfYear != lastWeekOfYear;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isNewDay) editor.putInt("lastDayOfYear", currentDayOfYear);
        if (isNewWeek) editor.putInt("lastWeekOfYear", currentWeekOfYear);
        editor.apply();

        return isNewDay || isNewWeek;
    }
}
