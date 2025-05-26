package com.example.femobile.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.femobile.model.request.AuthRequest.RefreshTokenRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_TOKEN_EXPIRATION = "tokenExpirationTime";
    private static final long TOKEN_EXPIRATION_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final AuthApi authService;
    private boolean isRefreshing = false;
    private OnTokenRefreshListener currentRefreshListener;

    public TokenManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.authService = RetrofitClient.getAuthService(context);
    }

    public boolean hasValidTokens() {
        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();
        long expirationTime = getTokenExpirationTime();

        Log.d(TAG, "Access Token: " + (accessToken.isEmpty() ? "Empty" : "Exists"));
        Log.d(TAG, "Refresh Token: " + (refreshToken.isEmpty() ? "Empty" : "Exists"));
        Log.d(TAG, "Expiration Time: " + expirationTime);

        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            if (System.currentTimeMillis() >= expirationTime) {
                // Token hết hạn, tự động refresh
                refreshToken(null);
                return false;
            }
            return true;
        }
        return false;
    }

    public void refreshToken(OnTokenRefreshListener listener) {
        if (isRefreshing) {
            // Nếu đang trong quá trình refresh, lưu listener để gọi lại sau
            currentRefreshListener = listener;
            return;
        }

        String refreshToken = getRefreshToken();
        if (refreshToken.isEmpty()) {
            if (listener != null) {
                listener.onTokenRefreshFailed("No refresh token available");
            }
            return;
        }

        isRefreshing = true;
        Log.d(TAG, "Refreshing access token...");
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        authService.refreshToken(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Token refresh successful");
                    AuthResponse authResponse = response.body();
                    saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                    
                    if (listener != null) {
                        listener.onTokenRefreshSuccess();
                    }
                    if (currentRefreshListener != null) {
                        currentRefreshListener.onTokenRefreshSuccess();
                        currentRefreshListener = null;
                    }
                } else {
                    Log.e(TAG, "Token refresh failed: HTTP " + response.code());
                    clearTokens();
                    if (listener != null) {
                        listener.onTokenRefreshFailed("Token refresh failed: HTTP " + response.code());
                    }
                    if (currentRefreshListener != null) {
                        currentRefreshListener.onTokenRefreshFailed("Token refresh failed: HTTP " + response.code());
                        currentRefreshListener = null;
                    }
                }
                isRefreshing = false;
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Token refresh error: " + t.getMessage());
                clearTokens();
                if (listener != null) {
                    listener.onTokenRefreshFailed("Token refresh error: " + t.getMessage());
                }
                if (currentRefreshListener != null) {
                    currentRefreshListener.onTokenRefreshFailed("Token refresh error: " + t.getMessage());
                    currentRefreshListener = null;
                }
                isRefreshing = false;
            }
        });
    }

    public String getValidAccessToken() {
        if (hasValidTokens()) {
            return getAccessToken();
        }
        return "";
    }

    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putLong(KEY_TOKEN_EXPIRATION, System.currentTimeMillis() + TOKEN_EXPIRATION_TIME);
        editor.apply();
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, "");
    }

    public long getTokenExpirationTime() {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRATION, 0);
    }

    public interface OnTokenRefreshListener {
        void onTokenRefreshSuccess();
        void onTokenRefreshFailed(String error);
    }
}
