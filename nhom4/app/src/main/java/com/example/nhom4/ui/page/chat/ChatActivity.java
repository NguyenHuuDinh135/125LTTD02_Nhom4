package com.example.nhom4.ui.page.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.ChatAdapter;
import com.example.nhom4.ui.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;
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
    private TextView tvTitle;

    private String conversationId;
    private String partnerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox); // Giả sử bạn dùng lại layout chatbox

        // Lấy dữ liệu Intent
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        partnerId = getIntent().getStringExtra("PARTNER_ID");

        if (conversationId == null) {
            Toast.makeText(this, "Lỗi cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();
        setupRecyclerView();
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
    }

    // Load thông tin người kia để hiển thị lên Toolbar (Tạm thời gọi Firestore trực tiếp ở đây cho đơn giản UI)
    // Trong thực tế nên chuyển vào ViewModel loadPartnerInfo()
    /**
     * Tạm thời lấy thông tin user đối tác từ Firestore để hiển thị tiêu đề.
     */
    private void loadPartnerInfo() {
        if (partnerId != null) {
            FirebaseFirestore.getInstance().collection("users").document(partnerId)
                    .get().addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null && getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(user.getUsername());
                        }
                    });
        }
    }
}
