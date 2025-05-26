package com.example.femobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;
import com.example.femobile.security.TokenManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btnLogin, btnCreate;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);

        if (tokenManager.hasValidTokens()) {
            Log.d(TAG, "Valid tokens found, launching SecondActivity");
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
            finish();
        } else {
            Log.d(TAG, "No valid tokens found, showing login screen");
            setContentView(R.layout.activity_main);
            setupLoginRegisterButtons();
        }
    }

    private void setupLoginRegisterButtons() {
        Log.d(TAG, "Setting up login/register buttons");

        btnLogin = findViewById(R.id.btnLogin);
        btnCreate = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
