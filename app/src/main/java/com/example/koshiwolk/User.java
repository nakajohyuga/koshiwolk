package com.example.koshiwolk;

public class User {
    public String loginId;
    public String email;
    public int loginStreak = 1; // 初回ログインは1日目として設定
    public int totalLoginDays = 0;
    public long lastLoginDate = 0;
    public int points = 100; // 初回ポイント

    public User(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;
    }

    // 必要なgetter/setterのみ残す
    public int getLoginStreak() {
        return loginStreak;
    }

    public void setLoginStreak(int loginStreak) {
        this.loginStreak = loginStreak;
    }

    public int getTotalLoginDays() {
        return totalLoginDays;
    }

    public void setTotalLoginDays(int totalLoginDays) {
        this.totalLoginDays = totalLoginDays;
    }

    public long getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
