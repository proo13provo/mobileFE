package com.example.femobile.service.api;

import com.example.femobile.model.response.SearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchApi {
    @GET("open/search")
    Call<SearchResponse> searchSongs(@Query("keyword") String keyword);
}
