package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.UserSuggestionAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserSuggestionAdapter adapter;
    private List<User> userList;
    private MaterialButton btnContinue;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private String currentUserId;
    private boolean hasSentRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Kiểm tra user null để tránh crash
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        currentUserId = currentUser.getUid();

        // Ánh xạ view
        btnContinue = findViewById(R.id.btn_continue);
        recyclerView = findViewById(R.id.recycler_view_suggestions);
        toolbar = findViewById(R.id.toolbar);

        // --- 1. XỬ LÝ SỰ KIỆN BACK (QUAY LẠI) ---
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Đóng Activity hiện tại để quay lại màn hình trước
        });

        // Disable nút tiếp tục ban đầu
        updateContinueButtonState();

        // Setup RecyclerView
        userList = new ArrayList<>();
        // Khi bấm vào nút Add trên item -> Gọi hàm showConfirmDialog
        adapter = new UserSuggestionAdapter(userList, this::showConfirmDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load danh sách gợi ý
        loadSuggestedUsers();

        // Sự kiện nút Tiếp tục -> Vào Main
        btnContinue.setOnClickListener(v -> {
            goToMainActivity();
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateContinueButtonState() {
        // Nếu người dùng chưa gửi lời mời nào thì nút mờ đi, ngược lại thì sáng lên
        if (hasSentRequest) {
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
        } else {
            // Tùy chọn: Bạn có thể cho phép họ bấm "Tiếp tục" ngay cả khi không add ai
            // Nếu muốn bắt buộc add friend thì để false, nếu không thì để true
            btnContinue.setEnabled(true); // Để true cho trải nghiệm tốt hơn
            btnContinue.setAlpha(1.0f);
        }
    }

    private void loadSuggestedUsers() {
        db.collection("users")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            // Không hiển thị chính mình
                            if (user.getUid() != null && !user.getUid().equals(currentUserId)) {
                                userList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // --- 2. HIỂN THỊ DIALOG XÁC NHẬN ---
    private void showConfirmDialog(User targetUser) {
        new AlertDialog.Builder(this)
                .setTitle("Gửi lời mời kết bạn")
                .setMessage("Bạn có muốn gửi lời mời kết bạn đến " + targetUser.getUsername() + " không?")
                .setPositiveButton("Gửi", (dialog, which) -> {
                    sendFriendRequest(targetUser);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendFriendRequest(User targetUser) {
        // Kiểm tra trùng lặp trước khi gửi (Optional nhưng nên làm)
        // Ở đây làm đơn giản gửi thẳng lên
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("members", Arrays.asList(currentUserId, targetUser.getUid()));
        relationship.put("requesterId", currentUserId);
        relationship.put("recipientId", targetUser.getUid());
        relationship.put("status", "pending");
        relationship.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("relationships")
                .add(relationship)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã gửi lời mời tới " + targetUser.getUsername(), Toast.LENGTH_SHORT).show();

                    hasSentRequest = true;
                    updateContinueButtonState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
