package com.example.nhom4.utils;

import com.example.nhom4.data.model.Message;
import java.util.ArrayList;
import java.util.List;

public class MockDataHelper {

    // Mock data cho chat 1-1
    public static List<Message> getMockSingleChatMessages(String currentUserId) {
        List<Message> messages = new ArrayList<>();
        long baseTime = System.currentTimeMillis();

        // Tin nhắn 1
        messages.add(createMessage(
                "user456", "Nguyễn Văn A", currentUserId,
                "Chào bạn!", baseTime - 300000
        ));

        // Tin nhắn 2
        messages.add(createMessage(
                currentUserId, "Tôi", "user456",
                "Xin chào, bạn khỏe không?", baseTime - 240000
        ));

        // Tin nhắn 3
        messages.add(createMessage(
                "user456", "Nguyễn Văn A", currentUserId,
                "Mình khỏe, cảm ơn bạn!", baseTime - 180000
        ));

        // Tin nhắn 4
        messages.add(createMessage(
                currentUserId, "Tôi", "user456",
                "Hôm nay có rảnh không?", baseTime - 120000
        ));

        // Tin nhắn 5
        messages.add(createMessage(
                "user456", "Nguyễn Văn A", currentUserId,
                "OK! 😊", baseTime - 60000
        ));

        return messages;
    }

    // Mock data cho chat nhóm
    public static List<Message> getMockGroupChatMessages(String currentUserId) {
        List<Message> messages = new ArrayList<>();
        long baseTime = System.currentTimeMillis();

        messages.add(createMessage(
                "user456", "Nguyễn Văn A", "group123",
                "Xin chào cả nhóm!", baseTime - 600000
        ));

        messages.add(createMessage(
                "user789", "Trần Thị B", "group123",
                "Chào mọi người!", baseTime - 540000
        ));

        messages.add(createMessage(
                currentUserId, "Tôi", "group123",
                "Hi các bạn!", baseTime - 480000
        ));

        messages.add(createMessage(
                "user456", "Nguyễn Văn A", "group123",
                "Cuối tuần đi chơi không?", baseTime - 420000
        ));

        messages.add(createMessage(
                "user789", "Trần Thị B", "group123",
                "Đi chứ! 🎉", baseTime - 360000
        ));

        messages.add(createMessage(
                currentUserId, "Tôi", "group123",
                "Tuyệt vời!", baseTime - 300000
        ));

        return messages;
    }

    private static Message createMessage(String senderId, String senderName,
                                         String receiverId, String content,
                                         long timestamp) {
        Message message = new Message(
                senderId,
                senderName,
                "",
                receiverId,
                content,
                "text"
        );
        message.setTimestamp(timestamp);
        return message;
    }
}