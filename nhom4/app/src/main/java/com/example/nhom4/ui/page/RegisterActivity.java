package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom4.MainActivity; // Màn hình chính sau khi đăng ký/đăng nhập
import com.example.nhom4.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private CheckBox checkBoxShowPassword;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các view từ layout
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        etConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        tvLoginLink = findViewById(R.id.textViewLoginLink);
        progressBar = findViewById(R.id.progressBar);

        // Xử lý sự kiện cho nút "Create Account"
        btnRegister.setOnClickListener(v -> registerUser());

        // Xử lý sự kiện cho text "Login" để quay về màn hình đăng nhập
        tvLoginLink.setOnClickListener(v -> {
            // Kết thúc Activity hiện tại để quay lại màn hình Login
            finish();
        });

        // Xử lý sự kiện cho checkbox "Show password"
        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

            etPassword.setInputType(inputType);
            etConfirmPassword.setInputType(inputType);

            // Di chuyển con trỏ về cuối chuỗi
            etPassword.setSelection(etPassword.length());
            etConfirmPassword.setSelection(etConfirmPassword.length());
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // --- VALIDATE INPUT ---
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

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu.");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp.");
            etConfirmPassword.requestFocus();
            return;
        }

        // Hiển thị ProgressBar và vô hiệu hóa nút bấm
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // --- FIREBASE REGISTRATION ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Ẩn ProgressBar và kích hoạt lại nút bấm
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Đăng ký thành công, tự động đăng nhập và chuyển màn hình
                        Toast.makeText(RegisterActivity.this, "Đăng ký tài khoản thành công.", Toast.LENGTH_SHORT).show();

                        // (Tùy chọn) Gửi email xác thực
                        // FirebaseUser user = mAuth.getCurrentUser();
                        // if (user != null) {
                        //     user.sendEmailVerification();
                        // }

                        goToMainActivity();

                    } else {
                        // Nếu đăng ký thất bại, hiển thị thông báo lỗi
                        // Thường là do email đã tồn tại
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMainActivity() {
        // Thay vì vào Main, chúng ta vào màn hình tạo Username
        Intent intent = new Intent(getApplicationContext(), CreateUsernameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
