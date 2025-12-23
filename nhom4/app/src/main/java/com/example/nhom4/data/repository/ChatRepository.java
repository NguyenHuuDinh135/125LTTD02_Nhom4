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
    private ListenerRegistration relationshipListener; // THÊM: Để lắng nghe realtime bạn bè

    /**
     * LOAD DANH SÁCH HỘI THOẠI (REALTIME + FULL FRIENDS)
     */
    public void loadUserConversations(String currentUserId, MutableLiveData<Resource<List<Conversation>>> result) {
        // Hủy listener cũ nếu có
        if (conversationListener != null) {
            conversationListener.remove();
        }
        if (relationshipListener != null) {
            relationshipListener.remove();
        }

        result.postValue(Resource.loading(null));

        // BƯỚC 1: LẮNG NGHE REALTIME DANH SÁCH BẠN BÈ (status=accepted)
        relationshipListener = db.collection("relationships")
                .whereArrayContains("members", currentUserId)
                .whereEqualTo("status", "accepted")
                .addSnapshotListener((relSnaps, relError) -> {
                    if (relError != null) {
                        result.postValue(Resource.error(relError.getMessage(), null));
                        return;
                    }

                    List<String> friendIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : relSnaps) {
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

                    // BƯỚC 2: LOAD THÔNG TIN BẠN BÈ (REALTIME)
                    db.collection("users")
                            .whereIn("uid", friendIds)
                            .addSnapshotListener((userSnaps, userError) -> {
                                if (userError != null) {
                                    result.postValue(Resource.error(userError.getMessage(), null));
                                    return;
                                }

                                Map<String, User> friendMap = new HashMap<>();
                                for (QueryDocumentSnapshot doc : userSnaps) {
                                    User friend = doc.toObject(User.class);
                                    if (friend != null) {
                                        friendMap.put(friend.getUid(), friend);
                                    }
                                }

                                // BƯỚC 3: LẮNG NGHE REALTIME DANH SÁCH CONVERSATION CỦA TÔI
                                conversationListener = db.collection("conversations")
                                        .whereArrayContains("members", currentUserId)
                                        .addSnapshotListener((convSnaps, convError) -> {
                                            if (convError != null) {
                                                result.postValue(Resource.error(convError.getMessage(), null));
                                                return;
                                            }

                                            List<Conversation> conversations = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : convSnaps) {
                                                Conversation conv = doc.toObject(Conversation.class);
                                                if (conv != null) {
                                                    conv.setConversationId(doc.getId());

                                                    // Tìm ID bạn từ mảng members (loại trừ chính mình)
                                                    for (String memberId : conv.getMembers()) {
                                                        if (!memberId.equals(currentUserId)) {
                                                            User friend = friendMap.get(memberId);
                                                            if (friend != null) {
                                                                conv.setFriendId(memberId);
                                                                conv.setFriendName(friend.getUsername());
                                                                conv.setFriendAvatar(friend.getProfilePhotoUrl());
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    conversations.add(conv);
                                                }
                                            }

                                            // Sắp xếp theo thời gian lastMessageAt mới nhất (DESC)
                                            conversations.sort((c1, c2) -> Long.compare(c2.getTimestampLong(), c1.getTimestampLong()));

                                            result.postValue(Resource.success(conversations));
                                        });
                            });
                });
    }

    // [MỚI] Tìm hoặc tạo Conversation ID (Cho chat 1-1)
    public void findOrCreateConversation(String userId1, String userId2, MutableLiveData<Resource<String>> result) {
        result.postValue(Resource.loading(null));

        String[] sortedIds = {userId1, userId2};
        Arrays.sort(sortedIds);
        String convKey = sortedIds[0] + "_" + sortedIds[1];

        // Tìm conversation hiện có
        db.collection("conversations")
                .whereArrayContains("members", userId1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        List<String> members = (List<String>) doc.get("members");
                        if (members != null && members.contains(userId2)) {
                            result.postValue(Resource.success(doc.getId()));
                            return;
                        }
                    }

                    // Không tìm thấy -> Tạo mới
                    Map<String, Object> newConv = new HashMap<>();
                    newConv.put("members", Arrays.asList(userId1, userId2));
                    newConv.put("createdAt", FieldValue.serverTimestamp());
                    newConv.put("lastMessage", null);
                    newConv.put("lastMessageAt", null);
                    newConv.put("lastSenderId", null);

                    db.collection("conversations")
                            .add(newConv)
                            .addOnSuccessListener(ref -> result.postValue(Resource.success(ref.getId())))
                            .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // Gửi tin nhắn + Update lastMessage
    public void sendMessage(String currentUserId, String conversationId, Message message, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        // Gửi tin nhắn vào sub-collection "messages"
        db.collection("conversations").document(conversationId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(ref -> {
                    // Update lastMessage
                    updateLastMessage(currentUserId, conversationId, message.getContent());
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
                        List<Message> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            Message msg = doc.toObject(Message.class);
                            if (msg != null) {
                                msg.setMessageId(doc.getId()); // <--- Gán ID thật từ Firestore vào object
                                messages.add(msg);
                            }
                        }
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