package com.example.nhom4.ui.page.chatbox;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.repository.ChatboxRepository;

import java.util.List;

public class ChatboxViewModel extends ViewModel {

    private final ChatboxRepository chatRepository;

    private LiveData<List<Message>> messagesLiveData;
    private final MutableLiveData<Boolean> sendingStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private String conversationId; // ConversationId dùng cho toàn bộ chat

    public ChatboxViewModel() {
        chatRepository = new ChatboxRepository();
    }

    // Khởi tạo Conversation ID (từ Activity gửi xuống)
    public void init(String conversationId) {
        if (this.conversationId == null) {
            this.conversationId = conversationId;
        }
    }

    // Lấy tin nhắn từ Firestore theo conversationId
    public LiveData<List<Message>> getMessages() {
        if (conversationId == null) {
            throw new IllegalStateException("ConversationId chưa được set! Hãy gọi init() trước.");
        }

        if (messagesLiveData == null) {
            messagesLiveData = chatRepository.getMessages(conversationId);
        }

        return messagesLiveData;
    }

    // Gửi tin nhắn
    public void sendMessage(Message message) {
        if (conversationId == null) {
            errorLiveData.setValue("Lỗi: conversationId null");
            return;
        }

        sendingStatus.setValue(true);

        chatRepository.sendMessage(conversationId, message, new ChatboxRepository.OnMessageSentListener() {
            @Override
            public void onSuccess() {
                sendingStatus.setValue(false);
            }

            @Override
            public void onFailure(String error) {
                sendingStatus.setValue(false);
                errorLiveData.setValue(error);
            }
        });
    }

    // Đánh dấu đã đọc
    public void markAsRead(String messageId, String userId) {
        if (conversationId != null) {
            chatRepository.markAsRead(conversationId, messageId, userId);
        }
    }

    public LiveData<Boolean> getSendingStatus() {
        return sendingStatus;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (conversationId != null) {
            chatRepository.removeListener(conversationId);
        }
    }
}
