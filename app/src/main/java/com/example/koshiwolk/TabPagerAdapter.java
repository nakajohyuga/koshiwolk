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
                return new StepsFragment();
            case 2:
                return new ProfileFragment();
//            case 3:
//                return new SettingsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;  // 4つのタブ
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "ダッシュボード";
            case 1:
                return "歩数";
            case 2:
                return "プロフィール";
//            case 3:
//                return "設定";
            default:
                return null;
        }
    }
}
