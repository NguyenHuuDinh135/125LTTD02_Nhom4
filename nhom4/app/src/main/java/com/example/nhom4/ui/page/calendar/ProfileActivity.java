package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.UserProfile;
import com.example.nhom4.ui.page.SplashActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileAvatar;
    private ImageView btnChangeAvatar;

    private TextInputEditText etEmail, etName, etBirthday;
    private MaterialButton btnSave, btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileAvatar.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        loadUserProfile();

        // Sự kiện chọn avatar
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        ivProfileAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Sự kiện Lưu
        btnSave.setOnClickListener(v -> saveProfileChanges());

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        btnChangeAvatar = findViewById(R.id.btn_edit_avatar_icon);

        etEmail = findViewById(R.id.item_email).findViewById(R.id.et_value);
        etName = findViewById(R.id.item_name).findViewById(R.id.et_value);
        etBirthday = findViewById(R.id.item_birthday).findViewById(R.id.et_value);

        btnSave = findViewById(R.id.buttonSave);
        btnLogout = findViewById(R.id.buttonSettings);
        btnLogout.setText("Đăng xuất");
        btnLogout.setIconResource(R.drawable.outline_logout_24);
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("user_profile").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("username") != null ? doc.getString("username") : "Chưa có tên");
                        etEmail.setText(doc.getString("email") != null ? doc.getString("email") : user.getEmail());
                        etBirthday.setText(doc.getString("birthday") != null ? doc.getString("birthday") : "Chưa có ngày sinh");

                        String avatarUrl = doc.getString("profilePhotoUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).into(ivProfileAvatar);
                        }
                    } else {
                        // Tạo profile mặc định nếu chưa có
                        UserProfile defaultProfile = new UserProfile(
                                user.getUid(),
                                "Chưa có tên",
                                user.getEmail(),
                                "Chưa có ngày sinh",
                                null,
                                null
                        );
                        db.collection("user_profile").document(user.getUid())
                                .set(defaultProfile);

                        etName.setText(defaultProfile.getUsername());
                        etEmail.setText(defaultProfile.getEmail());
                        etBirthday.setText(defaultProfile.getBirthday());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveProfileChanges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        String newEmail = etEmail.getText().toString().trim();
        String newName = etName.getText().toString().trim();
        String newBirthday = etBirthday.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", newEmail.isEmpty() ? user.getEmail() : newEmail);
        updates.put("username", newName.isEmpty() ? "Chưa có tên" : newName);
        updates.put("birthday", newBirthday.isEmpty() ? "Chưa có ngày sinh" : newBirthday);

        if (selectedImageUri != null) {
            StorageReference avatarRef = storage.getReference().child("avatars/" + user.getUid() + ".jpg");
            avatarRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                updates.put("profilePhotoUrl", uri.toString());
                                updateFirestore(updates);
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText("Lưu thay đổi");
                    });
        } else {
            updateFirestore(updates);
        }
    }

    private void updateFirestore(Map<String, Object> updates) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("user_profile").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu thay đổi");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu thay đổi");
                });
    }
}
