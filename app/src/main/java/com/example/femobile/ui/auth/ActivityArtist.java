package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.femobile.adapter.AlbumAdapter;
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.AlbumResponse;
import com.example.femobile.model.response.SearchResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.AlbumApi;
import com.example.femobile.service.api.SongApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityArtist extends AppCompatActivity {
    private static final String TAG = "ActivityArtist";
    private ImageView headerBackground;
    private TextView tvArtistName;
    private RecyclerView rvAlbums;
    private ImageButton btnBack;
    private AlbumAdapter albumAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        headerBackground = findViewById(R.id.headerBackground);
        tvArtistName = findViewById(R.id.tv_artist_name);
        rvAlbums = findViewById(R.id.rvAlbums2);
        btnBack = findViewById(R.id.btn_back2);

        albumAdapter = new AlbumAdapter();
        rvAlbums.setLayoutManager(new LinearLayoutManager(this));
        rvAlbums.setAdapter(albumAdapter);

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

        if (artistId != null && !artistId.isEmpty()) {
            fetchArtistAlbums(artistId);
        }

        albumAdapter.setOnItemClickListener(album -> {
            Intent albumIntent = new Intent(this, ActivityAlbum.class);
            albumIntent.putExtra("albumId", album.getId());
            albumIntent.putExtra("coverUrl", album.getCoverUrl());
            albumIntent.putExtra("albumTitle", album.getTitle());
            startActivity(albumIntent);
        });
    }

    private void fetchArtistAlbums(String artistId) {
        AlbumApi albumApi = RetrofitClient.getApialbum(this);
        albumApi.getAlbumsByArtist(artistId).enqueue((new Callback<List<Album>>() {
            @Override
            public void onResponse(@NonNull Call<List<Album>> call, @NonNull Response<List<Album>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d(TAG, "onResponse: success, albums fetched");
                    List<Album> albums = response.body();
                    if (albums != null && !albums.isEmpty()) {
                        albumAdapter.submitList(albums);
                    } else {
                        android.util.Log.d(TAG, "onResponse: albums list is empty or null");
                        albumAdapter.submitList(new ArrayList<>());
                    }
                } else {
                    android.util.Log.e(TAG, "onResponse: API call not successful. Code: " + response.code());
                    albumAdapter.submitList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Album>> call, @NonNull Throwable t) {
                android.util.Log.e(TAG, "onFailure: API call failed", t);
                albumAdapter.submitList(new ArrayList<>());
            }
        }));
    }
}
