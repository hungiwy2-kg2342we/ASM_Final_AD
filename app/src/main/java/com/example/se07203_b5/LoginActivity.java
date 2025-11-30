package com.example.se07203_b5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    // Hằng số cần thiết để đồng bộ với MainActivity
    private static final String PREF_LAST_ACTIVE_TIME = "last_active_time";
    // Giả định SESSION_TIMEOUT_MS = 60000ms (1 phút)

    Button btnSubmitLogin, btnGoToRegister;
    EditText edtUsername, edtPassword;

    DatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Khởi tạo SharedPreferences và DB Helper sớm
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        // ⭐ SỬA LỖI ĐỒNG BỘ: Dùng USER_ID thay vì cờ isLogin ⭐
        long userId = sharedPreferences.getLong("user_id", -1);

        // --- 1. KIỂM TRA TRẠNG THÁI ĐĂNG NHẬP HIỆN TẠI ---
        if (userId > 0) {
            // Nếu có User ID, kiểm tra timestamp để xem phiên có còn hiệu lực không
            if (isSessionStillValid()) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish(); // Kết thúc LoginActivity ngay lập tức
                return;
            } else {
                // Nếu phiên hết hạn (quá 1 phút), xóa user_id và tiến hành đăng nhập lại
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("user_id");
                editor.remove(PREF_LAST_ACTIVE_TIME);
                editor.apply();
                // Không cần return, tiếp tục chạy onCreate để người dùng đăng nhập lại
            }
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSubmitLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Username và Password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Giả định User class và getUserByUsernameAndPassword tồn tại
            User user = databaseHelper.getUserByUsernameAndPassword(username, password);

            if(user != null && user.getFullname() != null) {
                // ⭐ 2. LƯU TIMESTAMP KHI ĐĂNG NHẬP THÀNH CÔNG ⭐
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("fullname", user.getFullname());
                editor.putLong("user_id", user.getId());

                // Lưu dấu thời gian hoạt động cuối cùng
                editor.putLong(PREF_LAST_ACTIVE_TIME, System.currentTimeMillis());

                // Xóa cờ "isLogin" cũ (nếu có)
                editor.remove("isLogin");
                editor.apply();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Sai username hoặc password", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Kiểm tra xem phiên có còn hiệu lực không (dưới 1 phút kể từ lần cuối hoạt động).
     * Hàm này chỉ chạy khi có user_id > 0.
     */
    private boolean isSessionStillValid() {
        long lastActiveTime = sharedPreferences.getLong(PREF_LAST_ACTIVE_TIME, 0);
        long currentTime = System.currentTimeMillis();

        // SESSION_TIMEOUT_MS phải được định nghĩa/được biết
        // Giả sử 60000ms là giá trị timeout (đã định nghĩa trong MainActivity)
        final long TIMEOUT_MS = 60000;

        return lastActiveTime > 0 && (currentTime - lastActiveTime) < TIMEOUT_MS;
    }

    // Giả định User class có hàm getId() và getFullname()
}