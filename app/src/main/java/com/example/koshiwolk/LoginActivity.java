package com.example.koshiwolk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText loginIdEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginIdEditText = findViewById(R.id.login_id);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = loginIdEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            db.collection("users").document(userId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document.exists()) {
                                                long[] lastLoginDate = {document.getLong("lastLoginDate") != null ? document.getLong("lastLoginDate") : 0L};
                                                int[] loginStreak = {document.getLong("loginStreak") != null ? document.getLong("loginStreak").intValue() : 0};
                                                int[] totalLoginDays = {document.getLong("totalLoginDays") != null ? document.getLong("totalLoginDays").intValue() : 0};
                                                int[] points = {document.getLong("points") != null ? document.getLong("points").intValue() : 100};

                                                long currentDate = System.currentTimeMillis();
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTimeInMillis(currentDate);
                                                int currentDay = calendar.get(Calendar.DAY_OF_YEAR);

                                                boolean isNewDay = lastLoginDate[0] == 0 || !isSameDay(currentDate, lastLoginDate[0]);

                                                if (isNewDay) {
                                                    Calendar lastLoginCalendar = Calendar.getInstance();
                                                    lastLoginCalendar.setTimeInMillis(lastLoginDate[0]);
                                                    int lastLoginDay = lastLoginCalendar.get(Calendar.DAY_OF_YEAR);

                                                    // 連続ログイン判定
                                                    if (currentDay == lastLoginDay + 1) {
                                                        loginStreak[0]++;
                                                    } else {
                                                        loginStreak[0] = 1;
                                                    }

                                                    totalLoginDays[0]++;

                                                    // ポイント加算
                                                    int addedPoints = 1;
                                                    if (loginStreak[0] % 5 == 0) {
                                                        addedPoints = 2;
                                                    }
                                                    if (totalLoginDays[0] % 5 == 0) {
                                                        addedPoints += 5;
                                                    }
                                                    points[0] += addedPoints;

                                                    // Firestoreに更新
                                                    db.collection("users").document(userId)
                                                            .update("loginStreak", loginStreak[0], "totalLoginDays", totalLoginDays[0], "lastLoginDate", currentDate, "points", points[0])
                                                            .addOnSuccessListener(aVoid -> {
                                                                showLoginBonus(loginStreak[0], totalLoginDays[0], points[0]);
                                                                navigateToHomeScreen();
                                                            })
                                                            .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "データの更新に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                } else {
                                                    // 同日の場合はポイント加算なしでホーム画面へ
                                                    navigateToHomeScreen();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "ユーザー情報が見つかりません", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "ユーザー情報の取得に失敗しました", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "ログイン失敗: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 日付比較のためのメソッド
    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timestamp1);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(timestamp2);

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    private void navigateToHomeScreen() {
        Intent intent = new Intent(LoginActivity.this, TabActivity.class);
        startActivity(intent);
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        startService(serviceIntent);
        finish();
    }

    // ログインボーナスの表示
    private void showLoginBonus(int loginStreak, int totalLoginDays, int points) {
        String message = "ログインボーナス！\n";
        message += "連続ログイン日数: " + loginStreak + "日\n";
        message += "総ログイン日数: " + totalLoginDays + "日\n";
        message += "現在のポイント: " + points + "ポイント";

        // ログインボーナスのメッセージをポップアップで表示
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
