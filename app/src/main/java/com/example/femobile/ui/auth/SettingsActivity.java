package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.femobile.R;
import com.example.femobile.model.request.AuthRequest.LogoutRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.TokenManager;
import com.example.femobile.security.GoogleAuthManager;
import com.example.femobile.service.api.AuthApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        TokenManager tokenManager = new TokenManager(this);
        String accessToken = tokenManager.getAccessToken();
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        AuthApi authApi = RetrofitClient.getAuthService(this);
        if (accessToken != null && !accessToken.isEmpty() && email != null && !email.isEmpty()) {
            String bearerToken = "Bearer " + accessToken;
            LogoutRequest logoutRequest = new LogoutRequest(email);
            authApi.logout(bearerToken, logoutRequest).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    // Dù thành công hay thất bại đều xóa token local
                    performLocalLogout();
                }
                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    performLocalLogout();
                }
            });
        } else {
            performLocalLogout();
        }
    }

    private void performLocalLogout() {
        // Xóa token/email local
        TokenManager tokenManager = new TokenManager(this);
        tokenManager.clearTokens();
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        sharedPreferences.edit().remove("email").apply();
        // Đăng xuất Google nếu có
        GoogleAuthManager googleAuthManager = new GoogleAuthManager(this, null);
        googleAuthManager.signOut();
        // Thông báo và chuyển về màn hình đăng nhập
        Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 