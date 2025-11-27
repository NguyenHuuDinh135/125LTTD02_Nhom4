package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.data.repository.AuthRepository;
import com.example.nhom4.data.repository.FriendRepository;
import java.util.List;

/**
 * ViewModel cho màn hình AddFriend: tải danh sách gợi ý và gửi lời mời kết bạn.
 */
public class AddFriendViewModel extends ViewModel {
    private final FriendRepository friendRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<List<User>>> users = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> requestStatus = new MutableLiveData<>();

    public AddFriendViewModel() {
        friendRepository = new FriendRepository();
        authRepository = new AuthRepository();
        loadUsers();
    }

    public LiveData<Resource<List<User>>> getUsers() { return users; }
    public LiveData<Resource<Boolean>> getRequestStatus() { return requestStatus; }

    /**
     * Lấy danh sách người dùng phù hợp từ FriendRepository.
     */
    private void loadUsers() {
        String uid = authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
        if (uid != null) {
            friendRepository.getUsersToConnect(uid, users);
        } else {
            users.setValue(Resource.error("Chưa đăng nhập", null));
        }
    }

    /**
     * Gửi lời mời kết bạn đến user được chọn.
     */
    public void sendFriendRequest(User targetUser) {
        String uid = authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
        if (uid != null && targetUser.getUid() != null) {
            friendRepository.sendFriendRequest(uid, targetUser.getUid(), requestStatus);
        }
    }
}
