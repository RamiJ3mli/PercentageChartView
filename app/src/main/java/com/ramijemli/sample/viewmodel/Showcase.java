package com.ramijemli.sample.viewmodel;

public class Showcase {
    private int color;
    private String title;
    private String subtitle;

    public Showcase(int color, String title, String subtitle) {
        this.color = color;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
