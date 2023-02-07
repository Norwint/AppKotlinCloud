package com.otcengineering.white_app.serialization.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.otc.alice.api.model.Community;

import java.util.List;

/**
 * Created by cenci7
 */

public class OemDealerItem implements Parcelable {

    private long id;
    private String date;
    private String text;
    private int likes;
    private List<Community.PostImage> images;

    public OemDealerItem(Community.ConnecTechPost connectechPost) {
        this.id = connectechPost.getId();
        this.date = connectechPost.getDate();
        this.text = connectechPost.getText();
        this.likes = connectechPost.getLikes();
        this.images = connectechPost.getImagesList();
    }

    public OemDealerItem(Community.DealerPost dealerPost) {
        this.id = dealerPost.getId();
        this.date = dealerPost.getDate();
        this.text = dealerPost.getText();
        this.likes = dealerPost.getLikes();
        this.images = dealerPost.getImagesList();
    }

    protected OemDealerItem(Parcel in) {
        id = in.readLong();
        date = in.readString();
        text = in.readString();
        likes = in.readInt();
        in.readList(images, ClassLoader.getSystemClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(date);
        dest.writeString(text);
        dest.writeInt(likes);
        dest.writeList(images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OemDealerItem> CREATOR = new Creator<OemDealerItem>() {
        @Override
        public OemDealerItem createFromParcel(Parcel in) {
            return new OemDealerItem(in);
        }

        @Override
        public OemDealerItem[] newArray(int size) {
            return new OemDealerItem[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<Community.PostImage> getImages() {
        return this.images;
    }

    public void setImages(List<Community.PostImage> images) {
        this.images = images;
    }
}
