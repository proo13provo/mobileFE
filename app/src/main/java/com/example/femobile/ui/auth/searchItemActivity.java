package com.example.femobile.ui.auth;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.AlbumAdapter;
import com.example.femobile.adapter.ArtistAdapter;
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.request.ArtistRequest.Artist;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.SearchResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.SongApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class searchItemActivity extends AppCompatActivity {

    TextView cancelBtn, tvNoResults;
    EditText etSearch;
    RecyclerView rvSongs, rvAlbums, rvArtist;
    SongAdapter songAdapter;
    AlbumAdapter albumAdapter;

    ArtistAdapter artistAdapter;
    ImageView ivClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);

        cancelBtn = findViewById(R.id.tvCancel);
        etSearch = findViewById(R.id.etSearch);
        tvNoResults = findViewById(R.id.tvNoResults);
        rvSongs = findViewById(R.id.rvSongs);
        rvAlbums = findViewById(R.id.rvAlbums);
        rvArtist = findViewById(R.id.rvArtist);
        ivClear = findViewById(R.id.ivClear);

        songAdapter = new SongAdapter();
        albumAdapter = new AlbumAdapter();
        artistAdapter = new ArtistAdapter();

        rvSongs.setAdapter(songAdapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvAlbums.setAdapter(albumAdapter);
        rvAlbums.setLayoutManager(new LinearLayoutManager(this));
        rvArtist.setAdapter(artistAdapter);
        rvArtist.setLayoutManager(new LinearLayoutManager(this));

        songAdapter.setOnItemClickListener(song -> {
            Intent intent = new Intent(this, SongDetailActivity.class);
            intent.putExtra("songId", song.getId());
            startActivity(intent);
        });
        albumAdapter.setOnItemClickListener(album -> {
            Intent intent = new Intent(this, ActivityAlbum.class);
            intent.putExtra("albumId", album.getId());
            intent.putExtra("coverUrl", album.getCoverUrl());
            intent.putExtra("albumTitle", album.getTitle());
            startActivity(intent);
        });
        artistAdapter.setOnItemClickListener(artist -> {
            Intent intent = new Intent(this, ActivityArtist.class);
            intent.putExtra("artistId",artist.getId());
            intent.putExtra("UrlAvatar", artist.getUrlAvatar());
            intent.putExtra("username", artist.getUsername());
            startActivity(intent);
        });

        cancelBtn.setOnClickListener(v -> {
            finish();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    // Hiển thị nút xóa khi có text
                    ivClear.setVisibility(View.VISIBLE);
                    searchSongs(keyword);
                } else {
                    // Ẩn nút xóa và xóa danh sách khi không có text
                    ivClear.setVisibility(View.GONE);
                    songAdapter.submitList(new ArrayList<>());
                    tvNoResults.setVisibility(View.GONE); // Ẩn cả thông báo không tìm thấy kết quả
                }
            }
        });
        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
        });

    }

    private void searchSongs(String keyword) {
        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.searchSongs(keyword).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponse> call, @NonNull Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body().getData();
                    List<Album> albums = response.body().getAlbumsList();
                    List<Artist> artists = response.body().getArtistsList();

                    // Xử lý bài hát
                    if (songs != null && !songs.isEmpty()) {
                        songAdapter.submitList(songs);
                        rvSongs.setVisibility(View.VISIBLE);
                    } else {
                        songAdapter.submitList(new ArrayList<>());
                        rvSongs.setVisibility(View.GONE);
                    }

                    // Xử lý album
                    if (albums != null && !albums.isEmpty()) {
                        albumAdapter.submitList(albums);
                        rvAlbums.setVisibility(View.VISIBLE);
                    } else {
                        albumAdapter.submitList(new ArrayList<>());
                        rvAlbums.setVisibility(View.GONE);
                    }
                    if(artists != null && !artists.isEmpty()){
                        artistAdapter.submitList(artists);
                        rvArtist.setVisibility(View.VISIBLE);
                    }else {
                        artistAdapter.submitList(new ArrayList<>());
                        rvArtist.setVisibility(View.GONE);
                    }

                    // Hiện thông báo nếu không có kết quả
                    if ((songs == null || songs.isEmpty()) && (albums == null || albums.isEmpty())&&(artists == null || artists.isEmpty())) {
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                    }
                } else {
                    songAdapter.submitList(new ArrayList<>());
                    albumAdapter.submitList(new ArrayList<>());
                    artistAdapter.submitList(new ArrayList<>());
                    tvNoResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchResponse> call, @NonNull Throwable t) {
                songAdapter.submitList(new ArrayList<>());
                albumAdapter.submitList(new ArrayList<>());
                artistAdapter.submitList(new ArrayList<>());
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });
    }
}


