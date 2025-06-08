package com.example.femobile.service.api;

import com.example.femobile.model.request.AlbumRequest.Album;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AlbumApi {
    @GET("user/album/getAlbums")
    Call<List<Album>> getAlbums(@Header("Authorization") String token);

    @GET("user/album/getAlbum")
    Call<Album> getAlbum(@Query("albumId") String albumId );

    @GET("user/album/getAlbumsByArtist")
    Call<Album> getAlbumsByArtist(@Header("Authorization") String token,
                                                @Query("artistId") String artistId);

}
