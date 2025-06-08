package com.example.femobile.ui.auth.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.ListeningHistoryResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.service.api.SongApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<Song>> recentSongs = new MutableLiveData<>();
    private final TokenManager tokenManager;
    private final SongApi songApi;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
        songApi = RetrofitClient.getApiService(application);
    }

    public LiveData<List<Song>> getRecentSongs() {
        return recentSongs;
    }

    public void loadRecentListeningHistory() {
        if (!tokenManager.hasValidTokens()) {
            Log.d(TAG, "No valid tokens found");
            return;
        }

        String token = "Bearer " + tokenManager.getValidAccessToken();
        
        songApi.listeningHistory(token).enqueue(new Callback<ListeningHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<ListeningHistoryResponse> call, @NonNull Response<ListeningHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ListeningHistoryResponse.ListeningHistoryItem> historyItems = response.body().getListeningHistoryItemResponse();
                    Log.d(TAG, "Received " + (historyItems != null ? historyItems.size() : 0) + " history items");
                    
                    if (historyItems != null && !historyItems.isEmpty()) {
                        List<Song> songs = historyItems.stream()
                            .map(ListeningHistoryResponse.ListeningHistoryItem::getSongResponse)
                            .limit(3)
                            .collect(Collectors.toList());
                        recentSongs.postValue(songs);
                    } else {
                        Log.d(TAG, "No history items found");
                        recentSongs.postValue(new ArrayList<>());
                    }
                } else {
                    Log.d(TAG, "Token expired, attempting to refresh");
                    tokenManager.refreshToken(new TokenManager.OnTokenRefreshListener() {
                        @Override
                        public void onTokenRefreshSuccess() {
                            loadRecentListeningHistory();
                        }

                        @Override
                        public void onTokenRefreshFailed(String error) {
                            Log.e(TAG, "Token refresh failed: " + error);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListeningHistoryResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading listening history", t);
            }
        });
    }
} 