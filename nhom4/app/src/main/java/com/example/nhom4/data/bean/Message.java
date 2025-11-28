package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;

public class Message {
    private String messageId;
    private String senderId;
    private String content;
    private String type; // "text" hoặc "post_reply"
    private Timestamp createdAt;

    // Thông tin thêm cho Post Widget
    private String replyPostId;
    private String replyPostImage;
    private String replyPostTitle;

    public Message() {}

    // Constructor cho tin nhắn thường
    public Message(String senderId, String content, String type) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.createdAt = Timestamp.now();
    }

    // Constructor cho Post Reply
    public Message(String senderId, String content, String type, String replyPostId, String replyPostImage, String replyPostTitle) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.createdAt = Timestamp.now();
        this.replyPostId = replyPostId;
        this.replyPostImage = replyPostImage;
        this.replyPostTitle = replyPostTitle;
    }

    // --- Getter & Setter ---
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getReplyPostId() { return replyPostId; }
    public void setReplyPostId(String replyPostId) { this.replyPostId = replyPostId; }

    public String getReplyPostImage() { return replyPostImage; }
    public void setReplyPostImage(String replyPostImage) { this.replyPostImage = replyPostImage; }

    public String getReplyPostTitle() { return replyPostTitle; }
    public void setReplyPostTitle(String replyPostTitle) { this.replyPostTitle = replyPostTitle; }
}
