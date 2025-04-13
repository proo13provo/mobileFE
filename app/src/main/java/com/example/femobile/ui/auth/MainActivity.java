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
        setContentView(R.layout.index);
        btnLogin = findViewById(R.id.loginAccount);
        bthCreate = findViewById(R.id.createAccount);
        bthCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
            startActivity(intent);

        });
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}