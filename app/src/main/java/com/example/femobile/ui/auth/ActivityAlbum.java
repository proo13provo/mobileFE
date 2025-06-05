package com.example.femobile.ui.auth;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.femobile.R;

public class ActivityAlbum extends AppCompatActivity {
    private ImageView artistAvatar,headerBackground;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Initialize views
        headerBackground = findViewById(R.id.headerBackground);
        artistAvatar = findViewById(R.id.artistAvatar);
        btnBack = findViewById(R.id.btn_back2);

        // Get album data from intent
        String albumId = getIntent().getStringExtra("albumId");
        String coverUrl = getIntent().getStringExtra("coverUrl");

        // Load image using Glide
        if (coverUrl != null && !coverUrl.isEmpty()) {
            // Load image for header background
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .centerCrop()
                .into(headerBackground);

            // Load image for artist avatar
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .centerCrop()
                .into(artistAvatar);
        }

        // Set back button click listener
        btnBack.setOnClickListener(v -> finish());
    }
}
