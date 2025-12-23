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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;

import com.example.nhom4.data.bean.UserProfile;
import com.example.nhom4.ui.page.SplashActivity;
import com.example.nhom4.ui.page.widget.ActivityListWidgetProvider;
import com.example.nhom4.ui.viewmodel.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView ivProfileAvatar;
    private ImageView btnChangeAvatar, ivAddWidgetIcon, ivAddAcWidgetIcon, btnBack;
    private TextInputEditText etEmail, etName, etBirthday;
    private MaterialButton btnSave, btnLogout;

    // Thay thế các biến Firebase rời rạc bằng ViewModel
    private ProfileViewModel viewModel;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileAvatar.setImageURI(uri); // Hiển thị ảnh tạm thời
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupViewModel(); // Setup ViewModel
        setupEvents();
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        btnChangeAvatar = findViewById(R.id.btn_edit_avatar_icon);
        ivAddWidgetIcon = findViewById(R.id.iv_add_widget_icon);
        ivAddAcWidgetIcon = findViewById(R.id.iv_add_acwidget_icon);

        // Lưu ý: Đảm bảo ID trong layout khớp với code
        etEmail = findViewById(R.id.item_email).findViewById(R.id.et_value);
        etName = findViewById(R.id.item_name).findViewById(R.id.et_value);
        etBirthday = findViewById(R.id.item_birthday).findViewById(R.id.et_value);

        btnSave = findViewById(R.id.buttonSave);
        btnLogout = findViewById(R.id.buttonSettings);
        btnBack = findViewById(R.id.btn_back);

        // Setup UI ban đầu
        btnLogout.setText("Đăng xuất");
        btnLogout.setIconResource(R.drawable.outline_logout_24);
    }

    private void setupViewModel() {
        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 2. Lắng nghe dữ liệu User (REALTIME)
        viewModel.getUserProfile().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    // Có thể hiện ProgressBar nếu muốn
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        UserProfile user = resource.data;
                        // Cập nhật UI mỗi khi dữ liệu thay đổi
                        etName.setText(user.getUsername());
                        etEmail.setText(user.getEmail());
                        etBirthday.setText(user.getBirthday());

                        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                            Glide.with(this).load(user.getProfilePhotoUrl()).into(ivProfileAvatar);
                        }
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // 3. Lắng nghe trạng thái Lưu (Save Status)
        viewModel.getSaveStatus().observe(this, resource -> {
            if (resource == null) return;

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
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // 4. Gọi load dữ liệu lần đầu
        viewModel.loadProfile();
    }

    private void setupEvents() {
        btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        ivProfileAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Khi bấm lưu -> Gọi ViewModel xử lý
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();

            // Gọi hàm lưu bên ViewModel
            viewModel.saveProfile(name, email, birthday, selectedImageUri);
        });

        // Đăng xuất dùng ViewModel
        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
        ivAddWidgetIcon.setOnClickListener(v -> addWidgetToHomeScreen());
        ivAddAcWidgetIcon.setOnClickListener(v -> addActivityWidgetToHomeScreen());
    }

    // --- Các hàm Widget giữ nguyên ---
    private void addWidgetToHomeScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
            ComponentName myProvider = new ComponentName(this, com.example.nhom4.ui.page.widget.ActivityWidgetProvider.class);
            if (!appWidgetManager.isRequestPinAppWidgetSupported()) {
                Toast.makeText(this, "Launcher không hỗ trợ thêm widget", Toast.LENGTH_SHORT).show();
            } else {
                appWidgetManager.requestPinAppWidget(myProvider, null, null);
            }
        } else {
            Toast.makeText(this, "Yêu cầu Android 8.0 trở lên", Toast.LENGTH_SHORT).show();
        }
    }

    private void addActivityWidgetToHomeScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager manager = getSystemService(AppWidgetManager.class);
            ComponentName provider = new ComponentName(this, ActivityListWidgetProvider.class);
            if (manager.isRequestPinAppWidgetSupported()) {
                if (manager.requestPinAppWidget(provider, null, null)) {
                    Toast.makeText(this, "Đang thêm Widget...", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        Toast.makeText(this, "Vui lòng thêm Widget thủ công từ màn hình chính.", Toast.LENGTH_LONG).show();
    }
}