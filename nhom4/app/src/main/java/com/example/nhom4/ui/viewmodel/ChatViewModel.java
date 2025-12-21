package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.ChatRepository;

import java.util.List;

/**
 * ViewModel trung gian giữa ChatActivity và ChatRepository:
 * - Lắng nghe tin nhắn realtime
 * - Gửi tin nhắn text và reply post
 */
public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<Message>>> messages = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> sendStatus = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> deleteStatus = new MutableLiveData<>();


    private String currentUserId;

    public ChatViewModel() {
        chatRepository = new ChatRepository();
        authRepository = new AuthRepository();
        if (authRepository.getCurrentUser() != null) {
            currentUserId = authRepository.getCurrentUser().getUid();
        }
    }

    public LiveData<Resource<List<Message>>> getMessages() { return messages; }
    public LiveData<Resource<Boolean>> getSendStatus() { return sendStatus; }
    public LiveData<Resource<Boolean>> getDeleteResult() { return  deleteStatus; }
    public String getCurrentUserId() { return currentUserId; }

    // Bắt đầu lắng nghe tin nhắn khi có ConversationID
    public void startListening(String conversationId) {
        if (conversationId != null && !conversationId.isEmpty()) {
            chatRepository.getMessages(conversationId, messages); // Snapshot listener emit Resource
        }
    }

    // Gửi tin nhắn thường (Text)
    public void sendMessage(String conversationId, String content) {
        if (currentUserId == null || content.trim().isEmpty()) return;

        Message msg = new Message(currentUserId, content.trim(), "text");
        chatRepository.sendMessage(conversationId, msg, sendStatus); // Đẩy trạng thái LOADING/SUCCESS/ERROR
    }

    // [MỚI] Gửi tin nhắn Reply Post (Widget)
    public void sendReplyPost(String conversationId, String content, String postId, String postTitle, String postImage) {
        if (currentUserId == null) return;

        // Gọi Constructor đầy đủ cho Post Reply trong Message.java
        Message msg = new Message(
                currentUserId,
                content.trim(),
                "post_reply",
                postId,
                postImage,
                postTitle
        );

        chatRepository.sendMessage(conversationId, msg, sendStatus);
    }

    public  void deleteConversation(String conversationId) {
        chatRepository.deleteConversation(conversationId, deleteStatus);
    }
}
