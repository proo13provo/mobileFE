package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.femobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SecondActivity extends AppCompatActivity  {
    EditText user;
    EditText passW;
    BottomNavigationView navigationView;
    ViewPager2 mViewpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        navigationView = findViewById(R.id.bottom_nav);
        mViewpage = findViewById(R.id.view_pager);

        setUpViewPager();

        navigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                mViewpage.setCurrentItem(0);
                return true;
            } else if (id == R.id.menu_search) {
                mViewpage.setCurrentItem(1);
                return true;
            } else if (id == R.id.menu_library) {
                mViewpage.setCurrentItem(2);
                return true;
            }
            return false;
        });
    }
    private void setUpViewPager() {
        viewPagerAdapter adapter = new viewPagerAdapter(this);
        mViewpage.setAdapter(adapter);

    }
}
