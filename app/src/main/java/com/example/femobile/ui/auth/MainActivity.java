package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;

public class MainActivity extends AppCompatActivity {
    Button btnLogin,bthCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogin = findViewById(R.id.btnLogin);
        bthCreate = findViewById(R.id.btnRegister);
        bthCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,RegisterViewModel.class);
            startActivity(intent);

        });
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginViewModel.class);
            startActivity(intent);
        });
    }
}