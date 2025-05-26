package com.example.femobile.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;
    public AuthInterceptor(Context context) {
        this.context = context;
    }
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        // Lấy access token từ SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("accessToken", null);

        // Tạo request mới và thêm Authorization header nếu có token
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        if (accessToken != null) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }
}