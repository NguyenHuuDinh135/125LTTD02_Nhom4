// ProfileActivity.java
package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom4.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textViewUserEmail = findViewById(R.id.textViewUserEmail);

        // Nhận email từ LoginActivity
        String email = getIntent().getStringExtra("user_email");
        if (email != null) {
            textViewUserEmail.setText("Email: " + email);
        }

        findViewById(R.id.buttonSettings).setOnClickListener(v -> {
            // Quay lại màn hình Login
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Đóng ProfileActivity
        });
    }
}