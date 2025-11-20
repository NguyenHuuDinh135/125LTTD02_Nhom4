package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom4.R;
import com.example.nhom4.ui.page.AddFriendActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateUsernameActivity extends AppCompatActivity {

    private EditText etUsername;
    private MaterialButton btnContinue;
    private MaterialCardView cardInput; // Dùng để đổi màu viền nếu muốn báo lỗi

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Handler handler = new Handler();
    private Runnable checkUsernameRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.editTextUsername);
        btnContinue = findViewById(R.id.buttonContinue);
        cardInput = findViewById(R.id.cardInput);

        // Mặc định disable nút tiếp tục
        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.5f); // Làm mờ nút

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi gõ, disable nút trước
                btnContinue.setEnabled(false);
                btnContinue.setAlpha(0.5f);

                // Hủy lệnh kiểm tra cũ nếu người dùng vẫn đang gõ
                if (checkUsernameRunnable != null) {
                    handler.removeCallbacks(checkUsernameRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    // Chờ 600ms sau khi ngừng gõ mới kiểm tra database
                    checkUsernameRunnable = () -> checkUsernameExists(input);
                    handler.postDelayed(checkUsernameRunnable, 600);
                }
            }
        });

        btnContinue.setOnClickListener(v -> saveUsernameAndContinue());
    }

    private void checkUsernameExists(String username) {
        // Query vào collection users xem có ai dùng username này chưa
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Username đã tồn tại
                            etUsername.setError("Tên người dùng này đã được sử dụng!");
                            btnContinue.setEnabled(false);
                            btnContinue.setAlpha(0.5f);
                        } else {
                            // Username hợp lệ
                            etUsername.setError(null);
                            btnContinue.setEnabled(true);
                            btnContinue.setAlpha(1.0f);
                        }
                    } else {
                        Toast.makeText(CreateUsernameActivity.this, "Lỗi kiểm tra: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUsernameAndContinue() {
        String username = etUsername.getText().toString().trim();
        String userId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        // Tạo dữ liệu User
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("displayName", username); // Tạm lấy display name là username
        userMap.put("uid", userId);
        userMap.put("createdAt", com.google.firebase.Timestamp.now());

        // Lưu vào Firestore
        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Chuyển sang màn hình Add Friend
                    Intent intent = new Intent(CreateUsernameActivity.this, AddFriendActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateUsernameActivity.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
