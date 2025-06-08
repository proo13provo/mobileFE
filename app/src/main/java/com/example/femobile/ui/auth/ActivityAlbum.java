package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.femobile.R;
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.AlbumApi;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAlbum extends AppCompatActivity {
    private static final String TAG = "ActivityAlbum";
    private ImageView artistAvatar, headerBackground;
    private ImageButton btnBack;
    private TextView albumName;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> listSong = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Initialize views
        headerBackground = findViewById(R.id.headerBackground);
        artistAvatar = findViewById(R.id.artistAvatar);
        btnBack = findViewById(R.id.btn_back2);
        albumName = findViewById(R.id.albumName);
        rvSongs = findViewById(R.id.rvSongs);

        // Initialize RecyclerView
        songAdapter = new SongAdapter();
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songAdapter);

        // Set up song click listener
        songAdapter.setOnItemClickListener(song -> {
            Intent intent = new Intent(ActivityAlbum.this, SongDetailActivity.class);
            intent.putExtra("songId", song.getId());
            startActivity(intent);
        });

        // Get album data from intent
        int albumId = getIntent().getIntExtra("albumId", -1);
        String coverUrl = getIntent().getStringExtra("coverUrl");
        String albumTitle = getIntent().getStringExtra("albumTitle");

        // Set album title
        if (albumTitle != null && !albumTitle.isEmpty()) {
            albumName.setText(albumTitle);
        }

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

        // Fetch album data
        if (albumId != -1) {
            fetchAlbumData(String.valueOf(albumId));
        } else {
            Log.e(TAG, "No album ID provided");
            Toast.makeText(this, "Error: No album ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAlbumData(String albumId) {
        Log.d(TAG, "Fetching album data for ID: " + albumId);
        AlbumApi albumApi = RetrofitClient.getApialbum(this);
        albumApi.getAlbum(albumId).enqueue(new Callback<Album>() {
            @Override
            public void onResponse(@NonNull Call<Album> call, @NonNull Response<Album> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Album album = response.body();
                    List<Song> songs = album.getListSong();
                    Log.d(TAG, "Received album: " + album.getTitle() + " with " + 
                          (songs != null ? songs.size() : 0) + " songs");
                    
                    if (songs != null && !songs.isEmpty()) {
                        songAdapter.submitList(songs);
                    } else {
                        Log.w(TAG, "No songs found in album");
                        Toast.makeText(ActivityAlbum.this, "No songs found in this album", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error response: " + response.code());
                    Toast.makeText(ActivityAlbum.this, "Error loading album data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Album> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(ActivityAlbum.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
