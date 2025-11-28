package com.example.nhom4.data.bean;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

public class UserProfile {
    private String uid;             // ID user
    private String username;        // Tên hiển thị
    private String email;           // Email
    private String birthday;        // Ngày sinh, có thể lưu dạng String "dd/MM/yyyy"
    private String profilePhotoUrl; // Ảnh đại diện
    private String phoneNumber;     // Số điện thoại (tuỳ chọn)
    @ServerTimestamp
    private Timestamp createdAt;    // Thời gian tạo profile

    public UserProfile() {} // Constructor rỗng cho Firestore

    public UserProfile(String uid, String username, String email, String birthday,
                       String profilePhotoUrl, String phoneNumber) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.birthday = birthday;
        this.profilePhotoUrl = profilePhotoUrl;
        this.phoneNumber = phoneNumber;
    }

    // --- getters & setters ---
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}