package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.ui.page.auth.LoginActivity;
import com.example.nhom4.ui.page.auth.RegisterActivity;
import com.example.nhom4.ui.viewmodel.AuthViewModel;

public class SplashActivity extends AppCompatActivity {

    private View btnSignUp;
    private View btnLogin;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // [MVVM] Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Ánh xạ view
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.txtLogin);

        setupButtonActions();

        // Bắt đầu quy trình kiểm tra sau 1 giây
        new Handler().postDelayed(this::checkUserStatus, 1000);
    }

    private void checkUserStatus() {
        // [MVVM] Hỏi ViewModel xem đã đăng nhập chưa, không gọi Firebase trực tiếp
        if (authViewModel.isLoggedIn()) {
            // TRƯỜNG HỢP 1: Đã đăng nhập -> Vào Main ngay
            goToMainActivity();
        } else {
            // TRƯỜNG HỢP 2: Chưa đăng nhập -> Hiện nút sau 1 giây nữa
            new Handler().postDelayed(this::showButtons, 1000);
        }
    }

    private void showButtons() {
        // Hiện nút lên
        btnSignUp.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

        // Hiệu ứng fade-in
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        btnSignUp.startAnimation(fadeIn);
        btnLogin.startAnimation(fadeIn);
    }

    private void setupButtonActions() {
        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
