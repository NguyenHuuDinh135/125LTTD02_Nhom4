package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Message;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.ChatRepository;

public class ReplyViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<Boolean>> sendStatus = new MutableLiveData<>();

    public ReplyViewModel() {
        chatRepository = new ChatRepository();
        authRepository = new AuthRepository();
    }

    public LiveData<Resource<Boolean>> getSendStatus() {
        return sendStatus;
    }

    public void sendReply(String content, Post post) {
        if (content.isEmpty() || post == null) return;

        String currentUserId = authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            sendStatus.setValue(Resource.error("Bạn chưa đăng nhập", false));
            return;
        }

        sendStatus.setValue(Resource.loading(null));

        // 1. Tìm hoặc tạo Conversation ID
        MutableLiveData<Resource<String>> conversationResult = new MutableLiveData<>();
        chatRepository.findOrCreateConversation(currentUserId, post.getUserId(), conversationResult);

        // 2. Lắng nghe kết quả tìm ID -> Sau đó gửi tin
        conversationResult.observeForever(res -> {
            if (res.status == Resource.Status.SUCCESS && res.data != null) {
                String conversationId = res.data;

                // Tạo Message Object
                String postImage = (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty())
                        ? post.getPhotoUrl() : post.getMoodIconUrl();
                String postTitle = ("mood".equals(post.getType())) ? post.getMoodName() : post.getActivityTitle();

                Message msg = new Message(
                        currentUserId,
                        content,
                        "post_reply",
                        post.getPostId(),
                        postImage,
                        postTitle
                );

                // Gửi
                chatRepository.sendMessage(conversationId, msg, sendStatus);
            } else if (res.status == Resource.Status.ERROR) {
                sendStatus.setValue(Resource.error(res.message, false));
            }
        });
    }
}
