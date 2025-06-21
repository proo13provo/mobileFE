package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.femobile.R;
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.SearchResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.SongApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityArtist extends AppCompatActivity {
    private ImageView headerBackground;
    private TextView tvArtistName;
    private RecyclerView rvSongs;
    private ImageButton btnBack;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        headerBackground = findViewById(R.id.headerBackground);
        tvArtistName = findViewById(R.id.tv_artist_name);
        rvSongs = findViewById(R.id.rvSongs);
        btnBack = findViewById(R.id.btn_back2);

        songAdapter = new SongAdapter();
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songAdapter);

        Intent intent = getIntent();
        String artistId = intent.getStringExtra("artistId");
        String urlAvatar = intent.getStringExtra("UrlAvatar");
        String username = intent.getStringExtra("username");

        if (username != null) {
            tvArtistName.setText(username);
        }
        if (urlAvatar != null && !urlAvatar.isEmpty()) {
            Glide.with(this).load(urlAvatar).into(headerBackground);
        }

        btnBack.setOnClickListener(v -> finish());

        if (username != null && !username.isEmpty()) {
            fetchArtistSongs(username);
        }

        songAdapter.setOnItemClickListener(song -> {
            Intent songDetailIntent = new Intent(this, SongDetailActivity.class);
            songDetailIntent.putExtra("songId", song.getId());
            startActivity(songDetailIntent);
        });
    }

    private void fetchArtistSongs(String artistName) {
        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.searchSongs(artistName).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponse> call, @NonNull Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body().getData();
                    if (songs != null && !songs.isEmpty()) {
                        songAdapter.submitList(songs);
                    } else {
                        songAdapter.submitList(new ArrayList<>());
                    }
                } else {
                    songAdapter.submitList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchResponse> call, @NonNull Throwable t) {
                songAdapter.submitList(new ArrayList<>());
            }
        });
    }
}
