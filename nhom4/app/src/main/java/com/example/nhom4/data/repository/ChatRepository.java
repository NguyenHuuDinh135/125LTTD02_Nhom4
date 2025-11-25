package com.example.nhom4.data.repository;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.bean.Message; // <--- CORRECT IMPORT
import com.example.nhom4.data.bean.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // <--- CORRECT IMPORT FOR FIRESTORE
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadUserConversations(String currentUserId, MutableLiveData<Resource<List<Conversation>>> result) {
        // Logic phức tạp của DiscoveryFragment được chuyển về đây
        result.postValue(Resource.loading(null));

        db.collection("users").get().addOnSuccessListener(userSnaps -> {
            List<User> allUsers = new ArrayList<>();
            for (QueryDocumentSnapshot doc : userSnaps) {
                User user = doc.toObject(User.class);
                if (user.getUid() != null && !user.getUid().equals(currentUserId)) {
                    allUsers.add(user);
                }
            }

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

                        List<Conversation> displayList = new ArrayList<>();
                        for (User user : allUsers) {
                            if (existingMap.containsKey(user.getUid())) {
                                displayList.add(existingMap.get(user.getUid()));
                            } else {
                                Conversation fake = new Conversation();
                                fake.setMembers(Arrays.asList(currentUserId, user.getUid()));
                                fake.setLastMessage("Bắt đầu trò chuyện ngay");
                                displayList.add(fake);
                            }
                        }
                        result.postValue(Resource.success(displayList));
                    })
                    .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
        }).addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
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

    // 2. Gửi tin nhắn (Sửa lại kiểu tham số Message cho đúng)
    public void sendMessage(String conversationId, Message message, MutableLiveData<Resource<Boolean>> result) {
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> {
                    // Update lastMessage cho Conversation cha
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

    // [MỚI] Lắng nghe tin nhắn Realtime
    public void getMessages(String conversationId, MutableLiveData<Resource<List<Message>>> result) {
        result.postValue(Resource.loading(null));

        db.collection("conversations").document(conversationId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING) // <-- FIXED: Using Firebase Query Direction
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
