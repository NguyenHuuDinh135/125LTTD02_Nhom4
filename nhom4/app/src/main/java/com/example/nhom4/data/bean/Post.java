package com.example.nhom4.data.bean;

import com.google.firebase.Timestamp;

public class Post {
    private String postId;
    private String userId;
    private String caption;
    private String photoUrl;
    private String type; // "mood" hoặc "activity"
    private String moodName;
    private String moodIconUrl;

    // [CẬP NHẬT] Thêm activityId để tracking tiến độ
    private String activityId;
    private String activityTitle;

    private Timestamp createdAt;

    // Các trường hiển thị Runtime (không lưu vào collection 'posts' nếu dùng @Exclude, nhưng ở đây ta cứ để public)
    private String userName;
    private String userAvatar;

    public Post() { }

    // Getters
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getCaption() { return caption; }
    public String getPhotoUrl() { return photoUrl; }
    public String getType() { return type; }
    public String getMoodName() { return moodName; }
    public String getMoodIconUrl() { return moodIconUrl; }

    public String getActivityId() { return activityId; } // Getter mới
    public String getActivityTitle() { return activityTitle; }

    public Timestamp getCreatedAt() { return createdAt; }

    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }

    // Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCaption(String caption) { this.caption = caption; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setType(String type) { this.type = type; }
    public void setMoodName(String moodName) { this.moodName = moodName; }
    public void setMoodIconUrl(String moodIconUrl) { this.moodIconUrl = moodIconUrl; }

    public void setActivityId(String activityId) { this.activityId = activityId; } // Setter mới
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
}
