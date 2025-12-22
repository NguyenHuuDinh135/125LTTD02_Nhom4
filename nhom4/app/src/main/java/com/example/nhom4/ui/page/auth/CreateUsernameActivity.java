package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.ui.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * Màn hình đặt tên người dùng đầu tiên sau khi đăng ký, có kiểm tra trùng tên realtime.
 */
public class CreateUsernameActivity extends AppCompatActivity {

    private EditText etUsername;
    private MaterialButton btnContinue;

    private AuthViewModel viewModel;

    // Handler để Debounce (chờ người dùng gõ xong mới check)
    private final Handler handler = new Handler();
    private Runnable checkUsernameRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        View root = findViewById(android.R.id.content);
        root.setAlpha(0f);
        root.setTranslationY(40f);
        root.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350)
                .start();

        // 1. Init ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Ánh xạ View
        etUsername = findViewById(R.id.editTextUsername);
        btnContinue = findViewById(R.id.buttonContinue);

        // Mặc định disable nút tiếp tục
        setButtonEnabled(false);

        // 3. Setup Events
        setupTextWatcher();

        btnContinue.setOnClickListener(v -> {
            btnContinue.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        String username = etUsername.getText().toString().trim();
                        viewModel.createUserProfile(username);

                        // Trả lại scale bình thường
                        btnContinue.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start();
                    })
                    .start();
        });

        // 4. Observe ViewModel
        observeViewModel();
    }

    /**
     * Lắng nghe thay đổi text để debounce việc kiểm tra tên người dùng.
     */
    private void setupTextWatcher() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi gõ, disable nút trước
                setButtonEnabled(false);
                etUsername.setError(null); // Xóa lỗi cũ

                // Hủy lệnh kiểm tra cũ nếu người dùng vẫn đang gõ
                if (checkUsernameRunnable != null) {
                    handler.removeCallbacks(checkUsernameRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    // Chờ 600ms sau khi ngừng gõ mới gọi ViewModel kiểm tra
                    checkUsernameRunnable = () -> viewModel.checkUsername(input);
                    handler.postDelayed(checkUsernameRunnable, 600);
                } else {
                    // Nếu rỗng thì không check và giữ nút disable
                    setButtonEnabled(false);
                }
            }
        });
    }

    /**
     * Theo dõi kết quả từ ViewModel cho việc kiểm tra và tạo profile.
     */
    private void observeViewModel() {
        // 1. Quan sát kết quả kiểm tra Username
        viewModel.getUsernameCheckResult().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                boolean isExists = resource.data;
                if (isExists) {
                    etUsername.setError("Tên người dùng này đã được sử dụng!");
                    setButtonEnabled(false);
                } else {
                    etUsername.setError(null); // Hợp lệ
                    setButtonEnabled(true);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Lỗi kiểm tra: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Quan sát kết quả Lưu Profile (Nút Continue)
        viewModel.getCreateProfileResult().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    setButtonEnabled(false);
                    btnContinue.setText("Đang xử lý...");
                    break;
                case SUCCESS:
                    // Chuyển sang màn hình Add Friend
                    Intent intent = new Intent(CreateUsernameActivity.this, AddFriendActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case ERROR:
                    setButtonEnabled(true);
                    btnContinue.setText("Tiếp tục");
                    Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    /**
     * Helper bật/tắt nút Tiếp tục kèm hiệu ứng mờ.
     */
    private void setButtonEnabled(boolean enabled) {
        btnContinue.setEnabled(enabled);
        btnContinue.animate()
                .alpha(enabled ? 1f : 0.5f)
                .scaleX(enabled ? 1f : 0.95f)
                .scaleY(enabled ? 1f : 0.95f)
                .setDuration(200)
                .start();
        btnContinue.setAlpha(enabled ? 1.0f : 0.5f);
    }
}
