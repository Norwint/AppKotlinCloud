package com.otcengineering.white_app.serialization.pojo;

import com.otc.alice.api.model.General;

/**
 * Created by cenci7
 */

public class UserInRankingItem {

    private int id;
    private int position; // position in the ranking, two users could have the same position
    private int number; // position in the list, two users NEVER could have the same position
    private String name;
    private String image;
    private General.ProfileType profileType;
    private boolean isFriend;

    public UserInRankingItem(int id, int position, int number, String name, String image, General.ProfileType profileType, boolean isFriend) {
        this.id = id;
        this.position = position;
        this.number = number;
        this.name = name;
        this.image = image;
        this.profileType = profileType;
        this.isFriend = isFriend;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public General.ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(General.ProfileType profileType) {
        this.profileType = profileType;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
