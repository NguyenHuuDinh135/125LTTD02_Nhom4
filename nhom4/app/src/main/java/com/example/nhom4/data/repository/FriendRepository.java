package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

/**
 * FriendRepository
 * --------------------------------------------------
 * Class này quản lý các tương tác kết bạn giữa các User:
 * 1. Gợi ý kết bạn (Lấy danh sách người dùng).
 * 2. Gửi lời mời kết bạn (Tạo quan hệ mới).
 * 3. Xem danh sách lời mời đang chờ (Realtime).
 * 4. Phản hồi lời mời (Chấp nhận / Từ chối).
 */
public class FriendRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListenerRegistration relationshipListener;
    private ListenerRegistration userListener;
    private ListenerRegistration pendingListener;

    // 1. Lấy danh sách User gợi ý (Đã lọc bạn bè) - REALTIME
    public void getUsersToConnect(String currentUserId, MutableLiveData<Resource<List<User>>> result) {
        result.postValue(Resource.loading(null));

        // Hủy listener cũ nếu có
        if (relationshipListener != null) {
            relationshipListener.remove();
        }
        if (userListener != null) {
            userListener.remove();
        }

        // BƯỚC 1: LẮNG NGHE REALTIME DANH SÁCH MỐI QUAN HỆ (để biết bạn bè/pending để loại trừ)
        relationshipListener = db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .addSnapshotListener((relationshipSnapshots, error) -> {
                    if (error != null) {
                        result.postValue(Resource.error(error.getMessage(), null));
                        return;
                    }

                    // Tạo danh sách ID cần loại trừ: bạn bè (accepted) + pending + chính mình
                    List<String> excludeIds = new ArrayList<>();
                    excludeIds.add(currentUserId); // Loại chính mình

                    for (QueryDocumentSnapshot doc : relationshipSnapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                if (!memberId.equals(currentUserId)) {
                                    excludeIds.add(memberId); // Loại tất cả bạn bè và pending
                                }
                            }
                        }
                    }

                    // BƯỚC 2: LẮNG NGHE REALTIME DANH SÁCH USER (loại trừ excludeIds)
                    if (userListener != null) {
                        userListener.remove();
                    }
                    userListener = db.collection("users")
                            .addSnapshotListener((userSnapshots, userError) -> {
                                if (userError != null) {
                                    result.postValue(Resource.error(userError.getMessage(), null));
                                    return;
                                }

                                List<User> users = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : userSnapshots) {
                                    User user = doc.toObject(User.class);
                                    if (user != null && user.getUid() != null && !excludeIds.contains(user.getUid())) {
                                        users.add(user);
                                    }
                                }

                                // Giới hạn 50 user đầu tiên (nếu cần)
                                if (users.size() > 50) {
                                    users = users.subList(0, 50);
                                }

                                result.postValue(Resource.success(users));
                            });
                });
    }

    // 2. Gửi lời mời kết bạn
    public void sendFriendRequest(String senderId, String receiverId, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        Map<String, Object> relationship = new HashMap<>();
        relationship.put("members", Arrays.asList(senderId, receiverId));
        relationship.put("senderId", senderId);
        relationship.put("receiverId", receiverId);
        relationship.put("status", "pending");
        relationship.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("relationships")
                .add(relationship)
                .addOnSuccessListener(ref -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // 3. Lấy danh sách lời mời đang chờ (REALTIME)
    public void getPendingRequests(String currentUserId, MutableLiveData<Resource<List<FriendRequest>>> result) {
        result.postValue(Resource.loading(null));

        // Hủy listener cũ nếu có
        if (pendingListener != null) {
            pendingListener.remove();
        }

        pendingListener = db.collection("relationships")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        result.postValue(Resource.error(e.getMessage(), null));
                        return;
                    }

                    List<FriendRequest> requests = new ArrayList<>();
                    List<Task<User>> userTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        FriendRequest req = doc.toObject(FriendRequest.class);
                        if (req != null) {
                            req.setRequestId(doc.getId());

                            // Load sender info
                            if (req.getSenderId() != null) {
                                Task<User> userTask = db.collection("users").document(req.getSenderId())
                                        .get()
                                        .continueWith(task -> {
                                            if (task.isSuccessful() && task.getResult() != null) {
                                                return task.getResult().toObject(User.class);
                                            }
                                            return null;
                                        });
                                userTasks.add(userTask);
                            }
                            requests.add(req);
                        }
                    }

                    // Đợi tất cả userTasks hoàn thành
                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(users -> {
                        for (int i = 0; i < requests.size(); i++) {
                            User sender = (User) users.get(i);
                            if (sender != null) {
                                requests.get(i).setSender(sender);
                            }
                        }
                        result.postValue(Resource.success(requests));
                    }).addOnFailureListener(error -> result.postValue(Resource.error(error.getMessage(), null)));
                });
    }

    // 4. Phản hồi lời mời - phương thức chung (được gọi bởi accept/decline)
    public void respondToRequest(String requestId, String status, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        Map<String, Object> update = new HashMap<>();
        update.put("status", status);
        update.put("updatedAt", com.google.firebase.Timestamp.now());

        db.collection("relationships").document(requestId)
                .update(update)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // 5. Chấp nhận lời mời (gọi respondToRequest với status = "accepted")
    public void acceptFriendRequest(String currentUserId, String senderId, MutableLiveData<Resource<Boolean>> result) {
        // Tìm requestId tương ứng với senderId và currentUserId (receiver)
        db.collection("relationships")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        String requestId = doc.getId();
                        respondToRequest(requestId, "accepted", result);
                    } else {
                        result.postValue(Resource.error("Không tìm thấy lời mời kết bạn", false));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // 6. Từ chối lời mời (gọi respondToRequest với status = "declined")
    public void declineFriendRequest(String currentUserId, String senderId, MutableLiveData<Resource<Boolean>> result) {
        // Tìm requestId tương ứng
        db.collection("relationships")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        String requestId = doc.getId();
                        respondToRequest(requestId, "declined", result);
                    } else {
                        result.postValue(Resource.error("Không tìm thấy lời mời kết bạn", false));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // 7. Xóa bạn
    public void unfriendUser(String currentUserId, String targetUserId, MutableLiveData<Resource<Boolean>> result) {
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    String relationshipId = null;
                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null && members.contains(targetUserId)) {
                            relationshipId = doc.getId();
                            break;
                        }
                    }

                    if (relationshipId != null) {
                        db.collection("relationships").document(relationshipId)
                                .delete()
                                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi xóa bạn: " + e.getMessage(), false)));
                    } else {
                        result.postValue(Resource.success(true));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi tìm bạn: " + e.getMessage(), false)));
    }
}