package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;

public class RegisterViewModel extends AppCompatActivity {
    Button registerBt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        registerBt = findViewById(R.id.btnEmail);
        registerBt.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterViewModel.this, RegisterActivity.class);
            startActivity(intent);
        });
        TextView btnSignIn = findViewById(R.id.tvLogin);
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterViewModel.this, RegisterViewModel.class);
            startActivity(intent);
        });
        ImageButton btBack = findViewById(R.id.btnBack);
        btBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterViewModel.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
