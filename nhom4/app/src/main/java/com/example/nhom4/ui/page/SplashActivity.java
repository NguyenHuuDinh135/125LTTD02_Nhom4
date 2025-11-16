package com.example.nhom4.ui.page;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Thêm import này

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Điều này giúp người dùng có cảm giác app đang tải tài nguyên
        new Handler().postDelayed(this::checkUserStatus, 0); // Không delay
    }

    private void checkUserStatus() {
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Người dùng đã đăng nhập, chuyển đến MainActivity
            goToMainActivity();
        } else {
            // Người dùng chưa đăng nhập, chuyển đến LoginActivity
            goToLoginActivity();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        // Cờ này đảm bảo người dùng không thể quay lại SplashActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng SplashActivity
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Đóng SplashActivity
    }
}
