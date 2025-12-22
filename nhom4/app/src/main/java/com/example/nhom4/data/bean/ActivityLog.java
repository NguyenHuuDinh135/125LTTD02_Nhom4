package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;

public class ActivityLog {
    private String id;
    private String activityId;
    private String userId;
    private String proofImageUrl; // Ảnh chụp minh chứng
    private String note;
    private Timestamp completedAt;

    public ActivityLog() { }

    public ActivityLog(String activityId, String userId, String proofImageUrl, String note, Timestamp completedAt) {
        this.activityId = activityId;
        this.userId = userId;
        this.proofImageUrl = proofImageUrl;
        this.note = note;
        this.completedAt = Timestamp.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProofImageUrl() { return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) { this.proofImageUrl = proofImageUrl; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}