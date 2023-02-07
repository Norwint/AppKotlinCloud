package com.otcengineering.white_app.serialization;

import java.util.ArrayList;

public class BookingKeys {
    private long bookingId;
    private ArrayList<BookingSharedKey> bookingKeys;

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public ArrayList<BookingSharedKey> getBookingKeys() {
        return bookingKeys;
    }

    public void setBookingKeys(ArrayList<BookingSharedKey> bookingKeys) {
        this.bookingKeys = bookingKeys;
    }

    public BookingSharedKey getBookingKey(int idx) {
        return bookingKeys.get(idx);
    }

    public static class BookingSharedKey {
        private long id;
        private String code;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
