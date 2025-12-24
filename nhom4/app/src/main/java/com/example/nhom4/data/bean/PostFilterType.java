package com.example.nhom4.data.bean;

public enum PostFilterType {
    ALL("Mọi người"),        // Feed tổng hợp
    SPECIFIC_USER("Người dùng"), // Một người cụ thể (Bạn bè hoặc bất kỳ ai)
    SELF("Bản thân");        // Shortcut cho chính mình

    private final String label;

    PostFilterType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}