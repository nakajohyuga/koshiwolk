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
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText loginIdEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button registerButton;
    private Button backToLoginButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        // 戻るボタンの処理
        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // 登録ボタンの処理
        registerButton.setOnClickListener(v -> {
            String loginId = loginIdEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String email = emailEditText.getText().toString();

            if (loginId.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authenticationでユーザー登録
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // ユーザー登録成功
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserToFirestore(user.getUid(), loginId, email);  // Firestoreにユーザー情報を保存
                        } else {
                            // 登録失敗
                            Toast.makeText(RegisterActivity.this, "登録失敗: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveUserToFirestore(String userId, String loginId, String email) {
        // ユーザー情報をFirestoreに保存するためのデータを設定
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("loginId", loginId);
        userMap.put("email", email);
        userMap.put("loginStreak", 0); // 連続ログイン日数の初期値
        userMap.put("totalLoginDays", 0); // 総ログイン日数の初期値
        userMap.put("lastLoginDate", System.currentTimeMillis()); // 最後のログイン日

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "ユーザー情報がFirestoreに保存されました", Toast.LENGTH_SHORT).show();
                    finish();  // 登録が成功したらこの画面を終了
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "ユーザー情報の保存に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
