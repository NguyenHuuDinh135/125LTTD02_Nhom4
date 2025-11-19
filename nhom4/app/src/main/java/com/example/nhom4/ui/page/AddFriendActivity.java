package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.data.model.User;
import com.example.nhom4.ui.page.adapter.UserSuggestionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
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

    private FirebaseFirestore db;
    private String currentUserId;
    private boolean hasSentRequest = false; // Cờ kiểm tra xem đã gửi lời mời chưa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnContinue = findViewById(R.id.btn_continue);
        recyclerView = findViewById(R.id.recycler_view_suggestions);

        // Disable nút tiếp tục ban đầu
        updateContinueButtonState();

        // Setup RecyclerView
        userList = new ArrayList<>();
        adapter = new UserSuggestionAdapter(userList, this::sendFriendRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load danh sách gợi ý
        loadSuggestedUsers();

        // Sự kiện nút Tiếp tục -> Vào Main
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateContinueButtonState() {
        if (hasSentRequest) {
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
        } else {
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f);
        }
    }

    private void loadSuggestedUsers() {
        // Lấy tối đa 10 user khác bản thân
        db.collection("users")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            // Không hiển thị chính mình
                            if (!user.getUid().equals(currentUserId)) {
                                userList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void sendFriendRequest(User targetUser) {
        // Tạo document mới trong collection relationships
        // ID document có thể tự sinh, hoặc quy ước: requesterId_recipientId

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

                    // Đánh dấu đã gửi và mở khóa nút tiếp tục
                    hasSentRequest = true;
                    updateContinueButtonState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
