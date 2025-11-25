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
        loadConversations();
    }

    public LiveData<Resource<List<Conversation>>> getConversations() { return conversations; }

    public void loadConversations() {
        String uid = authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
        if (uid != null) {
            chatRepository.loadUserConversations(uid, conversations);
        }
    }
}
