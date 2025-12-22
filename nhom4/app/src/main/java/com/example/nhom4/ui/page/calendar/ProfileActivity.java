package com.example.nhom4.ui.page.calendar;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.ui.page.SplashActivity;
import com.example.nhom4.ui.page.widget.ActivityListWidgetProvider;
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

/**
 * Màn hình hồ sơ người dùng: xem/chỉnh sửa tên, email, ngày sinh và ảnh đại diện.
 */
public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileAvatar;
    private ImageView btnChangeAvatar, ivAddWidgetIcon, ivAddAcWidgetIcon;

    private TextInputEditText etEmail, etName, etBirthday;
    private MaterialButton btnSave, btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;

    private ImageView btnBack;
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

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Click icon thêm widget
        ivAddWidgetIcon.setOnClickListener(v -> addWidgetToHomeScreen());
        ivAddAcWidgetIcon.setOnClickListener(v -> addActivityWidgetToHomeScreen());

    }

    /**
     * Ánh xạ view và chuẩn bị trạng thái ban đầu cho nút đăng xuất.
     */
    private void initViews() {
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        btnChangeAvatar = findViewById(R.id.btn_edit_avatar_icon);

        // <-- đây là icon widget mới
        ivAddWidgetIcon = findViewById(R.id.iv_add_widget_icon);
        ivAddAcWidgetIcon = findViewById(R.id.iv_add_acwidget_icon);

        etEmail = findViewById(R.id.item_email).findViewById(R.id.et_value);
        etName = findViewById(R.id.item_name).findViewById(R.id.et_value);
        etBirthday = findViewById(R.id.item_birthday).findViewById(R.id.et_value);

        btnSave = findViewById(R.id.buttonSave);
        btnLogout = findViewById(R.id.buttonSettings);
        btnLogout.setText("Đăng xuất");
        btnLogout.setIconResource(R.drawable.outline_logout_24);
        btnBack = findViewById(R.id.btn_back);
    }

    // -----------------------------
    // Thêm widget vào màn hình chính
    // -----------------------------
    private void addWidgetToHomeScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
            ComponentName myProvider = new ComponentName(this, com.example.nhom4.ui.page.widget.ActivityWidgetProvider.class);

            if (!appWidgetManager.isRequestPinAppWidgetSupported()) {
                Toast.makeText(this, "Launcher không hỗ trợ thêm widget", Toast.LENGTH_SHORT).show();
            } else {
                // Pin widget trực tiếp, không cần PendingIntent callback
                appWidgetManager.requestPinAppWidget(myProvider, null, null);
            }
        } else {
            Toast.makeText(this, "Yêu cầu Android 8.0 trở lên để thêm widget từ app", Toast.LENGTH_SHORT).show();
        }
    }
    //widget activity
    private void addActivityWidgetToHomeScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager manager = getSystemService(AppWidgetManager.class);
            ComponentName provider = new ComponentName(this, ActivityListWidgetProvider.class);

            if (manager.isRequestPinAppWidgetSupported()) {
                boolean success = manager.requestPinAppWidget(provider, null, null);
                if (success) {
                    Toast.makeText(this, "Đang mở dialog thêm Widget Activity...", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Fallback thủ công
        Toast.makeText(this,
                "Launcher hiện tại không hỗ trợ thêm tự động Widget Activity.\n\n" +
                        "Vui lòng thêm thủ công:\n" +
                        "• Giữ lâu trên màn hình chính\n" +
                        "• Chọn 'Widget'\n" +
                        "• Tìm ứng dụng 'Nhom4'\n" +
                        "• Kéo 'Widget Activity' ra màn hình",
                Toast.LENGTH_LONG).show();
    }


    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("user_profile").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String finalName = doc.getString("username");
                    String finalEmail = doc.getString("email");
                    String finalBirthday = doc.getString("birthday");
                    String finalAvatar = doc.getString("profilePhotoUrl");

                    // Nếu chưa có, dùng giá trị mặc định
                    if (finalName == null) finalName = "Chưa có tên";
                    if (finalEmail == null) finalEmail = user.getEmail();
                    if (finalBirthday == null) finalBirthday = "Chưa có ngày sinh";

                    // Gán UI
                    etName.setText(finalName);
                    etEmail.setText(finalEmail);
                    etBirthday.setText(finalBirthday);

                    if (finalAvatar != null && !finalAvatar.isEmpty()) {
                        Glide.with(this).load(finalAvatar).into(ivProfileAvatar);
                    }

                    // Nếu chưa tồn tại -> tạo mới
                    if (!doc.exists()) {
                        Map<String, Object> defaultProfile = new HashMap<>();
                        defaultProfile.put("uid", uid);
                        defaultProfile.put("username", finalName);
                        defaultProfile.put("email", finalEmail);
                        defaultProfile.put("birthday", finalBirthday);
                        defaultProfile.put("profilePhotoUrl", finalAvatar);

                        db.collection("user_profile").document(uid).set(defaultProfile);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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