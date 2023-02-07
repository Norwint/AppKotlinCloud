package com.otcengineering.white_app.serialization;

import android.util.Base64;

public class VehicleBooking {
    private long vehicleId;
    private String bookingDate;
    private String bookingStatus;

    public byte[] getBookingStatus() {
        return Base64.decode(bookingStatus, Base64.DEFAULT);
    }

    public void setBookingStatus(byte[] bookingStatus) {
        this.bookingStatus = Base64.encodeToString(bookingStatus, Base64.DEFAULT);
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }
}
