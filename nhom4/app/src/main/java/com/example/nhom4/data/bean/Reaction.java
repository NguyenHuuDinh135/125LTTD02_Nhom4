package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Reaction {
    private String userId;
    private String userName;
    private String userAvatar;
    private String emoji; // Ví dụ: "❤️", "😂"
    @ServerTimestamp
    private Timestamp createdAt;

    public Reaction() {}

    public Reaction(String userId, String userName, String userAvatar, String emoji) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.emoji = emoji;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getEmoji() { return emoji; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters cần thiết cho Firestore
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}