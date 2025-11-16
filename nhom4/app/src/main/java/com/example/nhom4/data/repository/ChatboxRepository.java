package com.example.nhom4.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.nhom4.data.model.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class ChatboxRepository {
    private DatabaseReference messagesRef;
    private ChildEventListener messageListener;

    public ChatboxRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
    }

    // Gửi tin nhắn
    public void sendMessage(Message message, OnMessageSentListener listener) {
        String chatRoomId = getChatRoomId(message.getSenderId(), message.getReceiverId());
        DatabaseReference chatRef = messagesRef.child(chatRoomId);

        String messageId = chatRef.push().getKey();
        if (messageId != null) {
            message.setMessageId(messageId);
            chatRef.child(messageId).setValue(message.toMap())
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    // Lắng nghe tin nhắn realtime
    public LiveData<List<Message>> getMessages(String userId, String receiverId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        List<Message> messageList = new ArrayList<>();

        String chatRoomId = getChatRoomId(userId, receiverId);
        DatabaseReference chatRef = messagesRef.child(chatRoomId);

        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messagesLiveData.setValue(new ArrayList<>(messageList));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Message updatedMessage = snapshot.getValue(Message.class);
                if (updatedMessage != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getMessageId().equals(updatedMessage.getMessageId())) {
                            messageList.set(i, updatedMessage);
                            messagesLiveData.setValue(new ArrayList<>(messageList));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                Message removedMessage = snapshot.getValue(Message.class);
                if (removedMessage != null) {
                    messageList.removeIf(msg ->
                            msg.getMessageId().equals(removedMessage.getMessageId()));
                    messagesLiveData.setValue(new ArrayList<>(messageList));
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi
            }
        };

        chatRef.orderByChild("timestamp").addChildEventListener(messageListener);
        return messagesLiveData;
    }

    // Tạo chatRoomId duy nhất cho 2 người
    private String getChatRoomId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

//     Đánh dấu đã đọc
    public void markAsRead(String userId, String receiverId, String messageId) {
        String chatRoomId = getChatRoomId(userId, receiverId);
        messagesRef.child(chatRoomId).child(messageId).child("isRead").setValue(true);
    }

    // Xóa listener khi không dùng
    public void removeListener(String userId, String receiverId) {
        if (messageListener != null) {
            String chatRoomId = getChatRoomId(userId, receiverId);
            messagesRef.child(chatRoomId).removeEventListener(messageListener);
        }
    }

    // Interface callback
    public interface OnMessageSentListener {
        void onSuccess();
        void onFailure(String error);
    }
}