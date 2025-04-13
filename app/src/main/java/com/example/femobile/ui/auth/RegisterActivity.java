package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.model.request.LoginRequest;
import com.example.femobile.model.request.RegisterRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.AuthApi;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    Button bthButton;
    EditText edtEmail,edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create);
        bthButton = findViewById(R.id.createAccount);
        edtEmail = findViewById(R.id.email1);
        edtPassword = findViewById(R.id.password1);

        TextView btnLogin = findViewById(R.id.sendlogin);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        bthButton.setOnClickListener(v -> {
            Log.d("DEBUG", "Nút CreateAccount đã được click");
            Toast.makeText(this, "Đã nhấn nút!", Toast.LENGTH_SHORT).show();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            RegisterRequest registerRequest = new RegisterRequest(email, password);
            AuthApi authService = RetrofitClient.getAuthService(this);
            Log.d("DEBUG", "Email: " + email);
            Log.d("DEBUG", "Password: " + password);
            Log.d("DEBUG", "RegisterRequest: " + new Gson().toJson(registerRequest));
            authService.register(registerRequest).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();

                        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("message",message);
                        editor.apply();
                        // Hiển thị message và token
                        Toast.makeText(RegisterActivity.this, "Đăng kí thành công! vui lòng xác thực thực email", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng kí thất bại!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    System.out.println(t.getMessage());
                    t.printStackTrace();
                }
            });
        });
    }
}