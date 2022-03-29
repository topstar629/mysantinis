package com.app.mysantinis.adapter;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.app.mysantinis.fragment.FinishedOrdersFragment;
import com.app.mysantinis.fragment.IncomingOrdersFragment;

/**
 * Created by Mobile World on 12/5/2019.
 */

public class MainPagerAdapter extends FragmentStatePagerAdapter {
    public MainPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new IncomingOrdersFragment();
            case 1:
                return new FinishedOrdersFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Today Orders";
            case 1:
                return "Finished Orders";
        }
        return "";
    }
}
