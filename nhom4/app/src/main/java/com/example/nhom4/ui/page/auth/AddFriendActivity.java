package com.example.nhom4.ui.page.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.FriendRequestAdapter;
import com.example.nhom4.ui.adapter.UserSuggestionAdapter;
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

    private RecyclerView rcvFriends, rcvSuggestions;
    private FriendRequestAdapter requestAdapter;
    private UserSuggestionAdapter suggestionAdapter;

    private List<FriendRequest> requestList;
    private List<User> suggestionList;

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_sheet_friend);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        currentUserId = currentUser.getUid();

        rcvFriends = findViewById(R.id.rcvFriends);
        rcvSuggestions = findViewById(R.id.rcvSuggestions);

        // --- Friend Requests ---
        requestList = new ArrayList<>();
        requestAdapter = new FriendRequestAdapter(requestList, new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                updateFriendRequestStatus(request, "accepted");
            }

            @Override
            public void onDecline(FriendRequest request) {
                updateFriendRequestStatus(request, "declined");
            }
        });
        rcvFriends.setLayoutManager(new LinearLayoutManager(this));
        rcvFriends.setAdapter(requestAdapter);
        loadFriendRequests();

        // --- Friend Suggestions ---
        suggestionList = new ArrayList<>();
        suggestionAdapter = new UserSuggestionAdapter(suggestionList, this::sendFriendRequest);
        rcvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rcvSuggestions.setAdapter(suggestionAdapter);
        loadSuggestedUsers();
    }

    private void loadFriendRequests() {
        db.collection("relationships")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        FriendRequest fr = doc.toObject(FriendRequest.class);
                        fr.setRequestId(doc.getId());
                        requestList.add(fr);
                    }
                    requestAdapter.setRequests(requestList);
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi load friend requests: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void loadSuggestedUsers() {
        db.collection("users")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        suggestionList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            if (user.getUid() != null && !user.getUid().equals(currentUserId)) {
                                suggestionList.add(user);
                            }
                        }
                        suggestionAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateFriendRequestStatus(FriendRequest request, String status) {
        db.collection("relationships")
                .document(request.getRequestId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã " + status + " yêu cầu", Toast.LENGTH_SHORT).show();
                    requestList.remove(request);
                    requestAdapter.setRequests(requestList);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendFriendRequest(User targetUser) {
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("members", Arrays.asList(currentUserId, targetUser.getUid()));
        relationship.put("requesterId", currentUserId);
        relationship.put("recipientId", targetUser.getUid());
        relationship.put("status", "pending");
        relationship.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("relationships")
                .add(relationship)
                .addOnSuccessListener(docRef -> Toast.makeText(this,
                        "Đã gửi lời mời tới " + targetUser.getUsername(), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
