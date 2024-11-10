package com.example.koshiwolk;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String loginId;
    public String email;
    public int loginStreak = 1; // 初回ログインは1日目として設定
    public int totalLoginDays = 0;
    public long lastLoginDate = 0;
    public int points = 100; // 初回ポイント

    // 一時間ごとの歩数データ（24時間分）
    private List<Integer> hourlySteps;

    // 曜日ごとの歩数データ（7日分）
    private List<Integer> dailySteps;

    public User(String loginId, String email) {
        this.loginId = loginId;
        this.email = email;

//        // 一時間ごとのデータ初期化
//        hourlySteps = new ArrayList<>();
//        for (int i = 0; i < 24; i++) {
//            hourlySteps.add(0);
//        }
//
//        // 曜日ごとのデータ初期化
//        dailySteps = new ArrayList<>();
//        for (int i = 0; i < 7; i++) {
//            dailySteps.add(0);
//        }
    }

    // ゲッターとセッター
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

//    // 一時間ごとの歩数データの取得と設定
//    public List<Integer> getHourlySteps() {
//        return hourlySteps;
//    }
//
//    public void setHourlySteps(int hour, int steps) {
//        if (hour >= 0 && hour < 24) {
//            hourlySteps.set(hour, steps);
//        }
//    }
//
//    // 曜日ごとの歩数データの取得と設定
//    public List<Integer> getDailySteps() {
//        return dailySteps;
//    }
//
//    public void setDailySteps(int day, int steps) {
//        if (day >= 0 && day < 7) {
//            dailySteps.set(day, steps);
//        }
//    }
}
