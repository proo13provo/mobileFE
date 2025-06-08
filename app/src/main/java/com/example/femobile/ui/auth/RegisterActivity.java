package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.databinding.AuthRegisterBinding;
import com.example.femobile.model.request.AuthRequest.RegisterRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.security.GoogleAuthManager;
import com.example.femobile.service.api.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements GoogleAuthManager.GoogleAuthCallback {
    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 100;
    
    private AuthRegisterBinding binding;
    private GoogleAuthManager googleAuthManager;
    private AuthApi authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AuthRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initializeServices();
        setupClickListeners();
    }

    private void initializeServices() {
        authService = RetrofitClient.getAuthService(this);
        googleAuthManager = new GoogleAuthManager(this, this);
    }

    private void setupClickListeners() {
        binding.tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnRegister.setOnClickListener(v -> handleNormalRegister());
        binding.btnGoogle.setOnClickListener(v -> startGoogleSignIn());
    }

    private void handleNormalRegister() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        // Validate password match
        if (!confirmPassword.equals(password)) {
            binding.etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            binding.etConfirmPassword.setBackgroundColor(Color.parseColor("#33FF0000"));
            binding.etConfirmPassword.requestFocus();
            return;
        } else {
            binding.etConfirmPassword.setError(null);
            binding.etConfirmPassword.setBackgroundResource(R.drawable.edittext_background);
        }

        // Validate empty fields
        if(email.isEmpty()) {
            binding.etEmail.setError("Vui lòng nhập email");
            binding.etEmail.requestFocus();
            return;
        }
        if(password.isEmpty()) {
            binding.etPassword.setError("Vui lòng nhập mật khẩu");
            binding.etPassword.requestFocus();
            return;
        }

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(email, password);
        
        // Make API call
        authService.register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();

                    // Save data to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("message", message);
                    editor.putString("email", email);
                    editor.apply();

                    Toast.makeText(RegisterActivity.this, "Đăng kí thành công! Vui lòng xác thực email", Toast.LENGTH_LONG).show();
                    
                    // Navigate to VerifyActivity
                    Intent intent = new Intent(RegisterActivity.this, VerifyActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng kí thất bại!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void startGoogleSignIn() {
        Intent signInIntent = googleAuthManager.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", authResponse.getAccessToken());
        editor.putString("refreshToken", authResponse.getRefreshToken());
        editor.apply();

        showSuccess("Đăng nhập thành công!");

        Intent intent = new Intent(RegisterActivity.this, SecondActivity.class);
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