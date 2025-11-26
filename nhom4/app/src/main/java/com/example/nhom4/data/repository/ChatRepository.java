package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.bean.Message; // Import đúng Bean của bạn
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

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Load danh sách chat (CHỈ HIỂN THỊ BẠN BÈ)
    public void loadUserConversations(String currentUserId, MutableLiveData<Resource<List<Conversation>>> result) {
        result.postValue(Resource.loading(null));

        // BƯỚC 1: Lấy danh sách ID bạn bè (status = 'accepted')
        db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(relSnaps -> {
                    List<String> friendIds = new ArrayList<>();
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

                    // Nếu không có bạn bè nào
                    if (friendIds.isEmpty()) {
                        result.postValue(Resource.success(new ArrayList<>()));
                        return;
                    }

                    // BƯỚC 2: Lấy thông tin User của những người bạn này
                    // (Lấy toàn bộ users rồi lọc ID nằm trong friendIds để đảm bảo performance tốt hơn query 'in' nếu list dài)
                    db.collection("users").get().addOnSuccessListener(userSnaps -> {
                        List<User> friendUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : userSnaps) {
                            if (friendIds.contains(doc.getId())) {
                                User user = doc.toObject(User.class);
                                user.setUid(doc.getId());
                                friendUsers.add(user);
                            }
                        }

                        // BƯỚC 3: Lấy các cuộc trò chuyện ĐÃ CÓ của mình
                        db.collection("conversations")
                                .whereArrayContains("members", currentUserId)
                                .get()
                                .addOnSuccessListener(convSnaps -> {
                                    Map<String, Conversation> existingMap = new HashMap<>();
                                    for (QueryDocumentSnapshot doc : convSnaps) {
                                        Conversation c = doc.toObject(Conversation.class);
                                        c.setConversationId(doc.getId());
                                        for (String member : c.getMembers()) {
                                            if (!member.equals(currentUserId)) {
                                                existingMap.put(member, c);
                                                break;
                                            }
                                        }
                                    }

                                    // BƯỚC 4: Gộp danh sách (Merge)
                                    List<Conversation> displayList = new ArrayList<>();
                                    for (User friend : friendUsers) {
                                        if (existingMap.containsKey(friend.getUid())) {
                                            // A: Đã từng nhắn -> Lấy hội thoại cũ
                                            displayList.add(existingMap.get(friend.getUid()));
                                        } else {
                                            // B: Chưa nhắn -> Tạo hội thoại "ảo" để hiển thị
                                            Conversation fake = new Conversation();
                                            fake.setMembers(Arrays.asList(currentUserId, friend.getUid()));
                                            fake.setLastMessage("Bắt đầu trò chuyện ngay");
                                            displayList.add(fake);
                                        }
                                    }

                                    // Trả về kết quả
                                    result.postValue(Resource.success(displayList));
                                })
                                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load chat: " + e.getMessage(), null)));

                    }).addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load users: " + e.getMessage(), null)));

                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load friends: " + e.getMessage(), null)));
    }

    // 1. Tìm kiếm hoặc Tạo mới Conversation
    public void findOrCreateConversation(String currentUserId, String targetUserId, MutableLiveData<Resource<String>> result) {
        db.collection("conversations")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    String foundId = null;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null && members.contains(targetUserId)) {
                            foundId = doc.getId();
                            break;
                        }
                    }

                    if (foundId != null) {
                        result.postValue(Resource.success(foundId));
                    } else {
                        createConversation(currentUserId, targetUserId, result);
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

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
    public void sendMessage(String conversationId, Message message, MutableLiveData<Resource<Boolean>> result) {
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> {
                    String preview = "post_reply".equals(message.getType()) ? "Đã phản hồi bài viết" : message.getContent();
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

    // 3. Lắng nghe tin nhắn Realtime
    public void getMessages(String conversationId, MutableLiveData<Resource<List<Message>>> result) {
        result.postValue(Resource.loading(null));
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
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
}
