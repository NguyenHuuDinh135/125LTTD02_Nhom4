package com.example.nhom4.ui.page.chat;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.ChatAdapter;
import com.example.nhom4.ui.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatActivity extends AppCompatActivity {

    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private MaterialButton btnSend;
    private Toolbar toolbar;
    private MaterialTextView tvName;
    private MaterialTextView tvStatus;
    private ShapeableImageView imgAvatar;
    private MaterialButton btnMore;
    private MaterialButton btnOption;
    private EmojiPickerView emojiPicker;
    private boolean isEmojiShowing = false;

    private String conversationId;
    private String partnerId;
    private String partnerName;
    private String partnerAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        // 1. Nhận dữ liệu từ Intent
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        partnerId = getIntent().getStringExtra("PARTNER_ID");
        partnerName = getIntent().getStringExtra("PARTNER_NAME");
        partnerAvatar = getIntent().getStringExtra("PARTNER_AVATAR");

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();
        setupRecyclerView();
        setupMoreButton();
        setupEmojiLogic();
        loadPartnerInfo();
        observeViewModel();

        // 2. Logic Xử lý ID hội thoại (FIX LỖI KHÔNG VÀO ĐƯỢC CHAT)
        if (conversationId != null && !conversationId.isEmpty()) {
            // Trường hợp 1: Đã có ID (chat cũ) -> Lắng nghe tin nhắn ngay
            viewModel.startListening(conversationId);
        } else if (partnerId != null) {
            // Trường hợp 2: Chưa có ID (người mới) -> Gọi ViewModel tìm hoặc tạo ID
            // Tạm thời disable nút gửi để tránh lỗi
            btnSend.setEnabled(false);
            btnSend.setAlpha(0.5f);
            viewModel.findOrCreateConversation(partnerId);
        } else {
            Toast.makeText(this, "Lỗi: Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Sự kiện gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                if (conversationId != null) {
                    viewModel.sendMessage(conversationId, content);
                    etMessage.setText("");
                } else {
                    Toast.makeText(this, "Đang kết nối...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvName = findViewById(R.id.tvName);
        tvStatus = findViewById(R.id.tvStatus);
        imgAvatar = findViewById(R.id.iv_toolbar_avatar);
        btnMore = findViewById(R.id.btn_more);
        btnOption = findViewById(R.id.btnOption);
        emojiPicker = findViewById(R.id.emojiPicker);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(viewModel.getCurrentUserId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // 3. Lắng nghe kết quả tìm/tạo ID hội thoại (MỚI)
        viewModel.getConversationIdResult().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                conversationId = resource.data; // Cập nhật ID

                // Có ID rồi -> Bắt đầu load tin nhắn và mở khóa nút gửi
                viewModel.startListening(conversationId);
                btnSend.setEnabled(true);
                btnSend.setAlpha(1f);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Lỗi kết nối: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe tin nhắn về
        viewModel.getMessages().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                adapter.setMessages(resource.data);
                if (!resource.data.isEmpty()) {
                    recyclerView.smoothScrollToPosition(resource.data.size() - 1);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSendStatus().observe(this, resource -> {
            if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Gửi thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteResult().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadPartnerInfo() {
        // Ưu tiên hiển thị dữ liệu truyền từ Intent cho nhanh
        if (partnerName != null && !partnerName.isEmpty()) {
            tvName.setText(partnerName);
            tvStatus.setText("Đang hoạt động");
            if (partnerAvatar != null && !partnerAvatar.isEmpty()) {
                Glide.with(this).load(partnerAvatar).placeholder(R.drawable.avatar_placeholder).circleCrop().into(imgAvatar);
            }
            return;
        }

        // Nếu Intent thiếu thông tin, load lại từ Firestore
        if (partnerId != null) {
            FirebaseFirestore.getInstance().collection("users").document(partnerId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                tvName.setText(user.getUsername());
                                String avatarUrl = user.getProfilePhotoUrl();
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    Glide.with(ChatActivity.this).load(avatarUrl).placeholder(R.drawable.avatar_placeholder).circleCrop().into(imgAvatar);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> tvName.setText("Người dùng"));
        }
    }

    // --- Các hàm tiện ích UI (Giữ nguyên) ---
    private void setupMoreButton() {
        btnMore.setOnClickListener(v -> {
            View menuView = getLayoutInflater().inflate(R.layout.item_chat_more_menu, null);
            android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                    menuView,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            popupWindow.setElevation(10);
            TextView btnDelete = menuView.findViewById(R.id.action_delete_chat);
            btnDelete.setOnClickListener(view -> {
                popupWindow.dismiss();
                confirmDeleteConversation();
            });
            popupWindow.showAsDropDown(btnMore, 0, 0);
        });
    }

    private void confirmDeleteConversation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteConversation(conversationId);
                    if (partnerId != null) viewModel.unFriend(partnerId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupEmojiLogic() {
        emojiPicker.setOnEmojiPickedListener(emojiItem -> {
            int start = Math.max(etMessage.getSelectionStart(), 0);
            int end = Math.max(etMessage.getSelectionEnd(), 0);
            etMessage.getText().replace(Math.min(start, end), Math.max(start, end),
                    emojiItem.getEmoji(), 0, emojiItem.getEmoji().length());
        });

        btnOption.setOnClickListener(v -> {
            if (isEmojiShowing) showKeyboard(); else { hideKeyboard(); showEmojiPicker(); }
        });

        etMessage.setOnClickListener(v -> { if (isEmojiShowing) hideEmojiPicker(); });
        etMessage.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus && isEmojiShowing) hideEmojiPicker(); });
    }

    private void showEmojiPicker() {
        emojiPicker.setVisibility(View.VISIBLE);
        isEmojiShowing = true;
        btnOption.setIconResource(R.drawable.outline_keyboard_keys_24);
    }

    private void hideEmojiPicker() {
        emojiPicker.setVisibility(View.GONE);
        isEmojiShowing = false;
        btnOption.setIconResource(R.drawable.outline_add_reaction_24);
    }

    private void showKeyboard() {
        hideEmojiPicker();
        etMessage.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}