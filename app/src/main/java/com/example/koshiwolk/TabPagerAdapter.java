package com.example.koshiwolk;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter {

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DashboardFragment();
            case 1:
                return new ProfileFragment();
            case 2:
                return new SettingsFragment();
            case 3:
                return new StepsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;  // 4つのタブ
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "ダッシュボード";
            case 1:
                return "プロフィール";
            case 2:
                return "設定";
            case 3:
                return "歩数";
            default:
                return null;
        }
    }
}
