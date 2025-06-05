package com.example.femobile.ui.auth.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.femobile.R;
import com.example.femobile.adapter.SongAdapter;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.ListeningHistoryResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.service.MusicService;
import com.example.femobile.service.api.SongApi;
import com.example.femobile.ui.auth.SongDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private TokenManager tokenManager;
    private MusicService musicService;
    private boolean bound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind to MusicService
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bound) {
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize TokenManager
        tokenManager = new TokenManager(requireContext());
        
        // Initialize RecyclerView
        rvSongs = view.findViewById(R.id.rvSongs1);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize adapter
        songAdapter = new SongAdapter();
        rvSongs.setAdapter(songAdapter);
        
        // Set click listener for songs
        songAdapter.setOnItemClickListener(song -> {
            if (getActivity() != null) {
                if (bound && musicService != null) {
                    // Play the song
                    musicService.playSong(song);
                    
                    // Open song detail activity
                    Intent intent = new Intent(getActivity(), SongDetailActivity.class);
                    intent.putExtra("songId", song.getId());
                    intent.putExtra("currentSong", song);
                    intent.putExtra("isPlaying", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Đang kết nối service...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Load recent listening history
        loadRecentListeningHistory();
        
        return view;
    }

    private void loadRecentListeningHistory() {
        if (!tokenManager.hasValidTokens()) {
            Log.d(TAG, "No valid tokens found");
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch sử nghe nhạc", Toast.LENGTH_SHORT).show();
            return;
        }

        SongApi songApi = RetrofitClient.getApiService(requireContext());
        String token = "Bearer " + tokenManager.getValidAccessToken();
        
        songApi.listeningHistory(token).enqueue(new Callback<ListeningHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<ListeningHistoryResponse> call, @NonNull Response<ListeningHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ListeningHistoryResponse.ListeningHistoryItem> historyItems = response.body().getListeningHistoryItemResponse();
                    Log.d(TAG, "Received " + (historyItems != null ? historyItems.size() : 0) + " history items");
                    
                    if (historyItems != null && !historyItems.isEmpty()) {
                        // Convert to list of songs and take only first 3
                        List<Song> songs = historyItems.stream()
                            .map(ListeningHistoryResponse.ListeningHistoryItem::getSongResponse)
                            .limit(3)
                            .collect(Collectors.toList());
                        Log.d(TAG, "Displaying " + songs.size() + " songs");
                        songAdapter.submitList(songs);
                    } else {
                        Log.d(TAG, "No history items found");
                        songAdapter.submitList(new ArrayList<>());
                    }
                } else if (response.code() == 401) {
                    Log.d(TAG, "Token expired, attempting to refresh");
                    // Token expired, try to refresh
                    tokenManager.refreshToken(new TokenManager.OnTokenRefreshListener() {
                        @Override
                        public void onTokenRefreshSuccess() {
                            // Retry loading history with new token
                            loadRecentListeningHistory();
                        }

                        @Override
                        public void onTokenRefreshFailed(String error) {
                            Log.e(TAG, "Token refresh failed: " + error);
                            Toast.makeText(getContext(), "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Không thể tải lịch sử nghe nhạc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListeningHistoryResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
