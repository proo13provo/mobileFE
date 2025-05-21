package com.example.femobile.service;


import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.response.SongResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SongApi {

    @GET("open/song")
    Call<Song> getSong(@Query("songId") String songId);

    @GET("open/search")
    Call<SongResponse> searchSongs(@Query("keyword") String keyword);
}
