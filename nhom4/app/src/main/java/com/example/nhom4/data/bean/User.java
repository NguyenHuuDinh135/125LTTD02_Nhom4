package com.example.nhom4.data.bean;

public class User {
    private final String email;
    private final String password;
    private final String fullname;
    private final String address;
    public User(String email, String password, String fullname, String address) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.address = address;
    }
}
