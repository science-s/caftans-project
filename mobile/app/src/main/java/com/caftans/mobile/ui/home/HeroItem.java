package com.caftans.mobile.ui.home;

public class HeroItem {
    private String title;
    private String description;
    private String imageUrl;
    private String actionButtonText;

    public HeroItem(String title, String description, String imageUrl, String actionButtonText) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.actionButtonText = actionButtonText;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getActionButtonText() {
        return actionButtonText;
    }
}
