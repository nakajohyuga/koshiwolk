package com.example.koshiwolk;

public class User {
    private String loginId;
    private String email;

    public User(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;
    }

    // ゲッターとセッター（必要に応じて追加）
    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
