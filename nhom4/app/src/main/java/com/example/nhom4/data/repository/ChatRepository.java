package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.bean.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

/**
 * ChatRepository
 * -----------------------------------------------------------
 * Class này quản lý toàn bộ dữ liệu liên quan đến tính năng Chat:
 * 1. Load danh sách hội thoại (Kết hợp dữ liệu từ 3 bảng).
 * 2. Gửi/Nhận tin nhắn (Realtime).
 * 3. Tạo cuộc hội thoại mới giữa 2 người dùng.
 */
public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * LOAD DANH SÁCH HỘI THOẠI (Logic phức tạp nhất project)
     * Mục tiêu: Hiển thị danh sách bạn bè để chat.
     * - Người đã từng nhắn tin: Hiển thị tin nhắn cuối.
     * - Người chưa từng nhắn tin: Hiển thị dòng "Bắt đầu trò chuyện ngay".
     *
     * Quy trình xử lý (4 Bước lồng nhau):
     * B1: Tìm danh sách ID bạn bè.
     * B2: Lấy thông tin chi tiết (Avatar, Tên) của bạn bè.
     * B3: Lấy các cuộc hội thoại cũ từ DB.
     * B4: Gộp (Merge) lại để tạo danh sách hiển thị.
     */
    public void loadUserConversations(String currentUserId, MutableLiveData<Resource<List<Conversation>>> result) {
        result.postValue(Resource.loading(null));

        // --- BƯỚC 1: Lấy danh sách ID bạn bè (status = 'accepted') ---
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(relSnaps -> {
                    List<String> friendIds = new ArrayList<>();
                    // Lọc ra ID của người kia trong mối quan hệ
                    for (DocumentSnapshot doc : relSnaps) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null) {
                            for (String memberId : members) {
                                if (!memberId.equals(currentUserId)) {
                                    friendIds.add(memberId);
                                }
                            }
                        }
                    }

                    // Nếu không có bạn bè nào -> Trả về list rỗng ngay
                    if (friendIds.isEmpty()) {
                        result.postValue(Resource.success(new ArrayList<>()));
                        return;
                    }

                    // --- BƯỚC 2: Lấy thông tin User (Tên, Avatar) ---
                    // Lưu ý: Hiện tại đang lấy ALL Users rồi lọc (Client-side filter).
                    db.collection("users").get().addOnSuccessListener(userSnaps -> {
                        List<User> friendUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : userSnaps) {
                            // Chỉ lấy User nào nằm trong danh sách bạn bè
                            if (friendIds.contains(doc.getId())) {
                                User user = doc.toObject(User.class);
                                user.setUid(doc.getId());
                                friendUsers.add(user);
                            }
                        }

                        // --- BƯỚC 3: Lấy các cuộc trò chuyện ĐÃ CÓ trong DB ---
                        db.collection("conversations")
                                .whereArrayContains("members", currentUserId)
                                .get()
                                .addOnSuccessListener(convSnaps -> {
                                    // Tạo Map để tra cứu nhanh: Key = ID bạn bè, Value = Conversation
                                    Map<String, Conversation> existingMap = new HashMap<>();
                                    for (QueryDocumentSnapshot doc : convSnaps) {
                                        Conversation c = doc.toObject(Conversation.class);
                                        c.setConversationId(doc.getId());
                                        // Tìm ID người chat cùng mình trong conversation này
                                        for (String member : c.getMembers()) {
                                            if (!member.equals(currentUserId)) {
                                                existingMap.put(member, c);
                                                break;
                                            }
                                        }
                                    }

                                    // --- BƯỚC 4: Gộp danh sách (MERGE LOGIC) ---
                                    List<Conversation> displayList = new ArrayList<>();
                                    for (User friend : friendUsers) {
                                        if (existingMap.containsKey(friend.getUid())) {
                                            // Trường hợp A: Đã từng nhắn tin -> Lấy hội thoại thật từ DB
                                            // (Có tin nhắn cuối, thời gian...)
                                            displayList.add(existingMap.get(friend.getUid()));
                                        } else {
                                            // Trường hợp B: Chưa từng nhắn -> Tạo hội thoại "Ảo" (Fake)
                                            // Để UI vẫn hiển thị người đó cho mình click vào chat
                                            Conversation fake = new Conversation();
                                            fake.setMembers(Arrays.asList(currentUserId, friend.getUid()));
                                            fake.setLastMessage("Bắt đầu trò chuyện ngay"); // Placeholder text
                                            displayList.add(fake);
                                        }
                                    }

                                    // Trả về kết quả cuối cùng cho UI
                                    result.postValue(Resource.success(displayList));
                                })
                                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load chat: " + e.getMessage(), null)));

                    }).addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load users: " + e.getMessage(), null)));

                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load friends: " + e.getMessage(), null)));
    }

    // 1. Tìm kiếm hoặc Tạo mới Conversation
    // Logic: Khi click vào 1 người bạn, kiểm tra xem đã có conversation ID chưa.
    // Nếu có -> Mở ra chat tiếp. Nếu chưa -> Tạo mới.
    public void findOrCreateConversation(String currentUserId, String targetUserId, MutableLiveData<Resource<String>> result) {
        db.collection("conversations")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    String foundId = null;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        // Kiểm tra xem conversation này có chứa người kia không
                        if (members != null && members.contains(targetUserId)) {
                            foundId = doc.getId();
                            break;
                        }
                    }

                    if (foundId != null) {
                        result.postValue(Resource.success(foundId)); // Tìm thấy ID cũ
                    } else {
                        createConversation(currentUserId, targetUserId, result); // Tạo mới
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // Helper: Tạo document Conversation mới lên Firestore
    private void createConversation(String currentUserId, String targetUserId, MutableLiveData<Resource<String>> result) {
        Map<String, Object> conv = new HashMap<>();
        conv.put("members", Arrays.asList(currentUserId, targetUserId));
        conv.put("createdAt", com.google.firebase.Timestamp.now());
        conv.put("lastMessage", "Đã gửi một phản hồi");

        db.collection("conversations").add(conv)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(doc.getId())))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // 2. Gửi tin nhắn
    // Hành động kép: 1. Thêm tin nhắn vào sub-collection -> 2. Update tin nhắn cuối ở Conversation cha
    public void sendMessage(String conversationId, Message message, MutableLiveData<Resource<Boolean>> result) {
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> {
                    // Xử lý preview text (ví dụ nếu reply story thì không hiện nội dung tin nhắn gốc)
                    String preview = "post_reply".equals(message.getType()) ? "Đã phản hồi bài viết" : message.getContent();

                    // Cập nhật lastMessage để danh sách bên ngoài hiển thị đúng
                    updateLastMessage(conversationId, preview);
                    result.postValue(Resource.success(true));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    private void updateLastMessage(String conversationId, String lastMessage) {
        Map<String, Object> update = new HashMap<>();
        update.put("lastMessage", lastMessage);
        update.put("lastMessageAt", com.google.firebase.Timestamp.now());
        db.collection("conversations").document(conversationId).update(update);
    }

    // 3. Lắng nghe tin nhắn Realtime (Quan trọng)
    // Sử dụng addSnapshotListener để tự động cập nhật khi có tin nhắn mới
    public void getMessages(String conversationId, MutableLiveData<Resource<List<Message>>> result) {
        result.postValue(Resource.loading(null));

        db.collection("conversations").document(conversationId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING) // Tin cũ nhất ở trên, mới nhất ở dưới
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        result.postValue(Resource.error(e.getMessage(), null));
                        return;
                    }
                    if (snapshots != null) {
                        List<Message> messages = snapshots.toObjects(Message.class);
                        result.postValue(Resource.success(messages));
                    }
                });
    }

    // 4. Xóa cuộc trò chuyện
    // Lưu ý: Việc xóa document cha trong Firestore KHÔNG tự động xóa sub-collection (messages).
    // Tuy nhiên, xóa document cha là đủ để cuộc trò chuyện biến mất khỏi danh sách UI.
    public void deleteConversation(String conversationId, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        if (conversationId == null || conversationId.isEmpty()) {
            result.postValue(Resource.error("Conversation ID không hợp lệ", false));
            return;
        }

        db.collection("conversations").document(conversationId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Xóa thành công
                result.postValue(Resource.success(true));
            })
            .addOnFailureListener(e -> {
                // Xóa thất bại
                result.postValue(Resource.error("Lỗi khi xóa: " + e.getMessage(), false));
            });
    }

}