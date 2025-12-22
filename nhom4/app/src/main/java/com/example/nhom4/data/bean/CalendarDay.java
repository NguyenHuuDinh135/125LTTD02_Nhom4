package com.example.nhom4.data.bean;

import java.time.LocalDate;

public class CalendarDay {
    public LocalDate date;
    public boolean isCurrentMonth;
    public boolean hasPost;
    public String thumbnailUrl; // [MỚI] Lưu link ảnh bài viết
    public Post post; // [MỚI] Lưu đối tượng Post để xử lý click
    public CalendarDay(LocalDate date, boolean isCurrentMonth, boolean hasPost, String thumbnailUrl, Post post) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.hasPost = hasPost;
        this.thumbnailUrl = thumbnailUrl;
        this.post = post;
    }
}
