package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

public class FriendRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ... (Giữ nguyên code cũ getUsersToConnect và sendFriendRequest) ...
    // Nếu bạn chưa có file này từ bước trước, hãy copy lại phần cũ và thêm phần mới dưới đây:

    // 1. Lấy danh sách User gợi ý (Logic cũ)
    public void getUsersToConnect(String currentUserId, MutableLiveData<Resource<List<User>>> result) {
        result.postValue(Resource.loading(null));
        db.collection("users").limit(50).get()
                .addOnSuccessListener(snapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        User user = doc.toObject(User.class);
                        user.setUid(doc.getId()); // Quan trọng: set ID
                        if (user.getUid() != null && !user.getUid().equals(currentUserId)) {
                            users.add(user);
                        }
                    }
                    result.postValue(Resource.success(users));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // 2. Gửi lời mời kết bạn (Logic cũ)
    public void sendFriendRequest(String currentUserId, String targetUserId, MutableLiveData<Resource<Boolean>> result) {
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("members", Arrays.asList(currentUserId, targetUserId));
        relationship.put("requesterId", currentUserId);
        relationship.put("recipientId", targetUserId);
        relationship.put("status", "pending");
        relationship.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("relationships").add(relationship)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // --- PHẦN MỚI CHO FRIENDS BOTTOM SHEET ---

    // 3. Lấy danh sách lời mời kết bạn đang chờ (Pending Requests)
    public void getPendingRequests(String currentUserId, MutableLiveData<Resource<List<FriendRequest>>> result) {
        result.postValue(Resource.loading(null));

        db.collection("relationships")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> { // Dùng Realtime Listener
                    if (e != null) {
                        result.postValue(Resource.error(e.getMessage(), null));
                        return;
                    }
                    if (snapshots != null) {
                        List<FriendRequest> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            FriendRequest fr = doc.toObject(FriendRequest.class);
                            if (fr != null) {
                                fr.setRequestId(doc.getId());
                                list.add(fr);
                            }
                        }
                        result.postValue(Resource.success(list));
                    }
                });
    }

    // 4. Phản hồi lời mời (Chấp nhận / Từ chối)
    public void respondToRequest(String requestId, String newStatus, MutableLiveData<Resource<Boolean>> result) {
        db.collection("relationships").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }
}
