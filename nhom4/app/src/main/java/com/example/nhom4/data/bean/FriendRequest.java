package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;

import java.util.Arrays;
import java.util.List;

/**
 * Bean đại diện cho một lời mời kết bạn (relationship document trong Firestore).
 * Sử dụng chung cho cả pending, accepted, declined.
 */
public class FriendRequest {
    private String requestId;           // ID của document trong collection "relationships"
    private String senderId;            // Người gửi lời mời (requester)
    private String receiverId;          // Người nhận lời mời (recipient)
    private String status;              // "pending", "accepted", "declined"
    private List<String> members;       // Mảng [senderId, receiverId] để query dễ dàng
    private Timestamp createdAt;        // Thời gian tạo lời mời
    private Timestamp updatedAt;        // Thời gian cập nhật (khi accept/decline)

    // Thông tin bổ sung (không lưu trong Firestore, load thêm khi cần)
    private User sender;                // User object của người gửi (load từ users collection)

    // Constructor mặc định (bắt buộc cho Firestore toObject())
    public FriendRequest() {}

    // Constructor tiện lợi khi tạo mới lời mời
    public FriendRequest(String senderId, String receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = "pending";
        this.members = Arrays.asList(senderId, receiverId);
        this.createdAt = Timestamp.now();
    }

    // --- Getter & Setter ---

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }
}