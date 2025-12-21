package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.UserSuggestionAdapter;
import com.example.nhom4.ui.viewmodel.AddFriendViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

/**
 * Màn hình gợi ý kết bạn ngay sau bước tạo tài khoản.
 * Cho phép người dùng gửi lời mời và chỉ mở MainActivity khi đã gửi thành công ít nhất một yêu cầu.
 */
public class AddFriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserSuggestionAdapter adapter;
    private MaterialButton btnContinue;
    private MaterialToolbar toolbar;

    private AddFriendViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(AddFriendViewModel.class);

        initViews();

        btnContinue.setTranslationY(200f);
        btnContinue.setAlpha(0f);

        btnContinue.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // [FIX] Ban đầu disable nút (nếu muốn bắt buộc kết bạn mới được đi tiếp)
        // Nếu muốn cho phép Skip, hãy đổi thành setEnabled(true) và setAlpha(1.0f)
        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.5f);

        setupRecyclerView();
        setupEvents();
        observeViewModel();
    }

    /**
     * Ánh xạ view từ layout.
     */
    private void initViews() {
        btnContinue = findViewById(R.id.btn_continue);
        recyclerView = findViewById(R.id.recycler_view_suggestions);
        toolbar = findViewById(R.id.toolbar);
    }

    /**
     * Khởi tạo RecyclerView cùng adapter gợi ý bạn bè.
     */
    private void setupRecyclerView() {
        // Truyền callback khi user bấm "Add Friend" trên item
        adapter = new UserSuggestionAdapter(new ArrayList<>(), this::showConfirmDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Gắn sự kiện cho toolbar và nút tiếp tục.
     */
    private void setupEvents() {
        // Xử lý nút Back trên Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Sự kiện nút Tiếp tục -> Vào Main
        btnContinue.setOnClickListener(v -> {
            btnContinue.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        btnContinue.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start();

                        Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .start();
        });
    }

    /**
     * Đăng ký lắng nghe LiveData từ ViewModel để cập nhật UI.
     */
    private void observeViewModel() {
        // 1. Lắng nghe danh sách User gợi ý
        viewModel.getUsers().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                if (resource.data != null) {
                    // Cập nhật adapter
                    adapter = new UserSuggestionAdapter(resource.data, this::showConfirmDialog);
                    // Thay toàn bộ danh sách để tránh lỗi diff khi server trả về tập mới
                    recyclerView.setAdapter(adapter);
                    //Animation
                    recyclerView.setAlpha(0f);
                    recyclerView.setTranslationY(40f);

                    recyclerView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .start();
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Lắng nghe trạng thái gửi kết bạn
        viewModel.getRequestStatus().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    // Có thể show loading nhỏ trên nút nếu muốn
                    break;
                case SUCCESS:
                    Toast.makeText(this, "Đã gửi lời mời!", Toast.LENGTH_SHORT).show();

                    // [QUAN TRỌNG] Kích hoạt nút Tiếp tục sau khi gửi thành công
                    btnContinue.setEnabled(true);
                    btnContinue.setAlpha(1.0f);
                    break;

                case ERROR:
                    Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    // Nếu lỗi, vẫn có thể cho phép tiếp tục hoặc giữ nguyên disable tùy logic
                    break;
            }
        });
    }

    // Hiển thị Dialog xác nhận gửi lời mời
    private void showConfirmDialog(User targetUser) {
        new AlertDialog.Builder(this)
                .setTitle("Kết bạn")
                .setMessage("Gửi lời mời kết bạn tới " + targetUser.getUsername() + "?")
                .setPositiveButton("Gửi", (dialog, which) -> {
                    // Gọi ViewModel để gửi
                    viewModel.sendFriendRequest(targetUser);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
