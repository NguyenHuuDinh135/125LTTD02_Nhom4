package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.ChatRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * ViewModel quản lý dữ liệu cho màn hình Khám phá (DiscoveryFragment).
 * Bao gồm:
 * 1. Danh sách tin nhắn (Realtime + hiển thị cả bạn bè chưa nhắn).
 * 2. Danh sách hoạt động (Activity) đã tham gia (Realtime).
 */
public class DiscoveryViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // LiveData chứa danh sách hội thoại (Chat List)
    private final MutableLiveData<Resource<List<Conversation>>> conversations = new MutableLiveData<>();

    // LiveData chứa danh sách hoạt động (Activities)
    private final MutableLiveData<Resource<List<Activity>>> activities = new MutableLiveData<>();

    // Cờ đánh dấu để tránh gọi listener nhiều lần khi config change
    private boolean isChatLoaded = false;
    private boolean isActivityLoaded = false;

    public DiscoveryViewModel() {
        chatRepository = new ChatRepository();
        authRepository = new AuthRepository();

        // Tự động kích hoạt load dữ liệu khi ViewModel được khởi tạo
        loadConversations();
        loadJoinedActivities();
    }

    // --- GETTERS ---
    public LiveData<Resource<List<Conversation>>> getConversations() {
        return conversations;
    }

    public LiveData<Resource<List<Activity>>> getActivities() {
        return activities;
    }

    // --- LOAD DATA ---

    /**
     * Load danh sách Chat (Sử dụng ChatRepository mới).
     * Repository sẽ tự động:
     * 1. Lấy danh sách bạn bè.
     * 2. Lắng nghe tin nhắn Realtime.
     * 3. Gộp lại để hiển thị đầy đủ.
     */
    public void loadConversations() {
        // Kiểm tra xem đã load chưa để tránh đăng ký listener trùng lặp
        if (isChatLoaded) return;

        if (authRepository.getCurrentUser() != null) {
            String uid = authRepository.getCurrentUser().getUid();

            // Gọi hàm trong ChatRepository
            chatRepository.loadUserConversations(uid, conversations);

            isChatLoaded = true;
        } else {
            conversations.postValue(Resource.error("Chưa đăng nhập", null));
        }
    }

    /**
     * Load danh sách Activity user đã tham gia (Realtime)
     */
    public void loadJoinedActivities() {
        if (isActivityLoaded) return;

        if (authRepository.getCurrentUser() != null) {
            String uid = authRepository.getCurrentUser().getUid();
            activities.postValue(Resource.loading(null));

            // Lắng nghe realtime collection activities
            // Lọc những activity mà user này có trong mảng 'participants'
            db.collection("activities")
                    .whereArrayContains("participants", uid)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            activities.postValue(Resource.error(error.getMessage(), null));
                            return;
                        }
                        if (snapshots != null) {
                            List<Activity> list = snapshots.toObjects(Activity.class);
                            activities.postValue(Resource.success(list));
                        }
                    });

            isActivityLoaded = true;
        }
    }
}