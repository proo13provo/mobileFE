package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;

public class LoginViewModel extends AppCompatActivity {
    Button Loginbt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Loginbt = findViewById(R.id.btnEmail);
        Loginbt.setOnClickListener(v -> {
            Intent intent = new Intent(LoginViewModel.this, LoginActivity.class);
            startActivity(intent);
        });
        ImageButton btBack = findViewById(R.id.btnBack);
        btBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoginViewModel.this, MainActivity.class);
            startActivity(intent);
        });
        TextView btnSignUp = findViewById(R.id.tvRegister);
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginViewModel.this, RegisterViewModel.class);
            startActivity(intent);
        });
    }
}
