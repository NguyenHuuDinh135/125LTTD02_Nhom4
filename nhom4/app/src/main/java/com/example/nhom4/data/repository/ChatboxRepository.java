package com.example.nhom4.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.nhom4.data.bean.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot; // Sửa import

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap; // Dùng cho listener an toàn hơn

public class ChatboxRepository {

    private final FirebaseFirestore firestore;
    // Sử dụng Map để quản lý nhiều listener cho nhiều cuộc trò chuyện khác nhau
    private final Map<String, ListenerRegistration> listenerMap = new ConcurrentHashMap<>();

    public ChatboxRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Gửi tin nhắn vào Firestore
    public void sendMessage(
            String conversationId,
            Message message,
            OnMessageSentListener listener
    ) {
        CollectionReference messagesRef = firestore
                .collection("conversations")
                .document(conversationId)
                .collection("messages");

        // Tạo ID cho message
        String messageId = messagesRef.document().getId();
        message.setMessageId(messageId);

        messagesRef.document(messageId)
                .set(message)
                .addOnSuccessListener(aVoid -> {
                    // Update conversation info
                    updateConversationLastMessage(conversationId, message);

                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    // Update lastMessage
    private void updateConversationLastMessage(String conversationId, Message message) {

        DocumentReference convRef = firestore
                .collection("conversations")
                .document(conversationId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message.getContent());
        updates.put("lastSenderId", message.getSenderId());
        updates.put("lastMessageAt", message.getCreatedAt());

        convRef.update(updates);
    }


    // Lắng nghe tin nhắn realtime từ Firestore
    public LiveData<List<Message>> getMessages(String conversationId) {
        MutableLiveData<List<Message>> liveData = new MutableLiveData<>();
        CollectionReference messagesRef = firestore
                .collection("conversations")
                .document(conversationId)
                .collection("messages");

        if (listenerMap.containsKey(conversationId)) {
            listenerMap.get(conversationId).remove();
        }

        ListenerRegistration registration = messagesRef
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value == null) return;

                    List<Message> messageList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.exists()) {
                            Message m = doc.toObject(Message.class);
                            messageList.add(m);
                        }
                    }
                    liveData.postValue(messageList); // Gửi dữ liệu qua LiveData
                });

        // Lưu listener mới vào map
        listenerMap.put(conversationId, registration);

        return liveData;
    }

    public void markAsRead(String conversationId, String messageId, String userId) {
        // Cần triển khai logic đánh dấu đã đọc ở đây
        // Ví dụ: cập nhật một trường trong document của tin nhắn
        // firestore.collection("conversations").document(conversationId)
        //          .collection("messages").document(messageId)
        //          .update("readBy." + userId, true);
    }

    // Xóa listener khi thoát khỏi Activity
    public void removeListener(String conversationId) {
        if (conversationId != null && listenerMap.containsKey(conversationId)) {
            listenerMap.get(conversationId).remove();
            listenerMap.remove(conversationId);
        }
    }


    // Interface callback (Giữ nguyên)
    public interface OnMessageSentListener {
        void onSuccess();
        void onFailure(String error);
    }
}
