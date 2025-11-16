package com.example.nhom4.ui.page.chatbox;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom4.data.model.Message;
import com.example.nhom4.data.repository.ChatboxRepository;
import java.util.List;

public class ChatboxViewModel extends ViewModel {
    private ChatboxRepository chatRepository;
    private LiveData<List<Message>> messagesLiveData;
    private MutableLiveData<Boolean> sendingStatus = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public ChatboxViewModel() {
        chatRepository = new ChatboxRepository();
    }

    // Lấy danh sách tin nhắn
    public LiveData<List<Message>> getMessages(String userId, String receiverId) {
        if (messagesLiveData == null) {
            messagesLiveData = chatRepository.getMessages(userId, receiverId);
        }
        return messagesLiveData;
    }

    // Gửi tin nhắn
    public void sendMessage(Message message) {
        sendingStatus.setValue(true);
        chatRepository.sendMessage(message, new ChatboxRepository.OnMessageSentListener() {
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
    public void markAsRead(String userId, String receiverId, String messageId) {
        chatRepository.markAsRead(userId, receiverId, messageId);
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
        // Cleanup listener khi ViewModel bị destroy
    }
}