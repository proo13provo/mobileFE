package com.example.femobile.service;

import com.example.femobile.model.request.LoginRequest;
import com.example.femobile.model.request.LogoutRequest;
import com.example.femobile.model.request.RefreshTokenRequest;
import com.example.femobile.model.request.RegisterRequest;
import com.example.femobile.model.request.VerificationRequest;
import com.example.femobile.model.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
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

    @POST("api/auth/getAccessToken")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

}
