package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
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
 * Quản lý toàn bộ logic kết bạn và tạo chat tự động.
 */
public class FriendRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListenerRegistration relationshipListener;
    private ListenerRegistration userListener;
    private ListenerRegistration pendingListener;

    // ====================================================================================
    // 1. Lấy danh sách gợi ý (User chưa kết bạn)
    // ====================================================================================
    public void getUsersToConnect(String currentUserId, MutableLiveData<Resource<List<User>>> result) {
        result.postValue(Resource.loading(null));

        // Hủy listener cũ để tránh leak
        if (relationshipListener != null) relationshipListener.remove();
        if (userListener != null) userListener.remove();

        // Bước 1: Lắng nghe bảng relationships để lọc ra những người đã là bạn hoặc đang chờ
        relationshipListener = db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .addSnapshotListener((relationshipSnapshots, error) -> {
                    if (error != null) {
                        result.postValue(Resource.error(error.getMessage(), null));
                        return;
                    }

                    List<String> excludeIds = new ArrayList<>();
                    excludeIds.add(currentUserId); // Loại bỏ chính mình

                    for (QueryDocumentSnapshot doc : relationshipSnapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                if (!memberId.equals(currentUserId)) {
                                    excludeIds.add(memberId); // Loại bỏ người đã tương tác
                                }
                            }
                        }
                    }

                    // Bước 2: Lắng nghe bảng users và loại trừ danh sách trên
                    if (userListener != null) userListener.remove();
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

                                if (users.size() > 50) {
                                    users = users.subList(0, 50);
                                }

                                result.postValue(Resource.success(users));
                            });
                });
    }

    // ====================================================================================
    // 2. Gửi lời mời kết bạn
    // ====================================================================================
    public void sendFriendRequest(String senderId, String receiverId, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        Map<String, Object> relationship = new HashMap<>();
        relationship.put("members", Arrays.asList(senderId, receiverId));
        relationship.put("senderId", senderId);
        relationship.put("receiverId", receiverId);
        relationship.put("status", "pending");
        relationship.put("createdAt", Timestamp.now());

        db.collection("relationships")
                .add(relationship)
                .addOnSuccessListener(ref -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // ====================================================================================
    // 3. Lấy danh sách lời mời đang chờ (Pending Requests)
    // ====================================================================================
    public void getPendingRequests(String currentUserId, MutableLiveData<Resource<List<FriendRequest>>> result) {
        result.postValue(Resource.loading(null));

        if (pendingListener != null) pendingListener.remove();

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

                            // Load thông tin người gửi (Sender Info)
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

                    // Đợi load xong info user mới trả về kết quả
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

    // ====================================================================================
    // 4. Chấp nhận lời mời kết bạn -> TỰ ĐỘNG TẠO CHAT
    // ====================================================================================
    public void acceptFriendRequest(String currentUserId, String senderId, MutableLiveData<Resource<Boolean>> result) {
        // Tìm document relationship đang pending
        db.collection("relationships")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        String requestId = doc.getId();

                        // 1. Cập nhật status -> accepted
                        db.collection("relationships").document(requestId)
                                .update("status", "accepted", "updatedAt", Timestamp.now())
                                .addOnSuccessListener(aVoid -> {
                                    // 2. QUAN TRỌNG: Tạo conversation ngay sau khi accept thành công
                                    createConversation(currentUserId, senderId, result);
                                })
                                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
                    } else {
                        result.postValue(Resource.error("Không tìm thấy lời mời kết bạn", false));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    /**
     * Hàm helper: Tạo document chat mới trong collection "conversations".
     * Được gọi sau khi acceptFriendRequest thành công.
     */
    private void createConversation(String user1, String user2, MutableLiveData<Resource<Boolean>> result) {
        // Kiểm tra xem đã tồn tại chat giữa 2 người này chưa
        db.collection("conversations")
                .whereArrayContains("members", user1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean exists = false;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null && members.contains(user2)) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        // Đã có chat -> Báo thành công luôn
                        result.postValue(Resource.success(true));
                    } else {
                        // Chưa có chat -> Tạo mới
                        Map<String, Object> chat = new HashMap<>();
                        chat.put("members", Arrays.asList(user1, user2));
                        chat.put("lastMessage", "Các bạn đã trở thành bạn bè 👋"); // Tin nhắn hệ thống đầu tiên
                        chat.put("lastMessageTime", Timestamp.now());
                        chat.put("createdAt", Timestamp.now());
                        chat.put("createdBy", user1);

                        db.collection("conversations")
                                .add(chat)
                                .addOnSuccessListener(ref -> result.postValue(Resource.success(true))) // Thành công hoàn toàn
                                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi tạo chat: " + e.getMessage(), false)));
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi query check thì vẫn cố tạo hoặc báo lỗi (ở đây chọn báo lỗi an toàn)
                    result.postValue(Resource.error("Lỗi kiểm tra chat: " + e.getMessage(), false));
                });
    }

    // ====================================================================================
    // 5. Từ chối lời mời
    // ====================================================================================
    public void declineFriendRequest(String currentUserId, String senderId, MutableLiveData<Resource<Boolean>> result) {
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
                        result.postValue(Resource.error("Không tìm thấy lời mời", false));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    private void respondToRequest(String requestId, String status, MutableLiveData<Resource<Boolean>> result) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", status);
        update.put("updatedAt", Timestamp.now());

        db.collection("relationships").document(requestId)
                .update(update)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // ====================================================================================
    // 6. Xóa bạn bè (Unfriend)
    // ====================================================================================
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
                        // Không tìm thấy relationship -> coi như đã xóa
                        result.postValue(Resource.success(true));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi tìm bạn: " + e.getMessage(), false)));
    }
}