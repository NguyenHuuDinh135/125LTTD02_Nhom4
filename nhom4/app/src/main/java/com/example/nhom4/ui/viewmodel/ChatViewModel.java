package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.ChatRepository;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<Message>>> messages = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> sendStatus = new MutableLiveData<>();

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
    public String getCurrentUserId() { return currentUserId; }

    // Bắt đầu lắng nghe tin nhắn khi có ConversationID
    public void startListening(String conversationId) {
        if (conversationId != null && !conversationId.isEmpty()) {
            chatRepository.getMessages(conversationId, messages);
        }
    }

    public void sendMessage(String conversationId, String content) {
        if (currentUserId == null || content.trim().isEmpty()) return;

        Message msg = new Message(currentUserId, content.trim(), "text");
        chatRepository.sendMessage(conversationId, msg, sendStatus);
    }
}
