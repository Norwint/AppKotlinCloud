package com.otcengineering.white_app.serialization.pojo;

import androidx.annotation.Keep;

import com.otc.alice.api.model.General;

import java.io.Serializable;
import java.util.ArrayList;

@Keep
public class PoiWrapper implements Serializable {
    private static long s_internalIdGenerator = 0;

    @Keep
    public enum PoiStatus {
        Added, Modified, Deleted, Undefined
    }
    private General.POI m_poi;
    private PoiStatus m_status;
    private ArrayList<PoiImageWrapper> m_images = new ArrayList<>();
    private long m_internalImageID;

    public PoiWrapper() {
        m_status = PoiStatus.Undefined;
        m_internalImageID = ++s_internalIdGenerator;
    }

    public PoiWrapper(PoiWrapper other) {
        m_poi = other.m_poi;
        m_status = other.m_status;
        m_internalImageID = other.m_internalImageID;

        for (PoiImageWrapper pow : other.getImages()) {
            m_images.add(new PoiImageWrapper(pow));
        }
    }

    public PoiWrapper(General.POI poi) {
        m_poi = poi;
        m_status = PoiStatus.Undefined;
        m_internalImageID = ++s_internalIdGenerator;
    }

    public General.POI getPoi() {
        return m_poi;
    }

    public void setPoi(General.POI value) {
        m_poi = value;
    }

    public PoiStatus getStatus() {
        return m_status;
    }

    public void setStatus(PoiStatus value) {
        m_status = value;
    }

    public ArrayList<PoiImageWrapper> getImages() { return m_images; }

    public void setImages(ArrayList<PoiImageWrapper> al) { m_images = al; }

    public long getInternalID() {
        return m_internalImageID;
    }
}

