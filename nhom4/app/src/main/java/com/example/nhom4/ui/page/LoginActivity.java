// LoginActivity.java
package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom4.R;
import com.example.nhom4.ui.page.ProfileActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // TODO: Xác thực đăng nhập (tạm thời giả lập)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Giả sử đăng nhập thành công → chuyển sang Profile
            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            intent.putExtra("user_email", email); // Truyền email sang Profile
            startActivity(intent);
            finish(); // Đóng LoginActivity để không quay lại bằng nút Back
        });
    }
}