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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.ArtistAdapter;
import com.example.femobile.adapter.LibraryItemAdapter;
import com.example.femobile.model.request.ArtistRequest.Artist;
import com.example.femobile.model.response.FollowArtistResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.service.api.ArtistAPI;
import com.example.femobile.ui.auth.ActivityArtist;
import com.example.femobile.ui.auth.viewmodel.LibraryViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class libraryFragment extends Fragment {
    private LibraryItemAdapter adapter;
    private LibraryViewModel libraryViewModel;
    RecyclerView rvArtist;
    private ArtistAdapter artistAdapter;
    ImageView ivClear;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
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
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFollowingArtists();
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
}
