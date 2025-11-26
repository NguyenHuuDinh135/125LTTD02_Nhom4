package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.ChatRepository;

import java.util.List;

public class DiscoveryViewModel extends ViewModel {
    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<Conversation>>> conversations = new MutableLiveData<>();

    public DiscoveryViewModel() {
        chatRepository = new ChatRepository();
        authRepository = new AuthRepository();
        loadConversations(); // Load ngay lần đầu khởi tạo
    }

    public LiveData<Resource<List<Conversation>>> getConversations() {
        return conversations;
    }

    // Hàm public để Fragment gọi khi cần refresh (onResume)
    public void loadConversations() {
        if (authRepository.getCurrentUser() != null) {
            chatRepository.loadUserConversations(authRepository.getCurrentUser().getUid(), conversations);
        }
    }
}
