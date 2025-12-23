package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.FriendRepository;

import java.util.List;

/**
 * ViewModel đứng sau BottomSheet bạn bè: tải gợi ý, lời mời và xử lý các hành động kết bạn.
 */
public class FriendsViewModel extends ViewModel {

    private final FriendRepository friendRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<User>>> suggestions = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<FriendRequest>>> friendRequests = new MutableLiveData<>();

    // Separate LiveData cho từng hành động
    private final MutableLiveData<Resource<Boolean>> sendResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> acceptResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> declineResult = new MutableLiveData<>();

    public FriendsViewModel() {
        friendRepository = new FriendRepository();
        authRepository = new AuthRepository();
        loadData();
    }

    public LiveData<Resource<List<User>>> getSuggestions() { return suggestions; }
    public LiveData<Resource<List<FriendRequest>>> getFriendRequests() { return friendRequests; }
    public LiveData<Resource<Boolean>> getSendResult() { return sendResult; }
    public LiveData<Resource<Boolean>> getAcceptResult() { return acceptResult; }
    public LiveData<Resource<Boolean>> getDeclineResult() { return declineResult; }

    /**
     * Lấy cả danh sách gợi ý và lời mời đang chờ ngay khi khởi tạo.
     */
    private void loadData() {
        String uid = getCurrentUserId();
        if (uid != null) {
            friendRepository.getUsersToConnect(uid, suggestions);
            friendRepository.getPendingRequests(uid, friendRequests);
        }
    }

    public void sendFriendRequest(String userId) {
        String uid = getCurrentUserId();
        if (uid != null) {
            friendRepository.sendFriendRequest(uid, userId, sendResult);
        } else {
            sendResult.postValue(Resource.error("Không thể gửi lời mời", false));
        }
    }

    public void acceptFriendRequest(String senderId) {
        String uid = getCurrentUserId();
        if (uid != null) {
            friendRepository.acceptFriendRequest(uid, senderId, acceptResult);
        } else {
            acceptResult.postValue(Resource.error("Không thể chấp nhận lời mời", false));
        }
    }

    public void declineFriendRequest(String senderId) {
        String uid = getCurrentUserId();
        if (uid != null) {
            friendRepository.declineFriendRequest(uid, senderId, declineResult);
        } else {
            declineResult.postValue(Resource.error("Không thể từ chối lời mời", false));
        }
    }

    private String getCurrentUserId() {
        return authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
    }
}