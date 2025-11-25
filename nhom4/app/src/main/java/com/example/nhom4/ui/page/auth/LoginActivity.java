package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.ui.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister, tvForgotPassword;
    private ProgressBar progressBar;

    // [MVVM] Sử dụng ViewModel thay vì gọi trực tiếp FirebaseAuth
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Ánh xạ View
        initViews();

        // 3. Thiết lập sự kiện (Click listener)
        setupListeners();

        // 4. Lắng nghe kết quả từ ViewModel (Observer)
        observeViewModel();
    }

    private void initViews() {
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvGoToRegister = findViewById(R.id.textViewSignUp);
        tvForgotPassword = findViewById(R.id.textViewForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // Nút Đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // Chuyển sang màn hình Đăng ký
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Quên mật khẩu (Tính năng chưa phát triển)
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void handleLogin() {
        String email = "";
        if (etEmail.getText() != null) email = etEmail.getText().toString().trim();

        String password = "";
        if (etPassword.getText() != null) password = etPassword.getText().toString().trim();

        // Validate dữ liệu đầu vào
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email.");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ.");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu.");
            etPassword.requestFocus();
            return;
        }

        // Gọi ViewModel để thực hiện đăng nhập
        authViewModel.login(email, password);
    }

    private void observeViewModel() {
        authViewModel.getAuthResult().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    // Hiển thị Loading
                    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);
                    break;

                case SUCCESS:
                    // Đăng nhập thành công
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                    break;

                case ERROR:
                    // Đăng nhập thất bại
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Lỗi: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
