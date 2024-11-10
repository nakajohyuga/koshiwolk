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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText loginIdEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button registerButton;
    private Button backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginIdEditText = findViewById(R.id.login_id);
        passwordEditText = findViewById(R.id.password);
        emailEditText = findViewById(R.id.email);
        registerButton = findViewById(R.id.register_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);

        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(v -> {
            String loginId = loginIdEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String email = emailEditText.getText().toString();

            if (loginId.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.getUid() != null) {
                                saveUserToFirestore(user.getUid(), loginId, email);
                                initializeStepData(user.getUid());  // 初期歩数データの保存
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "登録失敗: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveUserToFirestore(String userId, String loginId, String email) {
        User user = new User(loginId, email);
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "ユーザー情報がFirestoreに保存されました", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "ユーザー情報の保存に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 初期の歩数データをFirestoreに保存
    private void initializeStepData(String userId) {
        List<Integer> hourlySteps = new ArrayList<>(24);
        List<Integer> dailySteps = new ArrayList<>(7);

        // 初期化（0で埋める）
        for (int i = 0; i < 24; i++) hourlySteps.add(0);
        for (int i = 0; i < 7; i++) dailySteps.add(0);

        Map<String, Object> stepData = new HashMap<>();
        stepData.put("hourlySteps", hourlySteps);
        stepData.put("dailySteps", dailySteps);
        stepData.put("timestamp", System.currentTimeMillis());  // 現在の時刻を保存

        // Firestoreに初期データを保存
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .collection("stepsData")
                .document("steps")
                .set(stepData)
                .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "Initial step data successfully written!"))
                .addOnFailureListener(e -> Log.e("RegisterActivity", "Error writing initial step data", e));
    }
}
