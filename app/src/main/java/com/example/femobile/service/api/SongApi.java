package com.example.femobile.service.api;

import com.example.femobile.model.request.SongRequest.Song;
import com.example.femobile.model.request.SongRequest.SongIdsRequest;
import com.example.femobile.model.response.SearchResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SongApi {

    @GET("open/song")
    Call<Song> getSong(@Query("songId") String songId);

    @GET("open/search")
    Call<SearchResponse> searchSongs(@Query("keyword") String keyword);

    @POST("open/nextSong")
    Call<Song> getNextSong(@Body SongIdsRequest songIdsRequest);
}
