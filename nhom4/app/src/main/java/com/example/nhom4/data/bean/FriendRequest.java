package com.example.nhom4.data.bean;

import java.util.Arrays;
import java.util.List;

public class FriendRequest {

    private String requestId;     // ID document trên Firestore
    private String requesterId;   // người gửi yêu cầu
    private String recipientId;   // người nhận
    private String status;        // pending / accepted / declined
    private List<String> members; // [requesterId, recipientId]

    // Constructor mặc định (bắt buộc cho Firestore)
    public FriendRequest() {}

    // Constructor tiện lợi khi tạo mới
    public FriendRequest(String requesterId, String recipientId) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = "pending";
        this.members = Arrays.asList(requesterId, recipientId);
    }

    // --- Getter / Setter ---
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
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
}
