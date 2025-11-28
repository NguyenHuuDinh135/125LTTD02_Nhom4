package com.example.nhom4.data.bean;

import java.time.LocalDate;

public class CalendarDay {
    public LocalDate date;
    public boolean isCurrentMonth;
    public boolean hasPost;
    public String thumbnailUrl; // [MỚI] Lưu link ảnh bài viết

    public CalendarDay(LocalDate date, boolean isCurrentMonth, boolean hasPost, String thumbnailUrl) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.hasPost = hasPost;
        this.thumbnailUrl = thumbnailUrl;
    }
}
