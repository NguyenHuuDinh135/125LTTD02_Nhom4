package com.example.nhom4.data.repository;

import android.net.Uri;
import androidx.lifecycle.MutableLiveData;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }

    // Logic tải Profile phức tạp (User -> UserProfile -> Merge)
    public void loadUserProfile(String uid, MutableLiveData<Resource<UserProfile>> result) {
        result.postValue(Resource.loading(null));

        // BƯỚC 1: Lấy từ collection 'users'
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            final String[] nameFromUsers = {null};
            final String[] emailFromUsers = {null};
            final String[] avatarFromUsers = {null};

            if (userDoc.exists()) {
                nameFromUsers[0] = userDoc.getString("username");
                emailFromUsers[0] = userDoc.getString("email");
                avatarFromUsers[0] = userDoc.getString("profilePhotoUrl");
            }

            // BƯỚC 2: Lấy từ collection 'user_profile'
            db.collection("user_profile").document(uid).get().addOnSuccessListener(profileDoc -> {
                String finalName = "Chưa có tên";
                String finalEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "";
                String finalBirthday = "Chưa có ngày sinh";
                String finalAvatar = null;

                if (profileDoc.exists()) {
                    if (profileDoc.getString("username") != null) finalName = profileDoc.getString("username");
                    if (profileDoc.getString("email") != null) finalEmail = profileDoc.getString("email");
                    if (profileDoc.getString("birthday") != null) finalBirthday = profileDoc.getString("birthday");
                    if (profileDoc.getString("profilePhotoUrl") != null) finalAvatar = profileDoc.getString("profilePhotoUrl");
                }

                // Ưu tiên dữ liệu từ 'users'
                if (nameFromUsers[0] != null) finalName = nameFromUsers[0];
                if (emailFromUsers[0] != null) finalEmail = emailFromUsers[0];
                if (avatarFromUsers[0] != null) finalAvatar = avatarFromUsers[0];

                // Tạo object UserProfile để trả về
                UserProfile profile = new UserProfile();
                profile.setUid(uid);
                profile.setUsername(finalName);
                profile.setEmail(finalEmail);
                profile.setBirthday(finalBirthday);
                profile.setProfilePhotoUrl(finalAvatar);

                // Nếu user_profile chưa có, tạo mặc định (Silent create)
                if (!profileDoc.exists()) {
                    Map<String, Object> defaultMap = new HashMap<>();
                    defaultMap.put("uid", uid);
                    defaultMap.put("username", finalName);
                    defaultMap.put("email", finalEmail);
                    defaultMap.put("birthday", finalBirthday);
                    defaultMap.put("profilePhotoUrl", finalAvatar);
                    db.collection("user_profile").document(uid).set(defaultMap);
                }

                result.postValue(Resource.success(profile));

            }).addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load profile: " + e.getMessage(), null)));

        }).addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load users: " + e.getMessage(), null)));
    }

    // Logic Upload ảnh và Cập nhật Firestore
    public void updateProfile(String uid, Map<String, Object> updates, Uri imageUri, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        if (imageUri != null) {
            // Case 1: Có ảnh mới -> Upload Storage -> Get URL -> Update Firestore
            StorageReference avatarRef = storage.getReference().child("avatars/" + uid + ".jpg");
            avatarRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("profilePhotoUrl", uri.toString());
                        updateFirestore(uid, updates, result);
                    }))
                    .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi upload ảnh: " + e.getMessage(), false)));
        } else {
            // Case 2: Không đổi ảnh -> Update Firestore ngay
            updateFirestore(uid, updates, result);
        }
    }

    private void updateFirestore(String uid, Map<String, Object> updates, MutableLiveData<Resource<Boolean>> result) {
        db.collection("user_profile").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }
}
