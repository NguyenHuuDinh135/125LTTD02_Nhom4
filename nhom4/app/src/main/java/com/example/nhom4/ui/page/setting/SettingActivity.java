package com.example.nhom4.ui.page.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.model.UserProfile;
import com.example.nhom4.ui.page.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvUserName, tvEmail, tvBirthday;
    private Button btnChangePhoto;
    private MaterialButton btnBack;
    private ProgressBar progressBar;

    private UserProfileViewModel viewModel;
    private String currentUserUid;

    private RecyclerView recyclerViewSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // UID hiện tại
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBirthday = findViewById(R.id.tvBirthday);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        recyclerViewSettings = findViewById(R.id.recyclerViewSettings);

        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        loadUserProfile();

        btnBack.setOnClickListener(v -> finish());

        btnChangePhoto.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đổi ảnh chưa triển khai", Toast.LENGTH_SHORT).show());

        findViewById(R.id.rowDeleteAccount).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng xóa tài khoản chưa triển khai", Toast.LENGTH_SHORT).show());

        findViewById(R.id.rowLogout).setOnClickListener(v -> {
            viewModel.logoutUser();
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        viewModel.getUserProfileLiveData().observe(this, profile -> {
            progressBar.setVisibility(View.GONE);
            if (profile != null) {
                // Hiển thị thông tin profile
                tvUserName.setText(profile.getUsername() != null ? profile.getUsername() : "Chưa có tên");
                tvEmail.setText(profile.getEmail() != null ? profile.getEmail() : "Chưa có email");
                tvBirthday.setText(profile.getBirthday() != null ? profile.getBirthday() : "Chưa có ngày sinh");

                // Glide
                String avatarUrl = profile.getProfilePhotoUrl();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.avatar)
                            .into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.avatar);
                }

                // Cập nhật RecyclerView
                List<SettingItem> items = new ArrayList<>();
                items.add(new SettingItem("Tên hiển thị", profile.getUsername(), "username"));
                items.add(new SettingItem("Ngày sinh", profile.getBirthday(), "birthday"));
                items.add(new SettingItem("Email", profile.getEmail(), "email"));

                SettingAdapter adapter = new SettingAdapter(this, items, viewModel, currentUserUid);
                recyclerViewSettings.setAdapter(adapter);
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            progressBar.setVisibility(View.GONE);
            if ("User profile not found".equals(error)) {
                // Tạo profile mặc định
                String defaultUsername = "Chưa có tên";
                String defaultEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                        FirebaseAuth.getInstance().getCurrentUser().getEmail() : "chưa có email";
                UserProfile defaultProfile = new UserProfile(
                        currentUserUid,
                        defaultUsername,
                        defaultEmail,
                        null,
                        null,
                        null
                );

                // Lưu profile mặc định vào Firestore
                viewModel.updateUserProfile(defaultProfile);

                // Load lại profile sau khi tạo profile mặc định
                viewModel.loadUserProfile(currentUserUid);
            } else if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load dữ liệu từ Firestore
        viewModel.loadUserProfile(currentUserUid);
    }

}
