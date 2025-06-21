package com.example.femobile.service.api;

import com.example.femobile.model.response.FollowArtistResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ArtistAPI {

    @POST("user/followArtist/follow")
    Call<FollowArtistResponse> followArtist(@Header("Authorization") String token, @Query("artistId") String artistId);

    @DELETE("user/followArtist/unfollow")
    Call<Void> unfollowArtist(@Header("Authorization") String token, @Query("artistId") String artistId);

    @GET("user/followArtist/artists")
    Call<List<FollowArtistResponse>> getFollowingArtists(@Header("Authorization") String token);
}
