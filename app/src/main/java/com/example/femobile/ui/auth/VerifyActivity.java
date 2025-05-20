package com.example.femobile.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.model.request.AuthRequest.VerificationRequest;
import com.example.femobile.model.response.AuthResponse;
import com.example.femobile.network.RetrofitClient;
import com.example.femobile.service.AuthApi;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyActivity extends AppCompatActivity {
    EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    Button btnVerify;
    ImageButton btnBack;
    TextView tvResendCode,tvEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify);

        // Initialize UI components
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        btnVerify = findViewById(R.id.btn_verify);
        btnBack = findViewById(R.id.btn_back);
        tvResendCode = findViewById(R.id.tv_resend);
        tvEmail = findViewById(R.id.tv_email);
        setupOtpInputs();

        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "example@email.com");
        if (tvEmail != null) {
            tvEmail.setText(email);
        }
        // Back button click listener
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(VerifyActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Verify button click listener
        btnVerify.setOnClickListener(v -> {
            // Combine OTP digits
            String code = etOtp1.getText().toString() +
                    etOtp2.getText().toString() +
                    etOtp3.getText().toString() +
                    etOtp4.getText().toString() +
                    etOtp5.getText().toString() +
                    etOtp6.getText().toString();

            // Validate OTP
            if (code.length() != 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số mã xác thực", Toast.LENGTH_SHORT).show();
                etOtp1.requestFocus();
                return;
            }

            // Get email from SharedPreferences
            SharedPreferences sharedPreferences1 = getSharedPreferences("app_preferences", MODE_PRIVATE);
            String email1 = sharedPreferences1.getString("email", null);

            if (email1 == null) {
                Toast.makeText(this, "Không tìm thấy email. Vui lòng đăng ký lại.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(VerifyActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Create verification request
            VerificationRequest verifyRequest = new VerificationRequest(email,code);
            AuthApi authService = RetrofitClient.getAuthService(this);

            Log.d("DEBUG", "Verification Request: " + new Gson().toJson(verifyRequest));

            // Make API call to verify code
            authService.verifyRegistration(verifyRequest).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();
                        Toast.makeText(VerifyActivity.this, "Xác thực thành công: " + message, Toast.LENGTH_LONG).show();

                        // Clear SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("email");
                        editor.remove("message");
                        editor.apply();

                        // Navigate to LoginActivity
                        Intent intent = new Intent(VerifyActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerifyActivity.this, "Xác thực thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(VerifyActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                }
            });
        });

        // Resend code click listener (optional)
        if (tvResendCode != null) {
            tvResendCode.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng gửi lại mã chưa được triển khai", Toast.LENGTH_SHORT).show();
                // TODO: Implement resend code API call if required
            });
        }
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        // Chuyển focus sang ô tiếp theo
                        if (index < otpFields.length - 1) {
                            otpFields[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        // Quay lại ô trước nếu xóa
                        if (index > 0) {
                            otpFields[index - 1].requestFocus();
                        }
                    }
                }
            });
        }
    }

}