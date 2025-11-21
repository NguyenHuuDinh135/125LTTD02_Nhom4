package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;

public class Post {
    private String postId;
    private String userId;
    private String caption;
    private String photoUrl;
    private String type; // "mood" hoặc "activity"
    private String moodName;
    private String activityTitle;
    private Timestamp createdAt;
    private String moodIconUrl; // Thêm trường này

    // Bắt buộc phải có constructor rỗng cho Firestore
    public Post() { }

    // Getters
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getCaption() { return caption; }
    public String getPhotoUrl() { return photoUrl; }
    public String getType() { return type; }
    public String getMoodName() { return moodName; }
    public String getActivityTitle() { return activityTitle; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters (Firestore cần để map dữ liệu)
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCaption(String caption) { this.caption = caption; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setType(String type) { this.type = type; }
    public void setMoodName(String moodName) { this.moodName = moodName; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getMoodIconUrl() { return moodIconUrl; }
    public void setMoodIconUrl(String moodIconUrl) { this.moodIconUrl = moodIconUrl; }
}
