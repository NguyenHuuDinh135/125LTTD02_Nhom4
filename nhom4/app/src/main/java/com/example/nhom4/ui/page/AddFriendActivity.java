package com.example.nhom4.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.data.model.FriendRequest;
import com.example.nhom4.data.model.User;
import com.example.nhom4.ui.page.adapter.UserSuggestionAdapter;
import com.example.nhom4.ui.page.friend.FriendRequestAdapter;
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

    // RecyclerViews
    private RecyclerView rcvFriendRequests, recyclerViewSuggestions;
    private FriendRequestAdapter requestAdapter;
    private UserSuggestionAdapter suggestionAdapter;

    // Data lists
    private List<FriendRequest> requestList;
    private List<User> suggestionList;

    private MaterialButton btnContinue;
    private FirebaseFirestore db;
    private String currentUserId;
    private boolean hasSentRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnContinue = findViewById(R.id.btn_continue);
        recyclerViewSuggestions = findViewById(R.id.recycler_view_suggestions);
        rcvFriendRequests = findViewById(R.id.rcvFriendRequests);

        // Disable Continue button initially
        updateContinueButtonState();

        // --- Friend Requests setup ---
        requestList = new ArrayList<>();
        requestAdapter = new FriendRequestAdapter(requestList, new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request, int position) {
                acceptFriendRequest(request, position);
            }

            @Override
            public void onDecline(FriendRequest request, int position) {
                declineFriendRequest(request, position);
            }
        });
        rcvFriendRequests.setLayoutManager(new LinearLayoutManager(this));
        rcvFriendRequests.setAdapter(requestAdapter);

        loadFriendRequests();

        // --- Friend Suggestions setup ---
        suggestionList = new ArrayList<>();
        suggestionAdapter = new UserSuggestionAdapter(suggestionList, this::sendFriendRequest);
        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggestions.setAdapter(suggestionAdapter);

        loadSuggestedUsers();

        // Continue button click -> MainActivity
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateContinueButtonState() {
        if (hasSentRequest || !requestList.isEmpty()) {
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
        } else {
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f);
        }
    }

    // --- Load Friend Requests ---
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
                    updateContinueButtonState();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Lỗi load friend requests: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    // --- Load Friend Suggestions ---
    private void loadSuggestedUsers() {
        db.collection("users")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        suggestionList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            if (!user.getUid().equals(currentUserId)) {
                                suggestionList.add(user);
                            }
                        }
                        suggestionAdapter.notifyDataSetChanged();
                    }
                });
    }

    // --- Send Friend Request ---
    private void sendFriendRequest(User targetUser) {
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
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- Accept Friend Request ---
    private void acceptFriendRequest(FriendRequest request, int position) {
        db.collection("relationships")
                .document(request.getRequestId())
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã chấp nhận yêu cầu", Toast.LENGTH_SHORT).show();
                    requestList.remove(position);
                    requestAdapter.setRequests(requestList);
                    updateContinueButtonState();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- Decline Friend Request ---
    private void declineFriendRequest(FriendRequest request, int position) {
        db.collection("relationships")
                .document(request.getRequestId())
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã từ chối yêu cầu", Toast.LENGTH_SHORT).show();
                    requestList.remove(position);
                    requestAdapter.setRequests(requestList);
                    updateContinueButtonState();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
