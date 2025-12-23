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

    // 1. Lấy danh sách User gợi ý
    // Logic: Lấy 50 user đầu tiên tìm thấy trong database (trừ bản thân).
    // 1. Lấy danh sách User gợi ý (Đã lọc bạn bè)
    public void getUsersToConnect(String currentUserId, MutableLiveData<Resource<List<User>>> result) {
        result.postValue(Resource.loading(null));

        // BƯỚC 1: Lấy danh sách tất cả các mối quan hệ của tôi (để biết ai cần loại trừ)
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(relationshipSnapshots -> {
                    // Tạo danh sách các ID cần loại trừ (bao gồm chính mình)
                    List<String> excludeIds = new ArrayList<>();
                    excludeIds.add(currentUserId);

                    for (DocumentSnapshot doc : relationshipSnapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                // Nếu ID trong mối quan hệ không phải là tôi, thì đó là bạn (hoặc người đang chờ duyệt)
                                if (!memberId.equals(currentUserId)) {
                                    excludeIds.add(memberId);
                                }
                            }
                        }
                    }

                    // BƯỚC 2: Lấy danh sách Users và lọc
                    // Lưu ý: Tăng limit lên một chút vì sau khi lọc số lượng có thể giảm đi
                    db.collection("users").limit(100).get()
                            .addOnSuccessListener(userSnapshots -> {
                                List<User> users = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : userSnapshots) {
                                    User user = doc.toObject(User.class);
                                    user.setUid(doc.getId());

                                    // QUAN TRỌNG: Chỉ thêm user nếu ID của họ KHÔNG nằm trong danh sách loại trừ
                                    if (user.getUid() != null && !excludeIds.contains(user.getUid())) {
                                        users.add(user);
                                    }
                                }
                                result.postValue(Resource.success(users));
                            })
                            .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy user: " + e.getMessage(), null)));

                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy danh sách bạn: " + e.getMessage(), null)));
    }

    // 2. Gửi lời mời kết bạn
    // Hành động: Tạo một document mới trong collection "relationships" với trạng thái 'pending'.
    public void sendFriendRequest(String currentUserId, String targetUserId, MutableLiveData<Resource<Boolean>> result) {
        Map<String, Object> relationship = new HashMap<>();

        // Mảng "members" giúp query các mối quan hệ của 1 user bất kỳ dễ dàng hơn (array-contains)
        relationship.put("members", Arrays.asList(currentUserId, targetUserId));
        relationship.put("requesterId", currentUserId); // Người gửi lời mời
        relationship.put("recipientId", targetUserId);  // Người nhận lời mời
        relationship.put("status", "pending");          // Trạng thái ban đầu: Đang chờ
        relationship.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("relationships").add(relationship)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    // --- PHẦN MỚI CHO FRIENDS BOTTOM SHEET ---

    /**
     * 3. Lấy danh sách lời mời kết bạn đang chờ (Pending Requests)
     * ĐẶC BIỆT: Sử dụng addSnapshotListener thay vì get().
     * Tác dụng: Tạo kết nối Realtime. Khi có ai đó gửi lời mời, danh sách này tự động cập nhật ngay lập tức.
     */
    public void getPendingRequests(String currentUserId, MutableLiveData<Resource<List<FriendRequest>>> result) {
        result.postValue(Resource.loading(null));

        // Query: Tìm trong bảng relationships, những dòng mà TÔI là người nhận (recipientId == me)
        // và trạng thái là đang chờ (pending).
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
                                // Lưu ID của request (relationship ID) để dùng cho việc Accept/Deny sau này
                                fr.setRequestId(doc.getId());
                                list.add(fr);
                            }
                        }
                        result.postValue(Resource.success(list));
                    }
                });
    }

    // 4. Phản hồi lời mời (Chấp nhận / Từ chối)
    // Logic: Chỉ cần update trường "status" của document relationship tương ứng.
    // newStatus sẽ là "accepted" (Chấp nhận) hoặc "declined" (Từ chối).
    public void respondToRequest(String requestId, String newStatus, MutableLiveData<Resource<Boolean>> result) {
        db.collection("relationships").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    public void unfriendUser(String currentUserId, String targetUserId, MutableLiveData<Resource<Boolean>> result) {
        // Tìm document trong collection "relationships" có chứa cả 2 user trong mảng "members"
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    String relationshipId = null;

                    // Lọc thủ công để tìm document chứa cả targetUserId
                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null && members.contains(targetUserId)) {
                            relationshipId = doc.getId();
                            break;
                        }
                    }

                    if (relationshipId != null) {
                        // Tìm thấy -> Xóa document
                        db.collection("relationships").document(relationshipId)
                                .delete()
                                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi xóa bạn: " + e.getMessage(), false)));
                    } else {
                        // Không tìm thấy quan hệ (có thể đã xóa trước đó) -> Coi như thành công
                        result.postValue(Resource.success(true));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi tìm bạn: " + e.getMessage(), false)));
    }
}