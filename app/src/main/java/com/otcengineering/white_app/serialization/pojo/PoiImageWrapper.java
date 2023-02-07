package com.otcengineering.white_app.serialization.pojo;

import androidx.annotation.Keep;

@Keep
public class PoiImageWrapper {
    @Keep
    public enum ImageStatus {
        Added, Modified, Deleted, Undefined
    }
    private Long m_id;
    private ImageStatus m_status;
    private String m_image;

    public PoiImageWrapper() {
        m_status = ImageStatus.Undefined;
    }

    public PoiImageWrapper(Long id) {
        m_id = id;
        m_status = ImageStatus.Modified;
    }

    public PoiImageWrapper(PoiImageWrapper other) {
        m_id = other.m_id;
        m_status = other.m_status;
        m_image = other.m_image;
    }

    public Long getID() { return m_id; }
    public void setID(Long lng) { m_id = lng; }
    public ImageStatus getStatus() { return m_status; }
    public void setStatus(ImageStatus sts) { m_status = sts; }
    public String getImage() { return m_image; }
    public void setImage(String bs) { m_image = bs; }
}
