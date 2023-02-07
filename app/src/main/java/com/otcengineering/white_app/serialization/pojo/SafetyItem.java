package com.otcengineering.white_app.serialization.pojo;


import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalDate;

import java.io.Serializable;

public class SafetyItem implements Serializable {

    private LocalDate date;
    private double drivingTechinique;
    private String duration;

    public SafetyItem(String dateString, double drivingTechinique, String duration) {
        this.date = DateUtils.stringToDate(dateString, DateUtils.FMT_SRV_DATE);
        this.drivingTechinique = drivingTechinique;
        this.duration = duration;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDayFormatted() {
        return "" + date.getDayOfMonth();
    }

    public String getMonthFormatted() {
        return DateUtils.getMonthFormatted(date);
    }

    public String getDayAndMonthFormatted() {
        return DateUtils.getDayAndMonthFormatted(date);
    }

    public String getYearFormatted() {
        return "" + date.getYear();
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getDrivingTechinique() {
        return drivingTechinique;
    }

    public void setDrivingTechinique(double drivingTechinique) {
        this.drivingTechinique = drivingTechinique;
    }

    public String getDuration() {
        if (duration == null || duration.isEmpty()) {
            duration = "00:00";
        }
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}

