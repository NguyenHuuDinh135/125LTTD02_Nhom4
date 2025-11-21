package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.ui.page.SplashActivity; // Import SplashActivity
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private ShapeableImageView ivProfileAvatar;
    private ImageView btnChangeAvatar;
    private TextInputEditText etEmail; // Giả sử bạn dùng EditText trong include layout
    private MaterialButton btnSave, btnLogout;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    private Uri selectedImageUri = null;

    // Launcher chọn ảnh
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileAvatar.setImageURI(uri); // Hiển thị ảnh vừa chọn
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadUserData();

        // Sự kiện đổi avatar
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Sự kiện bấm vào chính ảnh avatar cũng cho đổi
        ivProfileAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Sự kiện Lưu thay đổi
        btnSave.setOnClickListener(v -> saveProfileChanges());

        // --- SỬA ĐOẠN NÀY: ĐĂNG XUẤT VỀ SPLASH ---
        btnLogout.setOnClickListener(v -> {
            auth.signOut(); // Đăng xuất khỏi Firebase

            // Chuyển về SplashActivity
            Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);

            // Xóa toàn bộ Activity stack cũ để người dùng không thể bấm Back quay lại Profile
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        btnChangeAvatar = findViewById(R.id.btn_edit_avatar_icon);

        // Ánh xạ các trường text
        View includeEmail = findViewById(R.id.item_email);
        TextView valueEmail = includeEmail.findViewById(R.id.tv_value);

        View includeName = findViewById(R.id.item_name);
        TextView valueName = includeName.findViewById(R.id.tv_value);

        // Lưu reference lại để set text (nếu cần dùng sau này)
        // Ở đây chỉ để minh họa việc tìm view

        btnSave = findViewById(R.id.buttonSave);
        btnLogout = findViewById(R.id.buttonSettings);
        btnLogout.setText("Đăng xuất");
        btnLogout.setIconResource(R.drawable.outline_logout_24);
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Load Avatar
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(ivProfileAvatar);
            }

            // Load Info
            View includeEmail = findViewById(R.id.item_email);
            TextView valEmail = includeEmail.findViewById(R.id.tv_value);
            valEmail.setText(user.getEmail());

            View includeName = findViewById(R.id.item_name);
            TextView valName = includeName.findViewById(R.id.tv_value);
            valName.setText(user.getDisplayName());
        }
    }

    private void saveProfileChanges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        if (selectedImageUri != null) {
            // 1. Upload ảnh trước
            String uid = user.getUid();
            StorageReference avatarRef = storage.getReference().child("avatars/" + uid + ".jpg");

            avatarRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateFirebaseProfile(user, downloadUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu thay đổi");
                    });
        } else {
            // Chỉ update thông tin text (nếu có sửa tên)
            updateFirebaseProfile(user, null);
        }
    }

    private void updateFirebaseProfile(FirebaseUser user, String photoUrl) {
        UserProfileChangeRequest.Builder requestBuilder = new UserProfileChangeRequest.Builder();

        if (photoUrl != null) {
            requestBuilder.setPhotoUri(Uri.parse(photoUrl));
        }
        // Nếu có sửa tên: requestBuilder.setDisplayName(newName);

        user.updateProfile(requestBuilder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đồng bộ sang Firestore "users" collection
                        if (photoUrl != null) {
                            db.collection("users").document(user.getUid())
                                    .update("profilePhotoUrl", photoUrl);
                        }

                        Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Lỗi cập nhật profile", Toast.LENGTH_SHORT).show();
                    }
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu thay đổi");
                });
    }
}
