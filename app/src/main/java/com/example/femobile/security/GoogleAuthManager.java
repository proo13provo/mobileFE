package com.example.femobile.security;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.femobile.BuildConfig;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.AuthApi;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleAuthManager {
    private static final String TAG = "GoogleAuthManager";
    private static final int RC_SIGN_IN = 100;

    private final Context context;
    private final GoogleSignInClient googleSignInClient;
    private final AuthApi authService;
    private final GoogleAuthCallback callback;

    public interface GoogleAuthCallback {
        void onGoogleAuthSuccess(AuthResponse response);
        void onGoogleAuthError(String error);
    }

    public GoogleAuthManager(Context context, GoogleAuthCallback callback) {
        this.context = context;
        this.callback = callback;
        this.authService = RetrofitClient.getAuthService(context);
        this.googleSignInClient = initializeGoogleSignIn();
    }

    private GoogleSignInClient initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.CLIENT_ID)
                .requestEmail()
                .requestServerAuthCode(BuildConfig.CLIENT_ID)
                .build();

        return GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String serverAuthCode = account.getServerAuthCode();
            if (serverAuthCode != null) {
                sendGoogleAuthCodeToServer(serverAuthCode);
            } else {
                callback.onGoogleAuthError("Không thể lấy mã xác thực từ Google!");
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed", e);
            callback.onGoogleAuthError("Đăng nhập Google thất bại!");
        }
    }

    private void sendGoogleAuthCodeToServer(String authCode) {
        Log.d(TAG, "Sending Google auth code to server");
        authService.loginWithGoogle(authCode).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onGoogleAuthSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Google login failed. Code: " + response.code() + ", Error: " + errorBody);
                    callback.onGoogleAuthError("Đăng nhập Google thất bại! Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Google login network error", t);
                callback.onGoogleAuthError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void signOut() {
        googleSignInClient.signOut();
    }
} 