package com.example.koshiwolk;

public class User {
    public String loginId;
    public String email;
    public int loginStreak;
    public int totalLoginDays;
    public long lastLoginDate;
    public int points; // ポイントフィールドを追加

    public User(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;
        this.loginStreak = 1;  // 初回は1日目からスタート
        this.totalLoginDays = 1;  // 初回ログイン日数
        this.lastLoginDate = System.currentTimeMillis();  // 現在時刻を初回ログイン日時に設定
        this.points = 100; // 初回登録時のポイント
    }

    // Getter メソッド
    public String getLoginId() {
        return loginId;
    }

    public String getEmail() {
        return email;
    }

    public int getLoginStreak() {
        return loginStreak;
    }

    public int getTotalLoginDays() {
        return totalLoginDays;
    }

    public long getLastLoginDate() {
        return lastLoginDate;
    }

    public int getPoints() {
        return points;
    }

    // Setter メソッド
    public void setLoginStreak(int loginStreak) {
        this.loginStreak = loginStreak;
    }

    public void setTotalLoginDays(int totalLoginDays) {
        this.totalLoginDays = totalLoginDays;
    }

    public void setLastLoginDate(long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
