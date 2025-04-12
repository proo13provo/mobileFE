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


//        btnLogin = findViewById(R.id.button);
//        edtUser = findViewById(R.id.user);
//        edtPass = findViewById(R.id.pass);
//
//        btnLogin.setOnClickListener(v -> {
//            String name = edtUser.getText().toString();
//            String password = edtPass.getText().toString();
//
//            // Tạo request đăng nhập
//            UserLoginRequest request = new UserLoginRequest(name, password);
//
//            // Gọi API
//            RetrofitClient.getAuthService().login(request).enqueue(new Callback<LoginResponse>() {
//                @Override
//                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(MainActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_LONG).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<LoginResponse> call, Throwable t) {
//                    Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_LONG).show();
//                    System.out.println(t.getMessage());
//                    t.printStackTrace();
//                }
//            });
//        });
    }
}