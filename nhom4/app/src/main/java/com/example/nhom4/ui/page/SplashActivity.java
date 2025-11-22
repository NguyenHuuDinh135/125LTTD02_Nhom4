package com.example.nhom4.ui.page;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.view.animation.AlphaAnimation; // Để tạo hiệu ứng hiện từ từ

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.ui.page.auth.LoginActivity;
import com.example.nhom4.ui.page.auth.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View btnSignUp;
    private View btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ 2 nút
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.txtLogin);

        // Thiết lập sự kiện click cho nút (khi nó hiện lên)
        setupButtonActions();

        // Bắt đầu quy trình kiểm tra
        // Đợi 2 giây (2000ms) để kiểm tra đăng nhập
        new Handler().postDelayed(this::checkUserStatus, 1000);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // TRƯỜNG HỢP 1: Đã đăng nhập
            // Chuyển ngay sang MainActivity
            goToMainActivity();
        } else {
            // TRƯỜNG HỢP 2: Chưa đăng nhập
            // Người dùng ở lại màn hình Splash.
            // Yêu cầu: Delay nút 5 giây (tính từ lúc mở app).
            // Vì đã đợi 2 giây ở trên rồi, nên giờ đợi thêm 3 giây nữa (2 + 3 = 5).
            new Handler().postDelayed(this::showButtons, 1000);
        }
    }

    private void showButtons() {
        // Hiện nút lên
        btnSignUp.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

        // Thêm hiệu ứng fade-in cho mượt (tùy chọn)
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500); // Hiện trong 0.5s
        btnSignUp.startAnimation(fadeIn);
        btnLogin.startAnimation(fadeIn);
    }

    private void setupButtonActions() {
        // Nút Đăng ký -> RegisterActivity
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Nút Đăng nhập -> LoginActivity
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
