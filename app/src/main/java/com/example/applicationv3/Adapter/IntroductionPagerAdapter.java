package com.example.applicationv3.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.applicationv3.Fragments.OnBoarding1Fragment;
import com.example.applicationv3.Fragments.OnBoarding2Fragment;
import com.example.applicationv3.Fragments.OnBoarding3Fragment;
import com.example.applicationv3.Fragments.OnBoarding4Fragment;
import com.example.applicationv3.Fragments.OnBoarding5Fragment;

public class IntroductionPagerAdapter extends FragmentPagerAdapter {
    public IntroductionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 5; // Nombre total de pages
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new OnBoarding1Fragment();
            case 1:
                return new OnBoarding2Fragment();
            case 2:
                return new OnBoarding3Fragment();
            case 3:
                return new OnBoarding4Fragment();
            case 4:
                return new OnBoarding5Fragment();
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }
}
