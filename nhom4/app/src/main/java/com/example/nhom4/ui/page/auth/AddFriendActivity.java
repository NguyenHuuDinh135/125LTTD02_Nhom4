package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
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
        setupRecyclerView();
        setupEvents();
        observeViewModel();
    }

    private void initViews() {
        btnContinue = findViewById(R.id.btn_continue);
        recyclerView = findViewById(R.id.recycler_view_suggestions);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupRecyclerView() {
        // Truyền callback khi user bấm "Add Friend" trên item
        adapter = new UserSuggestionAdapter(new ArrayList<>(), this::showConfirmDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupEvents() {
        // Xử lý nút Back trên Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Sự kiện nút Tiếp tục -> Vào Main
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void observeViewModel() {
        // 1. Lắng nghe danh sách User gợi ý
        viewModel.getUsers().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                // Lưu ý: Cần cập nhật UserSuggestionAdapter để có phương thức setList
                // Trong bài trước tôi chưa thêm setList cho adapter này, bạn có thể dùng:
                // adapter = new UserSuggestionAdapter(resource.data, ...); recyclerView.setAdapter(adapter);
                // HOẶC TỐT NHẤT LÀ CẬP NHẬT ADAPTER CÓ HÀM setList()

                // Cách tạm thời nếu Adapter chưa có setList: tạo mới adapter
                if (resource.data != null) {
                    adapter = new UserSuggestionAdapter(resource.data, this::showConfirmDialog);
                    recyclerView.setAdapter(adapter);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Lắng nghe trạng thái gửi kết bạn
        viewModel.getRequestStatus().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    // Có thể hiện loading nhỏ nếu muốn
                    break;
                case SUCCESS:
                    Toast.makeText(this, "Đã gửi lời mời!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
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
