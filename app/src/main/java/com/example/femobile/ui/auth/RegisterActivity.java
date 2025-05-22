package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.model.request.AuthRequest.RegisterRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.api.AuthApi;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText edtEmail, edtPassword, edtPasswordconFirm;
    private ImageButton btnBack;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_register);
        
        // Initialize views
        btnRegister = findViewById(R.id.btn_register);
        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        edtPasswordconFirm = findViewById(R.id.et_confirm_password);
        btnBack = findViewById(R.id.btn_back);
        tvSignIn = findViewById(R.id.tv_sign_in);

        // Set up click listeners
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            Log.d("DEBUG", "Register button clicked");

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirmPassword = edtPasswordconFirm.getText().toString();

            // Validate password match
            if (!confirmPassword.equals(password)) {
                edtPasswordconFirm.setError("Mật khẩu xác nhận không khớp");
                edtPasswordconFirm.setBackgroundColor(Color.parseColor("#33FF0000"));
                edtPasswordconFirm.requestFocus();
                return;
            } else {
                edtPasswordconFirm.setError(null);
                edtPasswordconFirm.setBackgroundResource(R.drawable.edittext_background);
            }

            // Validate empty fields
            if(email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
                edtEmail.requestFocus();
                return;
            }
            if(password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            // Create register request
            RegisterRequest registerRequest = new RegisterRequest(email, password);
            AuthApi authService = RetrofitClient.getAuthService(this);
            
            Log.d("DEBUG", "Email: " + email);
            Log.d("DEBUG", "Password: " + password);
            Log.d("DEBUG", "RegisterRequest: " + new Gson().toJson(registerRequest));

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
        });
    }
}