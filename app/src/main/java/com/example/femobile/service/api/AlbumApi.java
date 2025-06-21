package com.example.femobile.service.api;

import com.example.femobile.model.request.AlbumRequest.Album;
import com.example.femobile.model.response.AlbumResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AlbumApi {
    @GET("user/album/getAllAlbums")
    Call<AlbumResponse> getAllAlbums(@Header("Authorization") String token);

    @GET("user/album/getAlbum")
    Call<Album> getAlbum(@Query("albumId") String albumId );

    @GET("user/album/getAlbumsByArtist")
    Call<Album> getAlbumsByArtist(@Header("Authorization") String token,
                                                @Query("artistId") String artistId);

}
