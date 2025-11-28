package com.example.se07203_b5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    Button btnSubmitLogin, btnGoToRegister;
    EditText edtUsername, edtPassword;

    DatabaseHelper databaseHelper; // Khai báo sử dụng database Helper

    SharedPreferences sharedPreferences; // Khai báo sử dụng shared Preferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo với key "AppData" và chế độ private
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        // Khởi tạo editor để chỉnh sửa dữ liệu trong shared Preferences
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSubmitLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();
            try {
                DatabaseHelper db = new DatabaseHelper(this);
                User user = db.getUserByUsernameAndPassword(username, password);
                if (user != null && user.getFullname() != null){
                    sharedPreferencesEditor.putString("username", username);
                    sharedPreferencesEditor.putString("fullname", user.getFullname());
                    sharedPreferencesEditor.putLong("user_id", user.getId());
                    sharedPreferencesEditor.putBoolean("isLogin", true);
                    sharedPreferencesEditor.apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
}