package com.example.nhom4.data.bean;

public class User {
    private String uid;
    private String username;
    private String profilePhotoUrl;

    public User() {} // Constructor rỗng cho Firestore

    public User(String uid, String username, String profilePhotoUrl) {
        this.uid = uid;
        this.username = username;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
}