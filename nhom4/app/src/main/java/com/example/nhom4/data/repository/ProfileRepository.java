package com.example.nhom4.data.repository;

import android.net.Uri;
import androidx.lifecycle.MutableLiveData;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
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

        // BƯỚC 1: Lấy dữ liệu gốc từ 'users' (Lấy 1 lần vì dữ liệu này ít đổi)
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {

            // Lưu tạm thông tin gốc
            String originalName = userDoc.exists() ? userDoc.getString("username") : null;
            String originalEmail = userDoc.exists() ? userDoc.getString("email") : null;
            String originalAvatar = userDoc.exists() ? userDoc.getString("profilePhotoUrl") : null;

            // BƯỚC 2: LẮNG NGHE REALTIME từ 'user_profile'
            // Dùng addSnapshotListener thay vì get()
            db.collection("user_profile").document(uid).addSnapshotListener((profileDoc, e) -> {
                if (e != null) {
                    result.postValue(Resource.error("Lỗi realtime: " + e.getMessage(), null));
                    return;
                }

                // Khởi tạo giá trị mặc định
                String finalName = "Chưa có tên";
                String finalEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "";
                String finalBirthday = "Chưa có ngày sinh";
                String finalAvatar = null;

                // --- BƯỚC 3: GỘP DỮ LIỆU (LOGIC MỚI) ---
                // ƯU TIÊN 1: Lấy từ 'user_profile' (Dữ liệu cập nhật)
                if (profileDoc != null && profileDoc.exists()) {
                    String pName = profileDoc.getString("username");
                    String pEmail = profileDoc.getString("email");
                    String pBirthday = profileDoc.getString("birthday");
                    String pAvatar = profileDoc.getString("profilePhotoUrl");

                    if (pName != null && !pName.isEmpty()) finalName = pName;
                    if (pEmail != null && !pEmail.isEmpty()) finalEmail = pEmail;
                    if (pBirthday != null && !pBirthday.isEmpty()) finalBirthday = pBirthday;
                    if (pAvatar != null && !pAvatar.isEmpty()) finalAvatar = pAvatar;
                }

                // ƯU TIÊN 2: Nếu 'user_profile' thiếu, trám bằng dữ liệu gốc 'users'
                // (Chỉ trám vào nếu giá trị hiện tại vẫn là mặc định hoặc rỗng)
                if (finalName.equals("Chưa có tên") && originalName != null) finalName = originalName;
                if (finalEmail.isEmpty() && originalEmail != null) finalEmail = originalEmail;
                if (finalAvatar == null && originalAvatar != null) finalAvatar = originalAvatar;

                // Đóng gói trả về UI
                UserProfile profile = new UserProfile();
                profile.setUid(uid);
                profile.setUsername(finalName);
                profile.setEmail(finalEmail);
                profile.setBirthday(finalBirthday);
                profile.setProfilePhotoUrl(finalAvatar);

                // Nếu chưa có doc bên user_profile thì tạo (Silent Create)
                if (profileDoc == null || !profileDoc.exists()) {
                    Map<String, Object> defaultMap = new HashMap<>();
                    defaultMap.put("uid", uid);
                    defaultMap.put("username", finalName);
                    defaultMap.put("email", finalEmail);
                    defaultMap.put("birthday", finalBirthday);
                    defaultMap.put("profilePhotoUrl", finalAvatar);
                    db.collection("user_profile").document(uid).set(defaultMap);
                }

                // Đẩy dữ liệu mới ra LiveData (UI sẽ tự cập nhật)
                result.postValue(Resource.success(profile));
            });

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
    // Sửa lại hàm này để update cả 2 nơi
    private void updateFirestore(String uid, Map<String, Object> updates, MutableLiveData<Resource<Boolean>> result) {
        WriteBatch batch = db.batch();

        // 1. Chuẩn bị update cho bảng 'user_profile' (Bảng chi tiết)
        DocumentReference profileRef = db.collection("user_profile").document(uid);
        batch.update(profileRef, updates);

        // 2. Chuẩn bị update cho bảng 'users' (Bảng dùng cho danh sách bạn bè/tìm kiếm)
        // Chỉ cần đồng bộ các thông tin cơ bản: Tên và Ảnh
        DocumentReference usersRef = db.collection("users").document(uid);
        Map<String, Object> publicUpdates = new HashMap<>();

        if (updates.containsKey("username")) {
            publicUpdates.put("username", updates.get("username"));
        }
        if (updates.containsKey("profilePhotoUrl")) {
            publicUpdates.put("profilePhotoUrl", updates.get("profilePhotoUrl"));
        }

        // Nếu có dữ liệu cần đồng bộ thì thêm vào batch
        if (!publicUpdates.isEmpty()) {
            batch.update(usersRef, publicUpdates);
        }

        // 3. Thực thi cả 2 lệnh cùng lúc (Transaction)
        batch.commit()
                .addOnSuccessListener(aVoid -> result.postValue(Resource.success(true)))
                .addOnFailureListener(e -> result.postValue(Resource.error("Lỗi đồng bộ: " + e.getMessage(), false)));
    }
}