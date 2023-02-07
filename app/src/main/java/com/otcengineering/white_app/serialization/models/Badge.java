package com.otcengineering.white_app.serialization.models;

public class Badge {

    private String name, objective, image, date, subtitle;
    private int state, type, size;

    public Badge(String name, String objective, String subtitle, String image, String date, int state, int type) {
        this.name = name;
        this.objective = objective;
        this.subtitle = subtitle;
        this.image = image;
        this.date = date;
        this.state = state;
        this.type = type;
    }

    public Badge(String name, String objective, String subtitle, String image, String date, int state, int type, int size) {
        this.name = name;
        this.objective = objective;
        this.subtitle = subtitle;
        this.image = image;
        this.date = date;
        this.state = state;
        this.type = type;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
