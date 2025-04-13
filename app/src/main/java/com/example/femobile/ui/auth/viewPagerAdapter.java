package com.example.femobile.ui.auth;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.femobile.ui.auth.fragment.HomeFragment;
import com.example.femobile.ui.auth.fragment.libraryFragment;
import com.example.femobile.ui.auth.fragment.searchFragment;
import com.example.femobile.ui.auth.fragment.settingFragment;

public class viewPagerAdapter extends FragmentStateAdapter {


    public viewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new HomeFragment();
            case 1:
                return new searchFragment();
            case 2:
                return new libraryFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
