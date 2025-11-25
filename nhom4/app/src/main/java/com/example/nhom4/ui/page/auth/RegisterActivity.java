package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom4.R;
import com.example.nhom4.ui.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private CheckBox checkBoxShowPassword;
    private TextView tvLoginLink;
    private ProgressBar progressBar;

    // Sử dụng ViewModel thay vì gọi trực tiếp FirebaseAuth
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupEvents();
        observeViewModel();
    }

    private void initViews() {
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        etConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        tvLoginLink = findViewById(R.id.textViewLoginLink);
        progressBar = findViewById(R.id.progressBar);

        // Lưu ý: Nếu layout của bạn có checkbox thì ánh xạ ở đây,
        // nếu không có thì bỏ qua để tránh NullPointerException
        // checkBoxShowPassword = findViewById(R.id.checkboxShowPassword);
    }

    private void setupEvents() {
        // Xử lý sự kiện đăng ký
        btnRegister.setOnClickListener(v -> handleRegister());

        // Quay lại màn hình đăng nhập
        tvLoginLink.setOnClickListener(v -> finish());

        // Xử lý ẩn/hiện mật khẩu
        if (checkBoxShowPassword != null) {
            checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int inputType = isChecked
                        ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

                etPassword.setInputType(inputType);
                etConfirmPassword.setInputType(inputType);

                // Di chuyển con trỏ về cuối chuỗi
                if (etPassword.getText() != null) {
                    etPassword.setSelection(etPassword.getText().length());
                }
                if (etConfirmPassword.getText() != null) {
                    etConfirmPassword.setSelection(etConfirmPassword.getText().length());
                }
            });
        }
    }

    private void handleRegister() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // --- VALIDATE INPUT (Logic UI đơn giản giữ ở View) ---
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email.");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu.");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu.");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp.");
            etConfirmPassword.requestFocus();
            return;
        }

        // Gọi ViewModel để xử lý logic nghiệp vụ
        authViewModel.register(email, password);
    }

    private void observeViewModel() {
        // Lắng nghe kết quả từ AuthViewModel (dùng chung LiveData authResult với Login)
        authViewModel.getAuthResult().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công.", Toast.LENGTH_SHORT).show();
                    goToNextStep();
                    break;
                case ERROR:
                    setLoading(false);
                    String errorMsg = resource.message != null ? resource.message : "Đăng ký thất bại";
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnRegister.setEnabled(!isLoading);
    }

    private void goToNextStep() {
        // Chuyển sang màn hình tạo Username
        Intent intent = new Intent(getApplicationContext(), CreateUsernameActivity.class);
        // Xóa cờ Activity cũ để không back lại được màn hình đăng ký
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
