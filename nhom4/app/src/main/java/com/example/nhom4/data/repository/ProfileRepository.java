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

/**
 * ProfileRepository
 * --------------------------------------------------
 * Class này chịu trách nhiệm xử lý dữ liệu hồ sơ cá nhân.
 * * ĐẶC ĐIỂM DỮ LIỆU:
 * Thông tin người dùng đang nằm ở 2 collections khác nhau:
 * 1. 'users': Thông tin gốc khi đăng ký (username, email).
 * 2. 'user_profile': Thông tin bổ sung/cập nhật sau này.
 * -> Cần logic lấy cả 2 và gộp lại (Merge) để hiển thị.
 */
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

    /**
     * Tải Profile User (Logic lồng nhau)
     * Quy trình:
     * B1: Lấy dữ liệu từ bảng 'users'.
     * B2: Lấy dữ liệu từ bảng 'user_profile'.
     * B3: Gộp dữ liệu (theo cơ chế ưu tiên).
     * B4: Tự động tạo hồ sơ nếu chưa có (Silent Create).
     */
    public void loadUserProfile(String uid, MutableLiveData<Resource<UserProfile>> result) {
        result.postValue(Resource.loading(null));

        // --- BƯỚC 1: Lấy từ collection 'users' (Dữ liệu gốc) ---
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            // Dùng mảng 1 phần tử để chứa giá trị (Workaround để dùng biến trong lambda)
            final String[] nameFromUsers = {null};
            final String[] emailFromUsers = {null};
            final String[] avatarFromUsers = {null};

            if (userDoc.exists()) {
                nameFromUsers[0] = userDoc.getString("username");
                emailFromUsers[0] = userDoc.getString("email");
                avatarFromUsers[0] = userDoc.getString("profilePhotoUrl");
            }

            // --- BƯỚC 2: Lấy từ collection 'user_profile' (Dữ liệu mở rộng) ---
            db.collection("user_profile").document(uid).get().addOnSuccessListener(profileDoc -> {
                // Khởi tạo giá trị mặc định phòng trường hợp không tìm thấy
                String finalName = "Chưa có tên";
                String finalEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "";
                String finalBirthday = "Chưa có ngày sinh";
                String finalAvatar = null;

                // Nếu tìm thấy trong user_profile, lấy dữ liệu ra dùng tạm
                if (profileDoc.exists()) {
                    if (profileDoc.getString("username") != null) finalName = profileDoc.getString("username");
                    if (profileDoc.getString("email") != null) finalEmail = profileDoc.getString("email");
                    if (profileDoc.getString("birthday") != null) finalBirthday = profileDoc.getString("birthday");
                    if (profileDoc.getString("profilePhotoUrl") != null) finalAvatar = profileDoc.getString("profilePhotoUrl");
                }

                // --- BƯỚC 3: GỘP DỮ LIỆU (MERGE) ---
                // [Lưu ý cho Team]: Code hiện tại ưu tiên dữ liệu từ bảng 'users'.
                // Nếu bảng 'users' có dữ liệu, nó sẽ ĐÈ lên dữ liệu của 'user_profile'.
                if (nameFromUsers[0] != null) finalName = nameFromUsers[0];
                if (emailFromUsers[0] != null) finalEmail = emailFromUsers[0];
                if (avatarFromUsers[0] != null) finalAvatar = avatarFromUsers[0];

                // Đóng gói vào object UserProfile để trả về UI
                UserProfile profile = new UserProfile();
                profile.setUid(uid);
                profile.setUsername(finalName);
                profile.setEmail(finalEmail);
                profile.setBirthday(finalBirthday);
                profile.setProfilePhotoUrl(finalAvatar);

                // --- BƯỚC 4: Silent Create (Tự động tạo) ---
                // Nếu user_profile chưa có (lần đầu vào xem profile), tạo document mặc định.
                // Việc này giúp các lần update sau không bị lỗi "Document not found".
                if (!profileDoc.exists()) {
                    Map<String, Object> defaultMap = new HashMap<>();
                    defaultMap.put("uid", uid);
                    defaultMap.put("username", finalName);
                    defaultMap.put("email", finalEmail);
                    defaultMap.put("birthday", finalBirthday);
                    defaultMap.put("profilePhotoUrl", finalAvatar);
                    db.collection("user_profile").document(uid).set(defaultMap);
                }

                // Trả kết quả thành công
                result.postValue(Resource.success(profile));

            }).addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load profile: " + e.getMessage(), null)));

        }).addOnFailureListener(e -> result.postValue(Resource.error("Lỗi load users: " + e.getMessage(), null)));
    }

    /**
     * Cập nhật Profile
     * Chia làm 2 trường hợp:
     * Case 1: Có ảnh mới -> Upload ảnh lấy link -> Update Firestore.
     * Case 2: Chỉ sửa thông tin (text) -> Update Firestore ngay.
     */
    public void updateProfile(String uid, Map<String, Object> updates, Uri imageUri, MutableLiveData<Resource<Boolean>> result) {
        result.postValue(Resource.loading(null));

        if (imageUri != null) {
            // Case 1: Có ảnh mới -> Upload Storage
            StorageReference avatarRef = storage.getReference().child("avatars/" + uid + ".jpg");
            avatarRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Upload xong, lấy URL ảnh gán vào map update
                        updates.put("profilePhotoUrl", uri.toString());
                        updateFirestore(uid, updates, result);
                    }))
                    .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi upload ảnh: " + e.getMessage(), false)));
        } else {
            // Case 2: Không đổi ảnh -> Update Firestore ngay
            updateFirestore(uid, updates, result);
        }
    }

    // Hàm phụ trợ: Update dữ liệu vào bảng 'user_profile'
    private void updateFirestore(String uid, Map<String, Object> updates, MutableLiveData<Resource<Boolean>> result) {
        db.collection("user_profile").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error(e.getMessage(), false)));
    }
}