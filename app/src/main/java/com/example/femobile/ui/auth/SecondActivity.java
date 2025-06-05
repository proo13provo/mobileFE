package com.example.femobile.ui.auth;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.femobile.R;
import com.example.femobile.adapter.viewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.service.MusicService;
import com.bumptech.glide.Glide;
import com.example.femobile.ui.auth.fragment.PremiumFragment;

public class SecondActivity extends AppCompatActivity {
    private ConstraintLayout miniPlayer;
    private ImageView songThumbnail;
    private TextView songTitle;
    private TextView songArtist;
    private ImageView btnPlay;
    private MusicService musicService;
    private boolean bound = false;
    BottomNavigationView navigationView;
    ViewPager2 mViewpage;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
            updateMiniPlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        navigationView = findViewById(R.id.bottom_nav);
        mViewpage = findViewById(R.id.view_pager);

        // Initialize mini player views
        miniPlayer = findViewById(R.id.mini_player);
        songThumbnail = findViewById(R.id.song_thumbnail);
        songTitle = findViewById(R.id.song_title);
        songArtist = findViewById(R.id.song_artist);
        btnPlay = findViewById(R.id.btn_play);

        // Set up mini player click listener
        miniPlayer.setOnClickListener(v -> {
            if (bound && musicService.getCurrentSong() != null) {
                Intent intent = new Intent(SecondActivity.this, SongDetailActivity.class);
                intent.putExtra("songId", musicService.getCurrentSong().getId());
                intent.putExtra("currentSong", musicService.getCurrentSong());
                intent.putExtra("isPlaying", musicService.isPlaying());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
            }
        });

        // Set up play button click listener
        btnPlay.setOnClickListener(v -> togglePlayPause());

        // Bind to MusicService
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

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
            } else if (id == R.id.menu_premium) {
                mViewpage.setCurrentItem(3);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bound) {
            updateMiniPlayer();
        }
    }

    private void setUpViewPager() {
        viewPagerAdapter adapter = new viewPagerAdapter(this);
        mViewpage.setAdapter(adapter);
    }

    private void updateMiniPlayer() {
        if (bound && musicService.getCurrentSong() != null) {
            Song currentSong = musicService.getCurrentSong();
            miniPlayer.setVisibility(View.VISIBLE);
            songTitle.setText(currentSong.getTitle());
            songArtist.setText(currentSong.getSinger());
            
            if (currentSong.getImageUrl() != null && !currentSong.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(currentSong.getImageUrl())
                    .placeholder(R.drawable.album_art_placeholder)
                    .error(R.drawable.album_art_placeholder)
                    .centerCrop()
                    .into(songThumbnail);
            }

            updatePlayPauseButton();
        } else {
            miniPlayer.setVisibility(View.GONE);
        }
    }

    private void togglePlayPause() {
        if (bound) {
            if (musicService.isPlaying()) {
                musicService.pausePlayback();
            } else {
                musicService.resumePlayback();
            }
            updatePlayPauseButton();
        }
    }

    private void updatePlayPauseButton() {
        if (bound) {
            btnPlay.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }
}
