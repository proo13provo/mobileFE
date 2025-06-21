package com.example.femobile.ui.auth.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.AlbumResponse;
import com.example.femobile.model.response.ListeningHistoryResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.service.api.AlbumApi;
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
    private final MutableLiveData<List<Album>> albums = new MutableLiveData<>();
    private final TokenManager tokenManager;
    private final SongApi songApi;
    private final AlbumApi albumApi;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
        songApi = RetrofitClient.getApiService(application);
        albumApi = RetrofitClient.getApialbum(application);
    }

    public LiveData<List<Song>> getRecentSongs() {
        return recentSongs;
    }

    public LiveData<List<Album>> getAlbums() {
        return albums;
    }

    public void loadRecentListeningHistory() {
        String token = getValidBearerTokenOrNull();
        if (token == null) {
            return;
        }
        
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
                } else if (response.code() == 401) {
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
                } else {
                    Log.e(TAG, "Error response: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ListeningHistoryResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading listening history", t);
            }
        });
    }

    public void loadAlbum() {
        String token = getValidBearerTokenOrNull();
        if (token == null) {
            return;
        }

        albumApi.getAllAlbums(token).enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(@NonNull Call<AlbumResponse> call, @NonNull Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AlbumResponse albumResponse = response.body();
                    List<Album> albumList = albumResponse.getContent();

                    if (albumList != null && !albumList.isEmpty()) {
                        Log.d(TAG, "Received " + albumList.size() + " albums");
                        albums.postValue(albumList);
                    } else {
                        Log.d(TAG, "No albums found in response");
                        albums.postValue(new ArrayList<>());
                    }

                } else if (response.code() == 401) {
                    Log.d(TAG, "Token expired, attempting to refresh");
                    tokenManager.refreshToken(new TokenManager.OnTokenRefreshListener() {
                        @Override
                        public void onTokenRefreshSuccess() {
                            loadAlbum();
                        }

                        @Override
                        public void onTokenRefreshFailed(String error) {
                            Log.e(TAG, "Token refresh failed: " + error);
                        }
                    });
                } else {
                    Log.e(TAG, "Error response: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AlbumResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading albums", t);
            }
        });
    }
    
    private String getValidBearerTokenOrNull() {
        if (!tokenManager.hasValidTokens()) {
            Log.d(TAG, "No valid tokens found");
            return null;
        }
        String accessToken = tokenManager.getValidAccessToken();
        if (accessToken == null) {
            Log.d(TAG, "No valid access token");
            return null;
        }
        return "Bearer " + accessToken;
    }
} 