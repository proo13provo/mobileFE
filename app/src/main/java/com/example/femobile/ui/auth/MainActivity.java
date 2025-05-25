package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.model.request.AuthRequest.RefreshTokenRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final long TOKEN_EXPIRATION_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds
    Button btnLogin, bthCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra token trước khi hiển thị giao diện
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("accessToken", "");
        String refreshToken = sharedPreferences.getString("refreshToken", "");
        long expirationTime = sharedPreferences.getLong("tokenExpirationTime", 0);
        
        Log.d(TAG, "Checking tokens - Access Token: " + (accessToken.isEmpty() ? "Empty" : "Exists" + accessToken));
        Log.d(TAG, "Checking tokens - Refresh Token: " + (refreshToken.isEmpty() ? "Empty" : "Exists" + refreshToken));
        Log.d(TAG, "Token expiration time: " + expirationTime);
        
        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            // Kiểm tra xem token có hết hạn chưa
            if (System.currentTimeMillis() >= expirationTime) {
                Log.d(TAG, "Access token expired, attempting to refresh");
                refreshAccessToken(refreshToken);
            } else {
                Log.d(TAG, "Access token still valid, proceeding to main screen");
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
                finish();
            }
            return;
        }
        
        Log.d(TAG, "No valid tokens found, showing login screen");
        setContentView(R.layout.activity_main);
        setupLoginRegisterButtons();
    }

    private void refreshAccessToken(String refreshToken) {
        Log.d(TAG, "Starting token refresh process");
        AuthApi authService = RetrofitClient.getAuthService(this);
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        authService.refreshToken(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Token refresh successful");
                    // Lưu token mới và thời gian hết hạn
                    SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("accessToken", response.body().getAccessToken());
                    editor.putString("refreshToken", response.body().getRefreshToken());
                    // Set expiration time to 10 minutes from now
                    editor.putLong("tokenExpirationTime", System.currentTimeMillis() + TOKEN_EXPIRATION_TIME);
                    editor.apply();

                    Log.d(TAG, "New tokens saved successfully with expiration time: " + (System.currentTimeMillis() + TOKEN_EXPIRATION_TIME));
                    // Chuyển đến SecondActivity
                    Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Token refresh failed - Response code: " + response.code());
                    // Clear invalid tokens
                    SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    
                    // Show login screen
                    setContentView(R.layout.activity_main);
                    setupLoginRegisterButtons();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Token refresh failed with error: " + t.getMessage());
                // Show login screen
                setContentView(R.layout.activity_main);
                setupLoginRegisterButtons();
            }
        });
    }

    private void setupLoginRegisterButtons() {
        Log.d(TAG, "Setting up login/register buttons");
        btnLogin = findViewById(R.id.btnLogin);
        bthCreate = findViewById(R.id.btnRegister);
        
        bthCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}