package com.example.nhom4.data.bean;

public class Mood {
    private String name;
    private String iconUrl;

    private boolean isPremium;

    public Mood(String name, String iconUrl, boolean isPremium) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.isPremium = isPremium;
    }
    public String getName() {
        return name;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public boolean isPremium() {
        return isPremium;
    }
    public void setPremium(boolean premium) {
        isPremium = premium;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
