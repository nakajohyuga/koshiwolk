package com.example.koshiwolk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private TextView loginIdTextView, emailTextView, loginStreakTextView, totalLoginDaysTextView, lastLoginDateTextView, pointsTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // TextViewの関連付け
        loginIdTextView = view.findViewById(R.id.loginIdTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        loginStreakTextView = view.findViewById(R.id.loginStreakTextView);
        totalLoginDaysTextView = view.findViewById(R.id.totalLoginDaysTextView);
        pointsTextView = view.findViewById(R.id.pointsTextView);

        // Firebaseの初期化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Firebaseからデータを取得して表示
        loadUserData();

        return view;
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // データの取得
                        String loginId = documentSnapshot.getString("loginId");
                        String email = documentSnapshot.getString("email");
                        int loginStreak = documentSnapshot.getLong("loginStreak").intValue();
                        int totalLoginDays = documentSnapshot.getLong("totalLoginDays").intValue();
                        int points = documentSnapshot.getLong("points").intValue();

                        // TextViewへのデータの反映
                        loginIdTextView.setText("ユーザー ID: " + loginId);
                        emailTextView.setText("メールアドレス: " + email);
                        loginStreakTextView.setText("連続ログイン日数: " + loginStreak + " 日");
                        totalLoginDaysTextView.setText("総ログイン日数: " + totalLoginDays +" 日");
                        pointsTextView.setText("現在のポイント数: " + points + " ポイント");
                    }
                })
                .addOnFailureListener(e -> {
                    // エラーハンドリング
                    loginIdTextView.setText("Failed to load data");
                });
    }
}
