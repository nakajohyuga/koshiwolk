package com.example.koshiwolk;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.koshiwolk.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText loginIdEditText;
    private EditText passwordEditText;
    private EditText kanjiNameEditText;
    private EditText kanaNameEditText;
    private EditText emailEditText;
    private EditText yubinEditText;
    private EditText jyushoEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginIdEditText = findViewById(R.id.login_id);
        passwordEditText = findViewById(R.id.password);
        kanjiNameEditText = findViewById(R.id.kanji_name);
        kanaNameEditText = findViewById(R.id.kana_name);
        emailEditText = findViewById(R.id.email);
        yubinEditText = findViewById(R.id.yubin);
        jyushoEditText = findViewById(R.id.jyusho);
        registerButton = findViewById(R.id.register_button);

        // 登録ボタンが押されたときの処理
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 入力内容を取得
                String loginId = loginIdEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String kanjiName = kanjiNameEditText.getText().toString();
                String kanaName = kanaNameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String yubin = yubinEditText.getText().toString();
                String jyusho = jyushoEditText.getText().toString();

                // 簡単な入力確認
                if (loginId.isEmpty() || password.isEmpty() || kanjiName.isEmpty() ||
                        kanaName.isEmpty() || email.isEmpty() || yubin.isEmpty() || jyusho.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                } else {
                    // ユーザー登録処理の実装
                    Toast.makeText(RegisterActivity.this, "ユーザー登録が完了しました", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
