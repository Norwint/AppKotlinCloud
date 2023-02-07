package com.otcengineering.white_app.serialization;

import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalTime;

public class BookingInfo {
    private long id;
    private long vehicleId;
    private String startDate;
    private String endDate;
    private String date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStartDateInMilitarFormat() {
        LocalTime ldt = DateUtils.stringToDateTime(startDate, "yyyy-MM-dd HH:mm:ss").toLocalTime();
        return ldt.getHour() * 100 + ldt.getMinute();
    }

    public int getEndDateInMilitarFormat() {
        LocalTime ldt = DateUtils.stringToDateTime(endDate, "yyyy-MM-dd HH:mm:ss").toLocalTime();
        return ldt.getHour() * 100 + ldt.getMinute();
    }
}
