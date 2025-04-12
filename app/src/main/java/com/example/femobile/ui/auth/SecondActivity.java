package com.example.femobile.ui.auth;

import static android.widget.Toast.LENGTH_LONG;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femobile.R;

public class SecondActivity extends AppCompatActivity {
    EditText user;
    EditText passW;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
        user = findViewById(R.id.infor);
        passW = findViewById(R.id.passinfor);
       String name = getIntent().getStringExtra("name");
       String pass = getIntent().getStringExtra("pass");
       user.setText("Ten " + name);
       passW.setText("Mat khau " + pass);
        Toast.makeText(this, "Tài khoản: " + name + "\nMật khẩu: " + passW,LENGTH_LONG).show();



    }
}
