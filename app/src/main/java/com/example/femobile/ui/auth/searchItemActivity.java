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
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.SongResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.SongApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class searchItemActivity extends AppCompatActivity implements SongAdapter.OnItemClickListener {

    TextView cancelBtn, tvNoResults;
    EditText etSearch;
    RecyclerView rvSongs;
    SongAdapter adapter;
    ImageView ivClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);

        cancelBtn = findViewById(R.id.tvCancel);
        etSearch = findViewById(R.id.etSearch);
        tvNoResults = findViewById(R.id.tvNoResults);
        rvSongs = findViewById(R.id.rvSongs);
        ivClear = findViewById(R.id.ivClear);

        adapter = new SongAdapter();
        rvSongs.setAdapter(adapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(this);

        cancelBtn.setOnClickListener(v -> {finish();});

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
                    adapter.submitList(new ArrayList<>());
                    tvNoResults.setVisibility(View.GONE); // Ẩn cả thông báo không tìm thấy kết quả
                }
            }
        });
        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
        });

    }
    private void searchSongs(String keyword){
        SongApi songApi = RetrofitClient.getApiService(this);
        songApi.searchSongs(keyword).enqueue(new Callback<SongResponse>() {
            @Override
            public void onResponse(@NonNull Call<SongResponse> call, @NonNull Response<SongResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body().getData();
                    if (songs != null && !songs.isEmpty()) {
                        adapter.submitList(songs);
                        tvNoResults.setVisibility(View.GONE);
                    } else {
                        adapter.submitList(new ArrayList<>());
                        tvNoResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    adapter.submitList(new ArrayList<>());
                    tvNoResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SongResponse> call, @NonNull Throwable t) {
                adapter.submitList(new ArrayList<>());
                tvNoResults.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(Song song) {
        Intent intent = new Intent(this, SongDetailActivity.class);
        intent.putExtra("songId", song.getId());
        startActivity(intent);
    }
}

