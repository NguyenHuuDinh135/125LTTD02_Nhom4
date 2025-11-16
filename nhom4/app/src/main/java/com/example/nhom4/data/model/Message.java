package com.example.nhom4.data.model;

import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String receiverId;
    private String content;
    private long timestamp;
    private boolean isRead;
    private String type; // text, image, file

    public Message() {
        // Constructor rỗng bắt buộc cho Firebase
    }

    public Message(String senderId, String senderName, String senderAvatar,
                   String receiverId, String content, String type) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.isRead = false;
    }

    // Chuyển sang Map để lưu vào Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("senderAvatar", senderAvatar);
        result.put("receiverId", receiverId);
        result.put("content", content);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("isRead", isRead);
        result.put("type", type);
        return result;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
