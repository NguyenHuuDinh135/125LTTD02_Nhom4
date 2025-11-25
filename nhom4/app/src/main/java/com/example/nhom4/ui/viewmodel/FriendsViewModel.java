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

public class FriendsViewModel extends ViewModel {

    private final FriendRepository friendRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<User>>> suggestions = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<FriendRequest>>> friendRequests = new MutableLiveData<>();

    // Dùng chung status cho các hành động (Gửi/Chấp nhận/Từ chối)
    private final MutableLiveData<Resource<String>> actionStatus = new MutableLiveData<>();

    public FriendsViewModel() {
        friendRepository = new FriendRepository();
        authRepository = new AuthRepository();
        loadData();
    }

    public LiveData<Resource<List<User>>> getSuggestions() { return suggestions; }
    public LiveData<Resource<List<FriendRequest>>> getFriendRequests() { return friendRequests; }
    public LiveData<Resource<String>> getActionStatus() { return actionStatus; }

    private void loadData() {
        String uid = getCurrentUserId();
        if (uid != null) {
            friendRepository.getUsersToConnect(uid, suggestions);
            friendRepository.getPendingRequests(uid, friendRequests);
        }
    }

    public void sendFriendRequest(User user) {
        String uid = getCurrentUserId();
        if (uid == null) return;

        MutableLiveData<Resource<Boolean>> tempResult = new MutableLiveData<>();
        friendRepository.sendFriendRequest(uid, user.getUid(), tempResult);

        // Quan sát kết quả tạm và update actionStatus
        tempResult.observeForever(res -> {
            if (res.status == Resource.Status.SUCCESS) {
                actionStatus.setValue(Resource.success("Đã gửi lời mời tới " + user.getUsername()));
            } else if (res.status == Resource.Status.ERROR) {
                actionStatus.setValue(Resource.error(res.message, null));
            }
        });
    }

    public void acceptRequest(FriendRequest request) {
        handleResponse(request, "accepted", "Đã chấp nhận kết bạn");
    }

    public void declineRequest(FriendRequest request) {
        handleResponse(request, "rejected", "Đã từ chối lời mời");
    }

    private void handleResponse(FriendRequest request, String status, String successMsg) {
        MutableLiveData<Resource<Boolean>> tempResult = new MutableLiveData<>();
        friendRepository.respondToRequest(request.getRequestId(), status, tempResult);

        tempResult.observeForever(res -> {
            if (res.status == Resource.Status.SUCCESS) {
                actionStatus.setValue(Resource.success(successMsg));
                // Danh sách request sẽ tự update nhờ SnapshotListener trong Repository
            } else if (res.status == Resource.Status.ERROR) {
                actionStatus.setValue(Resource.error(res.message, null));
            }
        });
    }

    private String getCurrentUserId() {
        return authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
    }
}
