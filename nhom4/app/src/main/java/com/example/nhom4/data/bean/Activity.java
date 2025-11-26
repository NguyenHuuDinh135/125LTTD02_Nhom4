package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Activity {
    private String id;          // ID của activity trên Firestore
    private String creatorId;   // Người tạo
    private String title;       // Tên hoạt động
    private String description; // [QUAN TRỌNG] Mô tả hoạt động
    private String imageUrl;    // [QUAN TRỌNG] Link ảnh đại diện

    private int progress;       // Số bài đã post
    private int target;         // Mục tiêu (Mặc định 10)
    private boolean isRewardClaimed;

    private List<String> participants; // Danh sách người tham gia
    private Timestamp createdAt;

    // Constructor rỗng bắt buộc cho Firestore
    public Activity() { }

    // Constructor tạo mới cơ bản
    public Activity(String creatorId, String title) {
        this.creatorId = creatorId;
        this.title = title;
        this.progress = 0;
        this.target = 10; // Mặc định 10 bài
        this.isRewardClaimed = false;
        this.participants = new ArrayList<>();
        this.participants.add(creatorId); // Người tạo tự động tham gia
        this.createdAt = Timestamp.now();
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public boolean isRewardClaimed() { return isRewardClaimed; }
    public void setRewardClaimed(boolean rewardClaimed) { isRewardClaimed = rewardClaimed; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
