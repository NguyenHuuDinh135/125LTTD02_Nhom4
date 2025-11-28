package com.example.nhom4.ui.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.UserProfile;
import com.example.nhom4.data.repository.ProfileRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel cho ProfileActivity: tải thông tin người dùng và cập nhật profile + avatar.
 */
public class ProfileViewModel extends ViewModel {
    private final ProfileRepository repository;

    private final MutableLiveData<Resource<UserProfile>> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> saveStatus = new MutableLiveData<>();

    public ProfileViewModel() {
        this.repository = new ProfileRepository();
    }

    public LiveData<Resource<UserProfile>> getUserProfile() { return userProfile; }
    public LiveData<Resource<Boolean>> getSaveStatus() { return saveStatus; }

    /**
     * Tải thông tin profile hiện tại của người dùng.
     */
    public void loadProfile() {
        FirebaseUser user = repository.getCurrentUser();
        if (user != null) {
            repository.loadUserProfile(user.getUid(), userProfile);
        }
    }

    /**
     * Chuẩn bị map dữ liệu và gọi repository cập nhật profile.
     */
    public void saveProfile(String name, String email, String birthday, Uri imageUri) {
        FirebaseUser user = repository.getCurrentUser();
        if (user == null) return;

        // Logic chuẩn bị dữ liệu update
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name.isEmpty() ? "Chưa có tên" : name);
        updates.put("email", email.isEmpty() ? user.getEmail() : email);
        updates.put("birthday", birthday.isEmpty() ? "Chưa có ngày sinh" : birthday);

        repository.updateProfile(user.getUid(), updates, imageUri, saveStatus); // Repo phụ trách upload ảnh (nếu có)
    }

    public void logout() {
        repository.logout();
    }
}
