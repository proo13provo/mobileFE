package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.femobile.R;
import com.example.femobile.model.request.LoginRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    Button bthButton;
    EditText edtEmail,edtPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        bthButton = findViewById(R.id.login);
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);

        TextView btnSignUp = findViewById(R.id.sendCreate);
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
     bthButton.setOnClickListener(v -> {
         String email = edtEmail.getText().toString();
         String password = edtPassword.getText().toString();
         LoginRequest request = new LoginRequest(email, password);
         AuthApi authService = RetrofitClient.getAuthService(this);

         authService.login(request).enqueue(new Callback<AuthResponse>() {
             @Override
             public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                 if (response.isSuccessful() && response.body() != null) {
                     String accessToken = response.body().getAccessToken();
                     String refreshToken = response.body().getRefreshToken();
                     SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                     SharedPreferences.Editor editor = sharedPreferences.edit();
                     editor.putString("accessToken", accessToken);
                     editor.putString("refreshToken", refreshToken);
                     editor.putString("message","Susscess");
                     editor.apply();
                     // Hiển thị message và token
                     Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                     Toast.makeText(LoginActivity.this, "Access Token: " + accessToken, Toast.LENGTH_LONG).show();
                     Toast.makeText(LoginActivity.this, "Refresh Token: " + refreshToken, Toast.LENGTH_LONG).show();
                 } else {
                     Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show();
                 }
             }

             @Override
             public void onFailure(Call<AuthResponse> call, Throwable t) {
                 Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                 System.out.println(t.getMessage());
                 t.printStackTrace();
             }
         });


        });


    }



}


