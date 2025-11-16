package com.example.nhom4.ui.page.chatbox;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom4.R;
import com.example.nhom4.data.model.Message;
import com.example.nhom4.utils.MockDataHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ChatboxActivity extends AppCompatActivity {
    private ChatboxAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private MaterialButton btnSend;

    private String currentUserId = "user123";
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        // Khởi tạo views
        recyclerView = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // Test chat 1-1
        adapter = new ChatboxAdapter(currentUserId, false); // false = single chat

        // Test chat nhóm
//        adapter = new ChatboxAdapter(currentUserId, true); // true = group chat

        recyclerView.setAdapter(adapter);

        // Load mock data
        loadMockData();

        // Test gửi tin nhắn
        btnSend.setOnClickListener(v -> sendMockMessage());
    }

    private void loadMockData() {
        // Test chat 1-1
        messageList = MockDataHelper.getMockSingleChatMessages(currentUserId);

        // Hoặc test chat nhóm
        // messageList = MockDataHelper.getMockGroupChatMessages(currentUserId);
        // adapter = new ChatAdapter(currentUserId, true); // Nhớ set true cho group chat

        adapter.setMessages(messageList);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendMockMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // Tạo tin nhắn mới
        Message newMessage = new Message(
                currentUserId,
                "Tôi",
                "",
                "user456",
                content,
                "text"
        );
        newMessage.setTimestamp(System.currentTimeMillis());

        // Thêm vào danh sách
        messageList.add(newMessage);
        adapter.setMessages(messageList);

        // Scroll xuống cuối
        recyclerView.smoothScrollToPosition(messageList.size() - 1);

        // Clear input
        etMessage.setText("");
    }
}