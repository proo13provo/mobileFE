package com.example.femobile.service.api;

import com.example.femobile.model.request.SongRequest.Album;
import com.example.femobile.model.request.SongRequest.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AlbumApi {
    @GET("/getAlbums")
    Call<List<Album>> getAlbums(@Header("Authorization") String token);

    @GET("/getAlbum")
    Call<Album> getAlbum(@Header("Authorization") String token,
                                 @Query("albumId") String albumId);

    @GET("/getAlbumsByArtist")
    Call<Album> getAlbumsByArtist(@Header("Authorization") String token,
                                                @Query("artistId") String artistId);

}
