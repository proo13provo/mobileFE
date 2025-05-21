package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.model.request.AuthRequest.RefreshTokenRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnLogin, bthCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra token trước khi hiển thị giao diện
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("accessToken", "");
        String refreshToken = sharedPreferences.getString("refreshToken", "");
        
        Log.d(TAG, "Checking tokens - Access Token: " + (accessToken.isEmpty() ? "Empty" : "Exists" + accessToken));
        Log.d(TAG, "Checking tokens - Refresh Token: " + (refreshToken.isEmpty() ? "Empty" : "Exists" + refreshToken));
        
        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            Log.d(TAG, "Both tokens exist, attempting to refresh access token");
            // Thử refresh token
            refreshAccessToken(refreshToken);
            return;
        }
        
        Log.d(TAG, "No valid tokens found, showing login screen");
        setContentView(R.layout.activity_main);
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

    private void refreshAccessToken(String refreshToken) {
        Log.d(TAG, "Starting token refresh process");
        AuthApi authService = RetrofitClient.getAuthService(this);
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        authService.refreshToken(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                Log.d(response.message(), "onResponse: ");
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Token refresh successful");
                    // Lưu token mới
                    SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("accessToken", response.body().getAccessToken());
                    editor.putString("refreshToken", response.body().getRefreshToken());
                    editor.apply();

                    Log.d(TAG, "New tokens saved successfully");
                    // Chuyển đến SecondActivity
                    Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "Token refresh failed - Response code: " + response.code());
                    // Nếu refresh token cũng hết hạn, chuyển về màn hình đăng nhập
                    setContentView(R.layout.activity_main);
                    setupLoginRegisterButtons();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Token refresh failed with error: " + t.getMessage());
                // Nếu có lỗi, chuyển về màn hình đăng nhập
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