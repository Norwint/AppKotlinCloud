package com.otcengineering.white_app.serialization;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Route {
    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public ArrayList<LatLng> getLatLngPoints() {
        ArrayList<LatLng> arrayList = new ArrayList<>();
        for (int i = 0; i < points.size(); ++i) {
            LatLng ll = new LatLng(points.get(i).latitude, points.get(i).longitude);
            arrayList.add(ll);
        }
        return arrayList;
    }

    public static class Point {
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    private ArrayList<Point> points;
}
