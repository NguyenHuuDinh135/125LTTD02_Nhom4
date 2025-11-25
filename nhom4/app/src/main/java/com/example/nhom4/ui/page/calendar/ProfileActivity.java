package com.example.nhom4.ui.page.calendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.UserProfile;
import com.example.nhom4.ui.page.SplashActivity;
import com.example.nhom4.ui.viewmodel.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileAvatar;
    private ImageView btnChangeAvatar;
    private TextInputEditText etEmail, etName, etBirthday;
    private MaterialButton btnSave, btnLogout;

    private ProfileViewModel viewModel;
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

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews();
        setupEvents();
        observeViewModel();

        // Load dữ liệu ban đầu
        viewModel.loadProfile();
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

    private void setupEvents() {
        // Chọn ảnh
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        ivProfileAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Lưu
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();

            viewModel.saveProfile(name, email, birthday, selectedImageUri);
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void observeViewModel() {
        // 1. Quan sát dữ liệu Profile
        viewModel.getUserProfile().observe(this, resource -> {
            if (resource.status == Resource.Status.LOADING) {
                // Có thể hiện loading skeleton
            } else if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                UserProfile profile = resource.data;
                etName.setText(profile.getUsername());
                etEmail.setText(profile.getEmail());
                etBirthday.setText(profile.getBirthday());

                if (profile.getProfilePhotoUrl() != null && !profile.getProfilePhotoUrl().isEmpty()) {
                    Glide.with(this).load(profile.getProfilePhotoUrl()).into(ivProfileAvatar);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Quan sát trạng thái Lưu
        viewModel.getSaveStatus().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    btnSave.setEnabled(false);
                    btnSave.setText("Đang lưu...");
                    break;
                case SUCCESS:
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu thay đổi");
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu thay đổi");
                    Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
