package com.example.nhom4.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom4.data.Resource;
import com.example.nhom4.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;

    // LiveData cho Login/Register
    private final MutableLiveData<Resource<FirebaseUser>> authResult = new MutableLiveData<>();

    // LiveData cho việc check username (CreateUsernameActivity)
    // True = Đã tồn tại (Lỗi), False = Chưa tồn tại (OK)
    private final MutableLiveData<Resource<Boolean>> usernameCheckResult = new MutableLiveData<>();

    // LiveData cho việc lưu profile (CreateUsernameActivity)
    // True = Thành công
    private final MutableLiveData<Resource<Boolean>> createProfileResult = new MutableLiveData<>();

    public AuthViewModel() {
        this.repository = new AuthRepository();
    }

    // --- GETTERS FOR LIVE DATA ---
    public LiveData<Resource<FirebaseUser>> getAuthResult() {
        return authResult;
    }

    public LiveData<Resource<Boolean>> getUsernameCheckResult() {
        return usernameCheckResult;
    }

    public LiveData<Resource<Boolean>> getCreateProfileResult() {
        return createProfileResult;
    }

    // --- AUTHENTICATION LOGIC ---

    public void login(String email, String password) {
        repository.login(email, password, authResult);
    }

    public void register(String email, String password) {
        repository.register(email, password, authResult);
    }

    public boolean isLoggedIn() {
        return repository.getCurrentUser() != null;
    }

    // --- USER PROFILE LOGIC (cho CreateUsernameActivity) ---

    public void checkUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        repository.checkUsernameExists(username.trim(), usernameCheckResult);
    }

    public void createUserProfile(String username) {
        FirebaseUser currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            createProfileResult.setValue(Resource.error("Người dùng chưa đăng nhập", false));
            return;
        }

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();

        repository.createNewUserProfile(userId, username, email, createProfileResult);
    }
}
