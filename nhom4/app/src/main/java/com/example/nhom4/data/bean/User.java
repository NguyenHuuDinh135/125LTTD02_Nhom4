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
    public String setUid(String uid) { return this.uid = uid; }
    public String setUsername(String username) { return this.username = username; }
    public String setProfilePhotoUrl(String profilePhotoUrl) { return this.profilePhotoUrl = profilePhotoUrl;
}
}