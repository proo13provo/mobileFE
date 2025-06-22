package com.example.femobile.ui.auth.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.ArtistAdapter;
import com.example.femobile.adapter.PlaylistAdapter;
import com.example.femobile.model.request.ArtistRequest.Artist;
import com.example.femobile.model.response.FollowArtistResponse;
import com.example.femobile.model.response.PlayListResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.service.api.ArtistAPI;
import com.example.femobile.service.api.PlayListApi;
import com.example.femobile.ui.auth.ActivityArtist;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class libraryFragment extends Fragment {
    RecyclerView rvArtist;
    private ArtistAdapter artistAdapter;
    RecyclerView rvPlaylists;
    private PlaylistAdapter playlistAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        // Playlist
        rvPlaylists = view.findViewById(R.id.rvAlbums); // Dùng lại rvAlbums cho playlist
        playlistAdapter = new PlaylistAdapter();
        rvPlaylists.setAdapter(playlistAdapter);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        // Artists
        rvArtist = view.findViewById(R.id.rvArtist);
        artistAdapter = new ArtistAdapter();
        rvArtist.setAdapter(artistAdapter);
        rvArtist.setLayoutManager(new LinearLayoutManager(getContext()));
        artistAdapter.setOnItemClickListener(artist -> {
            Intent intent = new Intent(requireContext(), ActivityArtist.class);
            intent.putExtra("artistId", String.valueOf(artist.getId()));
            intent.putExtra("UrlAvatar", artist.getUrlAvatar());
            intent.putExtra("username", artist.getUsername());
            startActivity(intent);
        });
        fetchFollowingArtists();
        fetchUserPlaylists();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFollowingArtists();
        fetchUserPlaylists();
    }

    private void fetchFollowingArtists() {
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getValidAccessToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập hoặc token hết hạn!", Toast.LENGTH_SHORT).show();
            artistAdapter.submitList(new ArrayList<>());
            return;
        }
        token = "Bearer " + token;
        ArtistAPI artistAPI = RetrofitClient.getArtistApi(requireContext());
        artistAPI.getFollowingArtists(token).enqueue(new Callback<List<FollowArtistResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowArtistResponse>> call, @NonNull Response<List<FollowArtistResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artists = new ArrayList<>();
                    for (FollowArtistResponse res : response.body()) {
                        try {
                            int id = Integer.parseInt(res.getArtistId());
                            artists.add(new Artist(id, res.getUsername(), res.getEmail(), res.getUrlAvatar()));
                        } catch (Exception e) {
                            Log.e("libraryFragment", "Lỗi chuyển đổi artistId", e);
                        }
                    }
                    artistAdapter.submitList(artists);
                } else {
                    artistAdapter.submitList(new ArrayList<>());
                    Toast.makeText(getContext(), "Không thể lấy danh sách nghệ sĩ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FollowArtistResponse>> call, @NonNull Throwable t) {
                artistAdapter.submitList(new ArrayList<>());
                Toast.makeText(getContext(), "Lỗi kết nối khi lấy nghệ sĩ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserPlaylists() {
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getValidAccessToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập hoặc token hết hạn!", Toast.LENGTH_SHORT).show();
            playlistAdapter.setPlaylists(new ArrayList<>());
            return;
        }
        token = "Bearer " + token;
        PlayListApi playListApi = RetrofitClient.getplayListApi(requireContext());
        playListApi.getPlayLists(token).enqueue(new Callback<List<PlayListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<PlayListResponse>> call, @NonNull Response<List<PlayListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    playlistAdapter.setPlaylists(response.body());
                } else {
                    playlistAdapter.setPlaylists(new ArrayList<>());
                    Toast.makeText(getContext(), "Không thể lấy danh sách playlist!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PlayListResponse>> call, @NonNull Throwable t) {
                playlistAdapter.setPlaylists(new ArrayList<>());
                Toast.makeText(getContext(), "Lỗi kết nối khi lấy playlist!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
