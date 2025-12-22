package com.example.nhom4.ui.page.chat;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Màn hình chat 1-1: hiển thị tin nhắn, gửi tin mới và load thông tin đối tác.
 */
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
    private  MaterialButton btnOption;
    private EmojiPickerView emojiPicker;
    private  boolean isEmojiShowing = false;


    private String conversationId;
    private String partnerId;
    private String partnerName;
    private String partnerAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox); // Giả sử bạn dùng lại layout chatbox

        // Lấy dữ liệu Intent
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        partnerId = getIntent().getStringExtra("PARTNER_ID");
        partnerName = getIntent().getStringExtra("PARTNER_NAME");
        partnerAvatar = getIntent().getStringExtra("PARTNER_AVATAR");

        if (conversationId == null) {
            Toast.makeText(this, "Lỗi cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();
        setupRecyclerView();
        setupMoreButton(btnMore);
        setupEmojiLogic();
        loadPartnerInfo(); // Load tên người chat cho đẹp

        // Bắt đầu lắng nghe tin nhắn
        viewModel.startListening(conversationId);
        observeViewModel();

        // Sự kiện gửi
        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString();
            viewModel.sendMessage(conversationId, content); // ViewModel tự bỏ qua tin rỗng
        });
    }

    /**
     * Ánh xạ View và cấu hình toolbar (nếu tồn tại).
     */
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

        // Setup Toolbar (Tùy chỉnh theo layout của bạn)
        toolbar = findViewById(R.id.toolbar); // Nếu có
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Xóa title mặc định
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    /**
     * Chuẩn bị RecyclerView với ChatAdapter và layout stackBottom.
     */
    private void setupRecyclerView() {
        adapter = new ChatAdapter(viewModel.getCurrentUserId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Tin nhắn mới ở dưới cùng
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupMoreButton(MaterialButton btnMore) {
        btnMore.setOnClickListener(v -> {
            // 1. Inflate layout từ file xml custom (item_chat_more_menu.xml)
            android.view.View menuView = android.view.LayoutInflater.from(this).inflate(R.layout.item_chat_more_menu, null);

            // 2. Tạo PopupWindow
            android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                    menuView,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    true // Cho phép bấm ra ngoài để tắt
            );

            // Thêm bóng đổ (elevation)
            popupWindow.setElevation(10);

            // 3. Xử lý sự kiện click cho nút "Xóa cuộc trò chuyện" trong menu
            TextView btnDelete = menuView.findViewById(R.id.action_delete_chat);
            btnDelete.setOnClickListener(view -> {
                popupWindow.dismiss(); // Đóng menu
                confirmDeleteConversation(); // Gọi hàm xác nhận xóa
            });

            // 4. Hiển thị Popup ngay bên dưới nút More, lệch sang trái một chút (-150, 0) để đẹp hơn
            popupWindow.showAsDropDown(btnMore, 0, 0);
        });
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi xóa.
     */
    private void confirmDeleteConversation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa cuộc trò chuyện")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ tin nhắn với người này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteConversation(conversationId);
                    viewModel.unFriend(partnerId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xử lý sự kiện khi chọn emoji từ bảng.
     */
    private void setupEmojiLogic() {
        // 1. Xử lý sự kiện khi chọn 1 emoji từ bảng
        emojiPicker.setOnEmojiPickedListener(emojiItem -> {
            // Chèn emoji vào vị trí con trỏ trong EditText
            int start = Math.max(etMessage.getSelectionStart(), 0);
            int end = Math.max(etMessage.getSelectionEnd(), 0);
            etMessage.getText().replace(Math.min(start, end), Math.max(start, end), emojiItem.getEmoji(), 0, emojiItem.getEmoji().length());
        });

        // 2. Xử lý nút Option (Toggle giữa Phím và Emoji)
        btnOption.setOnClickListener(v -> {
            if (isEmojiShowing) {
                // Đang hiện Emoji -> Chuyển sang hiện Bàn phím
                showKeyboard();
            } else {
                // Đang hiện Bàn phím (hoặc đóng) -> Chuyển sang hiện Emoji
                hideKeyboard();
                showEmojiPicker();
            }
        });

        // 3. Khi bấm vào ô nhập liệu -> Tự động ẩn Emoji để hiện bàn phím
        etMessage.setOnClickListener(v -> {
            if (isEmojiShowing) {
                hideEmojiPicker();
                // isEmojiShowing sẽ được set false trong hàm hide
            }
        });

        // Lắng nghe focus để xử lý mượt hơn
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isEmojiShowing) {
                hideEmojiPicker();
            }
        });
    }

    /**
     * Hiển thị bảng emoji
     */
    private void showEmojiPicker() {
        emojiPicker.setVisibility(View.VISIBLE);
        isEmojiShowing = true;
        // Đổi icon nút Option thành icon bàn phím (nếu muốn)
        btnOption.setIconResource(R.drawable.outline_keyboard_keys_24);
    }

    /**
     * Ẩn bảng emoji
     */
    private void hideEmojiPicker() {
        emojiPicker.setVisibility(View.GONE);
        isEmojiShowing = false;
        // Đổi icon nút Option lại thành mặt cười/cộng
        btnOption.setIconResource(R.drawable.outline_add_reaction_24);
    }

    /**
     * Hiển thị bàn phím
     */
    private void showKeyboard() {
        hideEmojiPicker();
        etMessage.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Ẩn bàn phím
     */
    private void hideKeyboard() {
        android.view.View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Theo dõi luồng tin nhắn và trạng thái gửi để cập nhật UI.
     */
    private void observeViewModel() {
        // Quan sát tin nhắn
        viewModel.getMessages().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                adapter.setMessages(resource.data);
                // Cuộn xuống cuối khi có tin nhắn mới
                if (!resource.data.isEmpty()) {
                    recyclerView.smoothScrollToPosition(resource.data.size() - 1);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát trạng thái gửi (để clear ô nhập)
        viewModel.getSendStatus().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                etMessage.setText(""); // Clear ô nhập
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Gửi lỗi", Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát xóa conversation
        viewModel.getDeleteResult().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(this, "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại màn hình trước
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load thông tin người kia để hiển thị lên Toolbar (Tạm thời gọi Firestore trực tiếp ở đây cho đơn giản UI)
    // Trong thực tế nên chuyển vào ViewModel loadPartnerInfo()
    /**
     * Tạm thời lấy thông tin user đối tác từ Firestore để hiển thị tiêu đề.
     */
    private void loadPartnerInfo() {
        // 1. Ưu tiên hiển thị dữ liệu từ Intent (Nhanh nhất)
        if (partnerName != null && !partnerName.isEmpty()) {
            tvName.setText(partnerName);
            // Tương tự cho avatar nếu cần
            tvStatus.setText("Đang hoạt động");
            if (partnerAvatar != null) {
                // Tải avatar bằng Glide
                Glide.with(this)
                    .load(partnerAvatar)                        // 1. Lấy ảnh từ link này
                    .placeholder(R.drawable.avatar_placeholder) // 2. (Tùy chọn) Hiện ảnh này trong lúc chờ tải
                    .error(R.drawable.avatar_placeholder)       // 3. (Tùy chọn) Hiện ảnh này nếu link lỗi
                    .centerCrop()                               // 4. Cắt ảnh cho vừa khung
                    .into(imgAvatar);
            }
            return;
        }

        // 2. Phương án dự phòng (Fallback): Chỉ gọi API khi Intent không có tên (ví dụ mở từ Notification)
        if (partnerId != null) {
            FirebaseFirestore.getInstance().collection("users").document(partnerId)
                    .get().addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            // Cập nhật tên vào TextView custom
                            tvName.setText(user.getUsername());
                            tvStatus.setText("Đang hoạt động");

                            // Cập nhật Avatar nếu có link
                            String avatarUrl = user.getProfilePhotoUrl(); // Giả sử User model có field này
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(ChatActivity.this)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.avatar_placeholder)
                                        .error(R.drawable.avatar_placeholder)
                                        .centerCrop()
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.avatar_placeholder);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi nếu cần (ví dụ: hiện tên mặc định)
                        tvName.setText("Người dùng");
                    });
        }

    }
}
