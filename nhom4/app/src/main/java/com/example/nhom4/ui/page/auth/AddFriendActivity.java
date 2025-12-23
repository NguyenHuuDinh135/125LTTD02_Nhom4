package com.example.nhom4.ui.page.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AddFriendActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerView;
    private UserSuggestionAdapter adapter;
    private MaterialButton btnContinue;
    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnSeeMore;

    // Layout containers for Invite actions
    private View itemMessenger, itemInstagram, itemTwitter, itemOther;
    private View inviteMessenger, inviteFacebook, inviteInstagram, inviteOther;

    // Logic Variables
    private AddFriendViewModel viewModel;
    private List<User> originalList = new ArrayList<>(); // Danh sách gốc để lọc
    private Timer searchTimer;
    private boolean isExpanded = false; // Trạng thái nút Xem thêm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        viewModel = new ViewModelProvider(this).get(AddFriendViewModel.class);

        initViews();
        setupAnimation();
        setupRecyclerView();
        setupSearchListener(); // Logic tìm kiếm
        setupInviteActions();  // Logic mời tải app
        setupEvents();         // Logic nút Tiếp tục & Xem thêm
        observeViewModel();
    }

    private void initViews() {
        btnContinue = findViewById(R.id.btn_continue);
        recyclerView = findViewById(R.id.recycler_view_suggestions);
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        btnSeeMore = findViewById(R.id.btn_see_more);

        // Ánh xạ các nút icon mạng xã hội (từ thẻ include trong XML)
        itemMessenger = findViewById(R.id.item_messenger);
        itemInstagram = findViewById(R.id.item_instagram);
        itemTwitter = findViewById(R.id.item_twitter);
        itemOther = findViewById(R.id.item_other);

        // Ánh xạ các dòng mời dọc
        inviteMessenger = findViewById(R.id.invite_messenger);
        inviteFacebook = findViewById(R.id.invite_facebook);
        inviteInstagram = findViewById(R.id.invite_instagram);
        inviteOther = findViewById(R.id.invite_other);
    }

    private void setupAnimation() {
        // Animation nút Continue trồi lên
        btnContinue.setTranslationY(200f);
        btnContinue.setAlpha(0f);
        btnContinue.animate().translationY(0f).alpha(1f).setDuration(350)
                .setInterpolator(new DecelerateInterpolator()).start();

        // Mặc định disable cho đến khi user làm gì đó (tùy logic)
        btnContinue.setEnabled(true);
    }

    private void setupRecyclerView() {
        adapter = new UserSuggestionAdapter(new ArrayList<>(), this::showConfirmDialog);
        // Mặc định hiển thị chế độ thu gọn (Limited)
        adapter.setLimitedMode(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // --- 1. LOGIC TÌM KIẾM (SEARCH) ---
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchTimer != null) searchTimer.cancel();
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> filterUsers(s.toString()));
                    }
                }, 300); // Debounce 300ms
            }
        });
    }

    private void filterUsers(String keyword) {
        String query = keyword.trim().toLowerCase(Locale.getDefault());

        if (query.isEmpty()) {
            // Nếu không tìm kiếm -> Trả về danh sách gốc & Khôi phục chế độ xem thêm
            adapter.setUsers(originalList);
            adapter.setLimitedMode(!isExpanded); // Giữ trạng thái expand hiện tại
            updateSeeMoreButtonVisibility(originalList.size());
        } else {
            // Đang tìm kiếm -> Lọc & Luôn hiển thị full (tắt limited mode)
            List<User> filtered = new ArrayList<>();
            for (User u : originalList) {
                if (u.getUsername() != null && u.getUsername().toLowerCase().contains(query)) {
                    filtered.add(u);
                }
            }
            adapter.setUsers(filtered);
            adapter.setLimitedMode(false); // Khi search thì hiện hết kết quả
            btnSeeMore.setVisibility(View.GONE); // Ẩn nút xem thêm khi đang search
        }
    }

    // --- 2. LOGIC NÚT "XEM THÊM" ---
    private void setupEvents() {
        toolbar.setNavigationOnClickListener(v -> finish());

        // Xử lý nút Xem thêm / Thu gọn
        btnSeeMore.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            adapter.setLimitedMode(!isExpanded); // Đảo ngược chế độ

            // Cập nhật text nút
            btnSeeMore.setText(isExpanded ? "Thu gọn" : "Xem thêm");
        });

        // Nút Tiếp tục
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateSeeMoreButtonVisibility(int listSize) {
        // Chỉ hiện nút nếu danh sách > 5 phần tử
        if (listSize > 5) {
            btnSeeMore.setVisibility(View.VISIBLE);
            btnSeeMore.setText(isExpanded ? "Thu gọn" : "Xem thêm");
        } else {
            btnSeeMore.setVisibility(View.GONE);
        }
    }

    // --- 3. LOGIC MỜI BẠN BÈ (INVITE SHARES) ---
    private void setupInviteActions() {
        // Link tải app giả lập
        String inviteMessage = "Tải ngay AppNhom4 để chat với tui nhé! Link: https://example.com/download";

        // Gắn sự kiện cho các Icon hàng ngang
        View.OnClickListener inviteListener = v -> shareAppInvite(inviteMessage);

        if (itemMessenger != null) itemMessenger.setOnClickListener(inviteListener);
        if (itemInstagram != null) itemInstagram.setOnClickListener(inviteListener);
        if (itemTwitter != null) itemTwitter.setOnClickListener(inviteListener);
        if (itemOther != null) itemOther.setOnClickListener(inviteListener);

        // Gắn sự kiện cho các dòng mời dọc
        if (inviteMessenger != null) inviteMessenger.setOnClickListener(inviteListener);
        if (inviteFacebook != null) inviteFacebook.setOnClickListener(inviteListener);
        if (inviteInstagram != null) inviteInstagram.setOnClickListener(inviteListener);
        if (inviteOther != null) inviteOther.setOnClickListener(inviteListener);
    }

    /**
     * Mở hộp thoại chia sẻ của hệ thống (System Share Sheet)
     */
    private void shareAppInvite(String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Lời mời kết bạn");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(intent, "Mời bạn bè qua..."));
    }

    // --- VIEWMODEL OBSERVER ---
    private void observeViewModel() {
        viewModel.getUsers().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                originalList = new ArrayList<>(resource.data);

                // Cập nhật adapter
                adapter.setUsers(resource.data);

                // Cập nhật trạng thái nút Xem thêm dựa trên số lượng user tải về
                updateSeeMoreButtonVisibility(resource.data.size());
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRequestStatus().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Đã gửi lời mời!", Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmDialog(User targetUser) {
        new AlertDialog.Builder(this)
                .setTitle("Kết bạn")
                .setMessage("Gửi lời mời kết bạn tới " + targetUser.getUsername() + "?")
                .setPositiveButton("Gửi", (dialog, which) -> viewModel.sendFriendRequest(targetUser))
                .setNegativeButton("Hủy", null)
                .show();
    }
}