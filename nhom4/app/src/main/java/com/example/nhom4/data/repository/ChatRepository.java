package com.example.nhom4.data.repository;

import android.util.Log;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.bean.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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

    // Giữ reference để hủy lắng nghe khi cần
    private ListenerRegistration conversationListener;

    /**
     * LOAD DANH SÁCH HỘI THOẠI (REALTIME + FULL FRIENDS)
     */
    public void loadUserConversations(String currentUserId, MutableLiveData<Resource<List<Conversation>>> result) {
        // Hủy listener cũ nếu có
        if (conversationListener != null) {
            conversationListener.remove();
        }

        result.postValue(Resource.loading(null));

        // BƯỚC 1: LẤY DANH SÁCH BẠN BÈ (Lấy 1 lần)
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
                                if (!memberId.equals(currentUserId)) friendIds.add(memberId);
                            }
                        }
                    }

                    if (friendIds.isEmpty()) {
                        result.postValue(Resource.success(new ArrayList<>()));
                        return;
                    }

                    // Lấy thông tin chi tiết (Avatar, Tên) của bạn bè
                    fetchFriendsInfoAndListenToChat(currentUserId, friendIds, result);
                })
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi lấy bạn bè: " + e.getMessage(), null)));
    }

    private void fetchFriendsInfoAndListenToChat(String currentUserId, List<String> friendIds, MutableLiveData<Resource<List<Conversation>>> result) {
        // Chia nhỏ list nếu quá 10 người (Firestore limit), ở đây làm đơn giản cho <10
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String fid : friendIds) {
            tasks.add(db.collection("users").document(fid).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            List<User> friendsList = new ArrayList<>();
            for (Object obj : objects) {
                DocumentSnapshot doc = (DocumentSnapshot) obj;
                if (doc.exists()) {
                    User u = doc.toObject(User.class);
                    u.setUid(doc.getId());
                    friendsList.add(u);
                }
            }

            // BƯỚC 2 & 3: LẮNG NGHE CONVERSATION REALTIME VÀ GỘP
            startListeningToConversations(currentUserId, friendsList, result);
        });
    }

    private void startListeningToConversations(String currentUserId, List<User> friendsList, MutableLiveData<Resource<List<Conversation>>> result) {
        // Lắng nghe bảng conversations REALTIME
        conversationListener = db.collection("conversations")
                .whereArrayContains("members", currentUserId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("ChatRepo", "Listen error", error);
                        return;
                    }

                    Map<String, Conversation> activeChatMap = new HashMap<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            Conversation c = doc.toObject(Conversation.class); // Tự động map field lastMessageAt vào Timestamp
                            c.setConversationId(doc.getId());

                            // Xác định ID đối phương
                            String partnerId = null;
                            if (c.getMembers() != null) {
                                for (String m : c.getMembers()) {
                                    if (!m.equals(currentUserId)) {
                                        partnerId = m;
                                        break;
                                    }
                                }
                            }
                            if (partnerId != null) {
                                activeChatMap.put(partnerId, c);
                            }
                        }
                    }

                    // BƯỚC 4: GỘP DANH SÁCH (MERGE)
                    List<Conversation> finalDisplayList = new ArrayList<>();

                    for (User friend : friendsList) {
                        Conversation displayItem;

                        if (activeChatMap.containsKey(friend.getUid())) {
                            // Đã có tin nhắn -> Dùng conversation thật
                            displayItem = activeChatMap.get(friend.getUid());
                        } else {
                            // Chưa có tin nhắn -> Tạo conversation giả để hiển thị
                            displayItem = new Conversation();
                            displayItem.setMembers(Arrays.asList(currentUserId, friend.getUid()));
                            displayItem.setLastMessage("Bắt đầu trò chuyện ngay");
                        }

                        // Luôn set lại thông tin hiển thị từ User mới nhất
                        displayItem.setFriendId(friend.getUid());
                        displayItem.setFriendName(friend.getUsername());
                        displayItem.setFriendAvatar(friend.getProfilePhotoUrl());

                        finalDisplayList.add(displayItem);
                    }

                    // Sắp xếp: Ai có tin nhắn mới nhất lên đầu
                    finalDisplayList.sort((c1, c2) -> Long.compare(c2.getTimestampLong(), c1.getTimestampLong()));

                    result.postValue(Resource.success(finalDisplayList));
                });
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN (Create, Send Message...) ---

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
        conv.put("createdAt", FieldValue.serverTimestamp());
        conv.put("lastMessage", "Đã gửi một phản hồi");
        conv.put("lastMessageAt", FieldValue.serverTimestamp());
        conv.put("lastSenderId", currentUserId);

        db.collection("conversations").add(conv)
                .addOnSuccessListener(doc -> result.postValue(Resource.success(doc.getId())))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    public void sendMessage(String currentUserId, String conversationId, Message message, MutableLiveData<Resource<Boolean>> result) {
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> {
                    String preview = "post_reply".equals(message.getType()) ? "Đã phản hồi bài viết" : message.getContent();
                    updateLastMessage(currentUserId, conversationId, preview);
                    result.postValue(Resource.success(true));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }

    private void updateLastMessage(String currentUserId, String conversationId, String lastMessage) {
        Map<String, Object> update = new HashMap<>();
        update.put("lastMessage", lastMessage);
        update.put("lastMessageAt", FieldValue.serverTimestamp());
        update.put("lastSenderId", currentUserId);
        db.collection("conversations").document(conversationId).update(update);
    }

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

    public void deleteConversation(String conversationId, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));
        if (conversationId == null || conversationId.isEmpty()) {
            result.postValue(Resource.error("Conversation ID không hợp lệ", false));
            return;
        }
        db.collection("conversations").document(conversationId)
                .delete()
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi khi xóa: " + e.getMessage(), false)));
    }
}