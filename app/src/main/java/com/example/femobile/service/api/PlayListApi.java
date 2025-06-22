package com.example.femobile.service.api;

import com.example.femobile.model.request.PlayListRequest.PlayListRequest;
import com.example.femobile.model.request.PlayListRequest.UpdatePlayListRequest;
import com.example.femobile.model.response.PlayListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface PlayListApi {
    // lấy tất cả các playList của người dùng
    @GET("user/playlist/getPlayLists")
    Call<List<PlayListResponse>> getPlayLists(@Header("Authorization") String token);
    //thêm playList
    @POST("user/playlist/addPlayList")
    Call<PlayListResponse> addPlayList(@Header("Authorization") String token, @Body PlayListRequest request);
    //cập nhật playlist
    @PUT("user/playlist/updatePlayList")
    Call<PlayListResponse> updatePlayList(@Header("Authorization") String token, @Body UpdatePlayListRequest request);
    // xóa playlist
    @DELETE("user/playlist/deletePlayList")
    Call<String> deletePlayList(@Header("Authorization") String token, @Body Long playlistId);
}
