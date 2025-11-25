// File: com/example/nhom4/data/repository/AuthRepository.java
package com.example.nhom4.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.nhom4.data.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db; // Thêm Firestore
    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance(); // Init Firestore
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void login(String email, String password, MutableLiveData<Resource<FirebaseUser>> result) {
        result.postValue(Resource.loading(null));
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.postValue(Resource.success(authResult.getUser())))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    public void register(String email, String password, MutableLiveData<Resource<FirebaseUser>> result) {
        result.postValue(Resource.loading(null));
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.postValue(Resource.success(authResult.getUser())))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    // Kiểm tra Username đã tồn tại chưa
    public void checkUsernameExists(String username, MutableLiveData<Resource<Boolean>> result) {
        // Không cần set Loading ở đây vì đây là check ngầm (silent check)
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Tìm thấy -> Tồn tại -> Trả về TRUE
                        result.postValue(Resource.success(true));
                    } else {
                        // Không thấy -> Chưa tồn tại -> Trả về FALSE (Hợp lệ)
                        result.postValue(Resource.success(false));
                    }
                })
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), null)));
    }

    // Lưu thông tin User mới vào Firestore
    public void createNewUserProfile(String userId, String username, String email, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("displayName", username);
        userMap.put("uid", userId);
        userMap.put("createdAt", com.google.firebase.Timestamp.now());
        // Thêm các trường mặc định khác nếu cần (profilePhotoUrl: null...)

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }
}
