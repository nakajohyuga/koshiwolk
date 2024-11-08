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

    private EditText loginIdEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

                            // Firestoreからユーザーデータを取得
                            db.collection("users").document(userId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document.exists()) {
                                                // ユーザー情報を取得
                                                Long lastLoginDate = document.getLong("lastLoginDate");
                                                Integer loginStreak = document.getLong("loginStreak") != null ? document.getLong("loginStreak").intValue() : 1;
                                                Integer totalLoginDays = document.getLong("totalLoginDays") != null ? document.getLong("totalLoginDays").intValue() : 0;
                                                Integer points = document.getLong("points") != null ? document.getLong("points").intValue() : 100;

                                                // 現在の日付を取得
                                                long currentDate = System.currentTimeMillis();
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTimeInMillis(currentDate);
                                                int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
                                                int currentYear = calendar.get(Calendar.YEAR);

                                                // 連続ログイン日数とポイントの更新
                                                if (lastLoginDate != null && lastLoginDate != 0) {
                                                    Calendar lastLoginCalendar = Calendar.getInstance();
                                                    lastLoginCalendar.setTimeInMillis(lastLoginDate);
                                                    int lastLoginDay = lastLoginCalendar.get(Calendar.DAY_OF_YEAR);
                                                    int lastLoginYear = lastLoginCalendar.get(Calendar.YEAR);

                                                    // 同じ日にログインしている場合は、何もしない
                                                    if (currentYear == lastLoginYear && currentDay == lastLoginDay) {
                                                        // 同じ日なのでログインボーナスを表示
                                                        showLoginBonus(loginStreak, totalLoginDays, points);
                                                        // ホーム画面へ遷移
                                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                        return;
                                                    } else if (currentDay == lastLoginDay + 1) {
                                                        loginStreak++;
                                                    } else if (currentDay != lastLoginDay) {
                                                        loginStreak = 1; // 連続ログイン日数が途切れた
                                                    }
                                                } else {
                                                    loginStreak = 1; // 初回ログインは連続ログイン1日目とする
                                                }

                                                totalLoginDays++;

                                                // ポイント加算（5日ごとにボーナスポイントを加算）
                                                int dailyPoints = 10; // 通常のログインポイント
                                                if (loginStreak % 5 == 0) {
                                                    points += dailyPoints + 20; // 5日ごとに20ポイントボーナスを追加
                                                } else {
                                                    points += dailyPoints;
                                                }

                                                final int updatedLoginStreak = loginStreak;
                                                final int updatedTotalLoginDays = totalLoginDays;
                                                final int updatedPoints = points;

                                                // Firestoreにユーザー情報を更新
                                                db.collection("users").document(userId)
                                                        .update("loginStreak", updatedLoginStreak,
                                                                "totalLoginDays", updatedTotalLoginDays,
                                                                "lastLoginDate", currentDate,
                                                                "points", updatedPoints)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // ログインボーナスを表示
                                                            showLoginBonus(updatedLoginStreak, updatedTotalLoginDays, updatedPoints);

                                                            // ホーム画面へ遷移
                                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "データの更新に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
