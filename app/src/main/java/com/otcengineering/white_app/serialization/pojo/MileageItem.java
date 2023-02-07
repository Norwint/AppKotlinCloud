package com.otcengineering.white_app.serialization.pojo;


import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalDate;

import java.io.Serializable;
import java.util.List;

public class MileageItem implements Serializable {

    private LocalDate date;
    private double distance;
    private String duration;
    private int globalRanking;
    private int localRanking;
    private List<Double> fragments;

    public MileageItem(String dateString, double distance, String duration, int globalRanking, int localRanking, List<Double> fragments) {
        this.date = DateUtils.stringToDate(dateString, DateUtils.FMT_SRV_DATE);
        this.distance = distance;
        this.duration = duration;
        this.globalRanking = globalRanking;
        this.localRanking = localRanking;
        this.fragments = fragments;
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

    public double getDistance() {
        return distance;
    }

    public double getDistanceInKms() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
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

    public int getGlobalRanking() {
        return globalRanking;
    }

    public void setGlobalRanking(int globalRanking) {
        this.globalRanking = globalRanking;
    }

    public int getLocalRanking() {
        return localRanking;
    }

    public void setLocalRanking(int localRanking) {
        this.localRanking = localRanking;
    }

    public List<Double> getFragments() {
        return fragments;
    }

    public void setFragments(List<Double> fragments) {
        this.fragments = fragments;
    }
}

