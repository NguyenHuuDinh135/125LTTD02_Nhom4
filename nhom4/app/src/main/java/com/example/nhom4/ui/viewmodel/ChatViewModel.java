package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.ChatRepository;
import com.example.nhom4.data.repository.FriendRepository;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final FriendRepository friendRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<Message>>> messages = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> sendStatus = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> deleteConversationStatus = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> unFriendStatus = new MutableLiveData<>();

    // [MỚI] LiveData để hứng kết quả tìm/tạo conversation ID
    private final MutableLiveData<Resource<String>> conversationIdResult = new MutableLiveData<>();

    private String currentUserId;

    public ChatViewModel() {
        chatRepository = new ChatRepository();
        authRepository = new AuthRepository();
        friendRepository = new FriendRepository();
        if (authRepository.getCurrentUser() != null) {
            currentUserId = authRepository.getCurrentUser().getUid();
        }
    }

    public LiveData<Resource<List<Message>>> getMessages() { return messages; }
    public LiveData<Resource<Boolean>> getSendStatus() { return sendStatus; }
    public LiveData<Resource<Boolean>> getDeleteResult() { return deleteConversationStatus; }
    // [MỚI] Getter cho conversationId
    public LiveData<Resource<String>> getConversationIdResult() { return conversationIdResult; }

    public String getCurrentUserId() { return currentUserId; }

    // [MỚI] Hàm tìm hoặc tạo cuộc hội thoại (Gọi khi vào màn hình Chat)
    public void findOrCreateConversation(String partnerId) {
        if (currentUserId != null && partnerId != null) {
            chatRepository.findOrCreateConversation(currentUserId, partnerId, conversationIdResult);
        }
    }

    // Bắt đầu lắng nghe tin nhắn khi đã có ConversationID
    public void startListening(String conversationId) {
        if (conversationId != null && !conversationId.isEmpty()) {
            chatRepository.getMessages(conversationId, messages);
        }
    }

    // Gửi tin nhắn thường
    public void sendMessage(String conversationId, String content) {
        if (currentUserId == null || content.trim().isEmpty()) return;
        if (conversationId == null) return; // Chặn nếu chưa có ID

        Message msg = new Message(currentUserId, content.trim(), "text");
        chatRepository.sendMessage(currentUserId, conversationId, msg, sendStatus);
    }

    // Gửi tin nhắn Reply Post
    public void sendReplyPost(String conversationId, String content, String postId, String postTitle, String postImage) {
        if (currentUserId == null) return;
        if (conversationId == null) return;

        Message msg = new Message(
                currentUserId,
                content.trim(),
                "post_reply",
                postId,
                postImage,
                postTitle
        );
        chatRepository.sendMessage(currentUserId, conversationId, msg, sendStatus);
    }

    public void deleteConversation(String conversationId) {
        chatRepository.deleteConversation(conversationId, deleteConversationStatus);
    }

    public void unFriend(String friendId) {
        friendRepository.unfriendUser(getCurrentUserId(), friendId, unFriendStatus);
    }
}