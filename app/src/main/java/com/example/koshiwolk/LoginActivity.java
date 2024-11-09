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
                                                // ユーザー情報の取得とnullチェック
                                                Long lastLoginDate = document.getLong("lastLoginDate");
                                                Integer loginStreak = document.getLong("loginStreak") != null ? document.getLong("loginStreak").intValue() : 1;
                                                Integer totalLoginDays = document.getLong("totalLoginDays") != null ? document.getLong("totalLoginDays").intValue() : 0;
                                                Integer points = document.getLong("points") != null ? document.getLong("points").intValue() : 100;

                                                long currentDate = System.currentTimeMillis();
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTimeInMillis(currentDate);
                                                int currentDay = calendar.get(Calendar.DAY_OF_YEAR);

                                                if (lastLoginDate != null && lastLoginDate != 0) {
                                                    Calendar lastLoginCalendar = Calendar.getInstance();
                                                    lastLoginCalendar.setTimeInMillis(lastLoginDate);
                                                    int lastLoginDay = lastLoginCalendar.get(Calendar.DAY_OF_YEAR);

                                                    if (currentDay != lastLoginDay) {
                                                        totalLoginDays++;
                                                    }
                                                } else {
                                                    // 初回ログイン時はtotalLoginDaysを1に設定
                                                    totalLoginDays = 1;
                                                }

                                                final int updatedTotalLoginDays = totalLoginDays;

                                                // Firestoreにユーザー情報を更新
                                                db.collection("users").document(userId)
                                                        .update("totalLoginDays", updatedTotalLoginDays, "lastLoginDate", currentDate)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // メッセージ表示
                                                            showLoginBonus(loginStreak, updatedTotalLoginDays, points);

                                                            // ホーム画面へ遷移
                                                            Intent intent = new Intent(LoginActivity.this, TabActivity.class);
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
