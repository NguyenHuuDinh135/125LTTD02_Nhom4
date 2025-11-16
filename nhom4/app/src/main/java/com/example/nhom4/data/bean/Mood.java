package com.example.nhom4.data.bean;

public class Mood {
    private String name;
    private int iconDrawableId;

    public Mood(String name, int iconDrawableId) {
        this.name = name;
        this.iconDrawableId = iconDrawableId;
    }

    public String getName() {
        return name;
    }

    public int getIconDrawableId() {
        return iconDrawableId;
    }
}
