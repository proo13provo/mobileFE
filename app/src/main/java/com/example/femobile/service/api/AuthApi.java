package com.example.femobile.service.api;

import com.example.femobile.model.request.AuthRequest.LoginRequest;
import com.example.femobile.model.request.AuthRequest.LogoutRequest;
import com.example.femobile.model.request.AuthRequest.RefreshTokenRequest;
import com.example.femobile.model.request.AuthRequest.RegisterRequest;
import com.example.femobile.model.request.AuthRequest.VerificationRequest;
import com.example.femobile.model.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/register/verify")
    Call<AuthResponse> verifyRegistration(@Body VerificationRequest request);

    @POST("auth/logout")
    Call<AuthResponse> logout(@Header("Authorization") String token, @Body LogoutRequest request);

    @POST("auth/accessToken")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("auth/google/callback")
    @FormUrlEncoded
    Call<AuthResponse> loginWithGoogle(@Field("code") String authCode);
}

