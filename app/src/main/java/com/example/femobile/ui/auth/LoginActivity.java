package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.databinding.AuthLoginBinding;
import com.example.femobile.model.request.AuthRequest.LoginRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.GoogleAuthManager;
import com.example.femobile.service.api.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements GoogleAuthManager.GoogleAuthCallback {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 100;
    private static final String PREF_NAME = "app_preferences";
    
    private AuthLoginBinding binding;
    private GoogleAuthManager googleAuthManager;
    private AuthApi authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AuthLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeServices();
        setupClickListeners();
    }

    private void initializeServices() {
        authService = RetrofitClient.getAuthService(this);
        googleAuthManager = new GoogleAuthManager(this, this);
    }

    private void setupClickListeners() {
        binding.btnGoogle.setOnClickListener(v -> startGoogleSignIn());
        binding.tvSignUP.setOnClickListener(v -> navigateToRegister());
        binding.btnLogin.setOnClickListener(v -> handleNormalLogin());
        binding.btnBack.setOnClickListener(v -> backToMain());
    }

    private void backToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    }

    private void startGoogleSignIn() {
        Intent signInIntent = googleAuthManager.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void handleNormalLogin() {
        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();

        if (!validateInput(email, password)) {
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        authService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveTokensAndNavigate(response.body());
                } else {
                    showError("Đăng nhập thất bại!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                showError("Lỗi: " + t.getMessage());
                Log.e(TAG, "Login error", t);
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.etEmail.setError("Vui lòng nhập email");
            return false;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            googleAuthManager.handleSignInResult(data);
        }
    }

    @Override
    public void onGoogleAuthSuccess(AuthResponse response) {
        saveTokensAndNavigate(response);
    }

    @Override
    public void onGoogleAuthError(String error) {
        showError(error);
    }

    private void saveTokensAndNavigate(AuthResponse authResponse) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", authResponse.getAccessToken());
        editor.putString("refreshToken", authResponse.getRefreshToken());
        String email = binding.etEmail.getText().toString();
        editor.putString("email", email);
        editor.apply();

        showSuccess("Đăng nhập thành công!");

        Intent intent = new Intent(LoginActivity.this, SecondActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
