package com.example.nhom4.data.bean;

import java.util.List;

public class Conversation {
    private String conversationId; // ID của document hội thoại
    private List<String> members;  // Danh sách các thành viên trong hội thoại [uid1, uid2]

    private String friendId;
    private String friendName;
    private String friendAvatar;
    private String lastMessage;
    private long timestamp;

    // Constructor mặc định (bắt buộc cho Firestore)
    public Conversation() { }

    // Constructor đầy đủ
    public Conversation(String friendId, String friendName, String friendAvatar, String lastMessage, long timestamp) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendAvatar = friendAvatar;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // --- Getter và Setter cho các trường bị thiếu (Nguyên nhân gây lỗi) ---

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    // --- Getter và Setter các trường cũ ---

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getFriendAvatar() { return friendAvatar; }
    public void setFriendAvatar(String friendAvatar) { this.friendAvatar = friendAvatar; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
