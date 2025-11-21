package com.example.nhom4.data.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Message {

    private String messageId;
    private String senderId;
    private String content;
    private String type;            // text, image,...
    @ServerTimestamp
    private Timestamp createdAt;
    private List<String> seenBy;    // danh sách userId đã đọc

    public Message() {}

    public Message(String messageId, String senderId, String content,
                   String type, Timestamp createdAt, List<String> seenBy) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
        this.seenBy = seenBy;
    }

    public Message(String senderId, String content, String type) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        // createdAt sẽ được set ngay trước khi gửi đi (thường là Server Timestamp)
        // messageId sẽ được set bởi Repository
        // seenBy sẽ được khởi tạo là một danh sách rỗng
        this.seenBy = new ArrayList<>();
    }

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public Timestamp getCreatedAt() { return createdAt; }
    public List<String> getSeenBy() { return seenBy; }

    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setContent(String content) { this.content = content; }
    public void setType(String type) { this.type = type; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setSeenBy(List<String> seenBy) { this.seenBy = seenBy; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("senderId", senderId);
        map.put("content", content);
        map.put("createdAt", null); // createdAt sẽ được set bởi Firestore
        map.put("seenBy", seenBy != null ? seenBy : new ArrayList<>());
        map.put("type", type);
        return map;
    }
}
