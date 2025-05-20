package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.BuildConfig;
import com.example.femobile.R;
import com.example.femobile.model.request.AuthRequest.LoginRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.AuthApi;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;

    EditText edtEmail, edtPassword;
    Button bthButton;
    ImageButton googlebt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_login);

        // Ánh xạ view
        bthButton = findViewById(R.id.btn_login);
        edtEmail = findViewById(R.id.et_email);
        edtPassword = findViewById(R.id.et_password);
        googlebt = findViewById(R.id.btn_google);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(BuildConfig.CLIENT_ID, true)  // <-- Thay YOUR_WEB_CLIENT_ID bằng web client ID của bạn từ Google Console
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Xử lý đăng nhập Google
        googlebt.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Xử lý đăng ký (đi đến màn hình khác nếu có)
        TextView btnSignUp = findViewById(R.id.tv_sign_UP);
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // <-- Sửa lại nếu LoginViewModel không phải là Activity
            startActivity(intent);
        });

        // Xử lý đăng nhập thường
        bthButton.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            LoginRequest request = new LoginRequest(email, password);
            AuthApi authService = RetrofitClient.getAuthService(this);

            authService.login(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        saveTokensAndNavigate(response.body());
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LOGIN_ERROR", t.getMessage(), t);
                }
            });
        });
    }

    // Nhận kết quả đăng nhập từ Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String authCode = account.getServerAuthCode();
                    sendGoogleAuthCodeToServer(authCode);
                    assert authCode != null;
                }
            } catch (ApiException e) {
                Log.d("GOOGLE_SIGN_IN", "resultCode: " + resultCode + ", data: " + data);
//                Toast.makeText(this, "Đăng nhập Google thất bại!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Gửi mã code từ Google Sign-In về server backend để xác thực
    private void sendGoogleAuthCodeToServer(String authCode) {
        AuthApi authService = RetrofitClient.getAuthService(this);
        authService.loginWithGoogle(authCode).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveTokensAndNavigate(response.body());
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lưu token và chuyển trang
    private void saveTokensAndNavigate(AuthResponse authResponse) {
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", authResponse.getAccessToken());
        editor.putString("refreshToken", authResponse.getRefreshToken());
        editor.apply();

        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, SecondActivity.class);
        startActivity(intent);
        finish();
    }
}
