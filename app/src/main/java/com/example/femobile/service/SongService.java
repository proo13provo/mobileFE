package com.example.femobile.service;

import android.content.Context;

import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.SongResponse;
import com.example.femobile.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.ArrayList;

public class SongService {
    private final SongApi songApi;

    public SongService(Context context) {
        songApi = RetrofitClient.getApiService(context);
    }

    public interface SearchCallback {
        void onSuccess(List<Song> songs);
        void onError(String errorMessage);
    }

    public interface GetSongCallback {
        void onSuccess(Song song);
        void onError(String errorMessage);
    }

    public void searchSongs(String keyword, SearchCallback callback) {
        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Vui lòng nhập từ khóa tìm kiếm");
            return;
        }

        Call<SongResponse> call = songApi.searchSongs(keyword.trim());
        call.enqueue(new Callback<SongResponse>() {
            @Override
            public void onResponse(Call<SongResponse> call, Response<SongResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SongResponse songResponse = response.body();
                    if (songResponse.isSuccess()) {
                        List<Song> songs = songResponse.getData();
                        if (songs != null) {
                            callback.onSuccess(songs);
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    } else {
                        callback.onError(songResponse.getMessage() != null ? 
                            songResponse.getMessage() : "Không tìm thấy kết quả");
                    }
                } else {
                    callback.onError("Không thể kết nối đến máy chủ");
                }
            }

            @Override
            public void onFailure(Call<SongResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void getSong(Long songId, GetSongCallback callback) {
        if (songId == null) {
            callback.onError("ID bài hát không hợp lệ");
            return;
        }

        Call<SongResponse> call = songApi.getSong(songId);
        call.enqueue(new Callback<SongResponse>() {
            @Override
            public void onResponse(Call<SongResponse> call, Response<SongResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SongResponse songResponse = response.body();
                    if (songResponse.isSuccess() && songResponse.getData() != null && !songResponse.getData().isEmpty()) {
                        callback.onSuccess(songResponse.getData().get(0));
                    } else {
                        callback.onError(songResponse.getMessage() != null ? 
                            songResponse.getMessage() : "Không tìm thấy bài hát");
                    }
                } else {
                    callback.onError("Không thể kết nối đến máy chủ");
                }
            }

            @Override
            public void onFailure(Call<SongResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
} 