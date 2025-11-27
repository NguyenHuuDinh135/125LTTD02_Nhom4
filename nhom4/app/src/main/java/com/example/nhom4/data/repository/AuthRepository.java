// File: com/example/nhom4/data/repository/AuthRepository.java
package com.example.nhom4.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.nhom4.data.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthRepository
 * --------------------------------------------------
 * Class này đóng vai trò "Cổng bảo vệ":
 * 1. Xử lý đăng nhập/đăng ký thông qua Firebase Auth.
 * 2. Quản lý việc tạo hồ sơ người dùng ban đầu vào Firestore.
 */
public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db; // Thêm Firestore để lưu thông tin chi tiết (username,...)

    public AuthRepository() {
        // Khởi tạo các instance của Firebase để sử dụng trong toàn bộ class
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance(); // Init Firestore
    }

    // Lấy user hiện tại (để kiểm tra xem đã login chưa khi mở app)
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Xử lý Đăng Nhập
     * Luồng: Loading -> Gọi Firebase Auth -> Trả về User (nếu thành công) hoặc Lỗi.
     */
    public void login(String email, String password, MutableLiveData<Resource<FirebaseUser>> result) {
        result.postValue(Resource.loading(null)); // Báo UI hiện loading

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.postValue(Resource.success(authResult.getUser())))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    /**
     * Xử lý Đăng Ký (Bước 1)
     * Lưu ý cho team: Hàm này chỉ tạo tài khoản Auth (Email/Pass).
     * Sau khi hàm này thành công, CẦN gọi tiếp createNewUserProfile để lưu username.
     */
    public void register(String email, String password, MutableLiveData<Resource<FirebaseUser>> result) {
        result.postValue(Resource.loading(null));

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.postValue(Resource.success(authResult.getUser())))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    // Kiểm tra Username đã tồn tại chưa (Dùng cho màn hình đăng ký)
    public void checkUsernameExists(String username, MutableLiveData<Resource<Boolean>> result) {
        // Không cần set Loading ở đây vì đây là check ngầm (silent check) khi user đang gõ
        db.collection("users")
                .whereEqualTo("username", username) // Query tìm username trùng
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Tìm thấy documents -> Tồn tại -> Trả về TRUE (Báo lỗi cho user)
                        result.postValue(Resource.success(true));
                    } else {