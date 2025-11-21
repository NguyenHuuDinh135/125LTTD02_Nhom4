// LoginActivity.java
package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom4.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
//    private CheckBox checkBoxShowPassword;
    private TextView tvGoToRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các view từ layout
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvGoToRegister = findViewById(R.id.textViewSignUp);
        tvForgotPassword = findViewById(R.id.textViewForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        // Xử lý sự kiện cho nút "Đăng Nhập"
        btnLogin.setOnClickListener(v -> loginUser());

        // Xử lý sự kiện cho text "Sign Up"
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

//        // Xử lý sự kiện cho checkbox "Show password"
//        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                // Hiển thị mật khẩu
//                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//            } else {
//                // Ẩn mật khẩu
//                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//            }
//            // Di chuyển con trỏ về cuối chuỗi
//            etPassword.setSelection(etPassword.length());
//        });

        // (Tùy chọn) Xử lý sự kiện cho "Forgot password"
        tvForgotPassword.setOnClickListener(v -> {
            // Bạn có thể tạo một activity/dialog mới để xử lý việc reset mật khẩu
            Toast.makeText(this, "Chức năng quên mật khẩu đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // --- VALIDATE INPUT ---
        if (com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email.");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ.");
            etEmail.requestFocus();
            return;
        }

        if (com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu.");
            etPassword.requestFocus();
            return;
        }

        // Hiển thị ProgressBar và vô hiệu hóa nút bấm
//        progressBar.setVisibility(com.google.ar.imp.view.View.VISIBLE);
        btnLogin.setEnabled(false);

        // --- FIREBASE AUTHENTICATION ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Ẩn ProgressBar và kích hoạt lại nút bấm
//                    progressBar.setVisibility(com.google.ar.imp.view.View.GONE);
                    btnLogin.setEnabled(true);

//                    if (task.isSuccessful()) {
//                        // Đăng nhập thành công
//                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
////                        goToMainActivity();
//                    } else {
//                        // Đăng nhập thất bại, hiển thị thông báo lỗi
//                        Toast.makeText(LoginActivity.this, "Xác thực thất bại: " + task.getException().getMessage(),
//                                Toast.LENGTH_LONG).show();
//                    }
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                        // Mở AddFriendActivity thay vì MainActivity
                        Intent intent = new Intent(LoginActivity.this, AddFriendActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Đăng nhập thất bại, hiển thị thông báo lỗi
                        Toast.makeText(LoginActivity.this, "Xác thực thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMainActivity() {
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        // Xóa tất cả các activity trước đó khỏi stack để người dùng không thể quay lại màn hình login
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish(); // Đóng LoginActivity
    }
}

