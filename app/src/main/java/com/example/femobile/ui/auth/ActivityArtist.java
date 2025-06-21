package com.example.femobile.ui.auth;

import android.annotation.SuppressLint;
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
import com.example.femobile.model.response.FollowArtistResponse;
import com.example.femobile.model.response.SearchResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.service.api.AlbumApi;
import com.example.femobile.service.api.ArtistAPI;
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
    private ImageButton imageButton;
    private TextView followButton;
    private AlbumAdapter albumAdapter;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        headerBackground = findViewById(R.id.headerBackground);
        tvArtistName = findViewById(R.id.tv_artist_name);
        rvAlbums = findViewById(R.id.rvAlbums2);
        btnBack = findViewById(R.id.btn_back2);
        imageButton = findViewById(R.id.imageButton);
        followButton = findViewById(R.id.followButton);

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
        imageButton.setOnClickListener(v -> {
            if (artistId != null && !artistId.isEmpty()) {
                toggleFollowStatus(artistId);
            }
        });

        if (artistId != null && !artistId.isEmpty()) {
            fetchArtistAlbums(artistId);
            checkIfFollowing(artistId);
        }

        albumAdapter.setOnItemClickListener(album -> {
            Intent albumIntent = new Intent(this, ActivityAlbum.class);
            albumIntent.putExtra("albumId", album.getId());
            albumIntent.putExtra("coverUrl", album.getCoverUrl());
            albumIntent.putExtra("albumTitle", album.getTitle());
            startActivity(albumIntent);
        });
    }

    private void toggleFollowStatus(String artistId) {
        if (isFollowing) {
            unfollowArtist(artistId);
        } else {
            followArtist(artistId);
        }
    }

    private void checkIfFollowing(String artistId) {
        TokenManager tokenManager = new TokenManager(this);
        String token = "Bearer " + tokenManager.getAccessToken();
        ArtistAPI artistAPI = RetrofitClient.getArtistApi(this);

        artistAPI.getFollowingArtists(token).enqueue(new Callback<List<FollowArtistResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowArtistResponse>> call, @NonNull Response<List<FollowArtistResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (FollowArtistResponse followedArtist : response.body()) {
                        if (followedArtist.getArtistId().equals(artistId)) {
                            isFollowing = true;
                            updateFollowButton(true);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FollowArtistResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to check follow status", t);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            followButton.setText("following");
            imageButton.setBackgroundResource(R.drawable.button_following_outline);
        } else {
            followButton.setText("follow");
            imageButton.setBackgroundResource(R.drawable.button_follow_outline);
        }
    }

    private void followArtist(String artistId) {
        TokenManager tokenManager = new TokenManager(this);
        String token = "Bearer " + tokenManager.getAccessToken();

        ArtistAPI artistAPI = RetrofitClient.getArtistApi(this);
        artistAPI.followArtist(token, artistId).enqueue(new Callback<FollowArtistResponse>() {
            @Override
            public void onResponse(@NonNull Call<FollowArtistResponse> call, @NonNull Response<FollowArtistResponse> response) {
                if (response.isSuccessful()) {
                    isFollowing = true;
                    updateFollowButton(true);
                } else {
                    Log.e(TAG, "Failed to follow artist. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<FollowArtistResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API call to follow artist failed.", t);
            }
        });
    }

    private void unfollowArtist(String artistId) {
        TokenManager tokenManager = new TokenManager(this);
        String token = "Bearer " + tokenManager.getAccessToken();
        ArtistAPI artistAPI = RetrofitClient.getArtistApi(this);
        artistAPI.unfollowArtist(token, artistId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    isFollowing = false;
                    updateFollowButton(false);
                } else {
                    Log.e(TAG, "Failed to unfollow artist. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

            }
        });
    }

    private void fetchArtistAlbums(String artistId) {
        AlbumApi albumApi = RetrofitClient.getApialbum(this);
        albumApi.getAlbumsByArtist(artistId).enqueue((new Callback<List<Album>>() {
            @Override
            public void onResponse(@NonNull Call<List<Album>> call, @NonNull Response<List<Album>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Album> albums = response.body();
                    if (albums != null && !albums.isEmpty()) {
                        albumAdapter.submitList(albums);
                    } else {
                        albumAdapter.submitList(new ArrayList<>());
                    }
                } else {
                    albumAdapter.submitList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Album>> call, @NonNull Throwable t) {
                albumAdapter.submitList(new ArrayList<>());
            }
        }));
    }
}
