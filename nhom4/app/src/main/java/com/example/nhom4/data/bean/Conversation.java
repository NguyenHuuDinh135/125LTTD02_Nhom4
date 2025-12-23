package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp; // Nhớ import cái này
import java.util.List;

public class Conversation {
    private String conversationId;
    private List<String> members;

    private String friendId;
    private String friendName;
    private String friendAvatar;
    private String lastMessage;

    // Sửa lại: Dùng Timestamp để hứng dữ liệu từ Firestore
    private Timestamp lastMessageAt;

    public Conversation() { }

    // --- Getter & Setter ---
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getFriendAvatar() { return friendAvatar; }
    public void setFriendAvatar(String friendAvatar) { this.friendAvatar = friendAvatar; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    // Getter/Setter cho Timestamp từ Firestore
    public Timestamp getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Timestamp lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    // Helper: Trả về milliseconds để sắp xếp list
    public long getTimestampLong() {
        return lastMessageAt != null ? lastMessageAt.toDate().getTime() : 0;
    }
}