// D:/Documents/Projects/125LTTD02_Nhom4/nhom4/app/src/main/java/com/example/nhom4/ui/page/chatbox/ChatboxActivity.java

package com.example.nhom4.ui.page.chatbox;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast; // Import Toast để hiển thị lỗi
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Message;
import com.google.android.material.button.MaterialButton;


public class ChatboxActivity extends AppCompatActivity {
    private ChatboxAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private MaterialButton btnSend;

    private ChatboxViewModel viewModel;

    // --- Giả sử các ID này được truyền từ màn hình trước ---
    private String currentUserId = "p3J7FY4cjsZbYPBpmwCc44MfQtq2"; // ID của người dùng hiện tại
    private String conversationId = "OpGMIA2nZ8cjCMfow41Z"; // ID của cuộc trò chuyện

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        // Lấy dữ liệu được truyền từ Intent
        // Các Activity khác khi chuyển tiê đến Activity này sẽ phải truyền thêm tham số sau
        // conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        // currentUserId = getCurrentUserFromAuth(); // Lấy từ Firebase Auth

        // --- Khởi tạo ViewModel ---
        viewModel = new ViewModelProvider(this).get(ChatboxViewModel.class);
        viewModel.init(conversationId); // Rất quan trọng: Khởi tạo conversationId cho ViewModel

        // Khởi tạo views
        recyclerView = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Setup RecyclerView
        setupRecyclerView();

        // --- Bắt đầu lắng nghe dữ liệu từ ViewModel ---
        observeMessages();
        observeErrors();

        // --- Bắt đầu gửi tin nhắn thật ---
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        // false = single chat, true = group chat
        adapter = new ChatboxAdapter(currentUserId, false);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // Tạm thời tắt để cuộn mượt hơn khi load
        recyclerView.setLayoutManager(layoutManager);
    }

    // --- Lắng nghe danh sách tin nhắn từ ViewModel ---
    private void observeMessages() {
        viewModel.getMessages().observe(this, messages -> {
            // Khi có dữ liệu mới từ Firestore, cập nhật adapter
            adapter.setMessages(messages);
            // Cuộn xuống tin nhắn cuối cùng
            if (messages != null && !messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });
    }

    // --- Lắng nghe các lỗi có thể xảy ra ---
    private void observeErrors() {
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Gửi tin nhắn thông qua ViewModel ---
    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        // Tạo một object Message mới
        Message newMessage = new Message(
                currentUserId,
                content,
                "text" // Kiểu tin nhắn là text
        );

        // Gửi tin nhắn qua ViewModel
        viewModel.sendMessage(newMessage);

        // Clear ô nhập liệu
        etMessage.setText("");
    }
}