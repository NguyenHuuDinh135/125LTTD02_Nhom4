package com.example.nhom4.data.repository;

import androidx.annotation.NonNull;

import com.example.nhom4.data.bean.UserProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileRepository {

    private final FirebaseFirestore firestore;
    private final String COLLECTION_NAME = "user_profiles";

    public UserProfileRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    /** Callback interface */
    public interface OnUserProfileCallback {
        void onSuccess(UserProfile profile);
        void onFailure(String error);
    }

    /** Lấy user profile theo UID */
    public void getUserProfile(String uid, OnUserProfileCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        callback.onSuccess(profile);
                    } else {
                        callback.onFailure("User profile not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Cập nhật toàn bộ profile */
    public void updateUserProfile(UserProfile profile, OnUserProfileCallback callback) {
        firestore.collection(COLLECTION_NAME)
                .document(profile.getUid())
                .set(profile)
                .addOnSuccessListener(aVoid -> callback.onSuccess(profile))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Cập nhật một trường bất kỳ */
    public void updateField(String uid, String field, Object value,
                            OnSuccessListener<Void> successListener,
                            OnFailureListener failureListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(field, value);

        firestore.collection(COLLECTION_NAME)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /** Đăng xuất */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}
