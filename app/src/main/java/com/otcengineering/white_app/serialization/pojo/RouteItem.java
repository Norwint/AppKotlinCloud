package com.otcengineering.white_app.serialization.pojo;

import android.os.Parcel;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cenci7
 */

public class RouteItem {

    private long id;
    private General.RouteType routeType;
    private long gpxFileId;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private double distanceInMeters;
    private int durationInMins;
    private double consumption;
    private double consumptionAvg;
    private double drivingTechnique;
    private String title;
    private String description;
    private String polyLine;
    private boolean favorite;
    private int likes;
    private int dislikes;
    private List<LatLng> latLngList = new ArrayList<>();
    private List<PoiWrapper> poiList = new ArrayList<>();

    public RouteItem() {
    }

    public RouteItem(String json) {
        Gson gson = new Gson();
        try {
            RouteItem item = gson.fromJson(json, RouteItem.class);
            if (item != null) {
                this.id = item.id;
                this.routeType = item.routeType;
                this.gpxFileId = item.gpxFileId;
                this.dateStart = item.dateStart;
                this.dateEnd = item.dateEnd;
                this.distanceInMeters = item.distanceInMeters;
                this.durationInMins = item.durationInMins;
                this.consumption = item.consumption;
                this.consumptionAvg = item.consumptionAvg;
                this.drivingTechnique = item.drivingTechnique;
                this.title = item.title;
                this.description = item.description;
                this.polyLine = item.polyLine;
                this.likes = item.likes;
                this.dislikes = item.dislikes;
                this.latLngList = item.latLngList;
                this.poiList.addAll(item.poiList);
            }
        } catch (JsonSyntaxException e) {
            //Log.e("RouteItem", "JsonSyntaxException", e);
        }
    }

    public RouteItem(RouteItem route) {
        this.id = route.id;
        this.routeType = route.routeType;
        this.gpxFileId = route.gpxFileId;
        this.dateStart = route.dateStart;
        this.dateEnd = route.dateEnd;
        this.distanceInMeters = route.distanceInMeters;
        this.durationInMins = route.durationInMins;
        this.consumption = route.consumption;
        this.consumptionAvg = route.consumptionAvg;
        this.drivingTechnique = route.drivingTechnique;
        this.title = route.title;
        this.description = route.description;
        this.favorite = route.favorite;
        this.polyLine = route.polyLine;
        this.likes = route.likes;
        this.dislikes = route.dislikes;

        for (PoiWrapper wrap : route.getPoiList()) {
            poiList.add(new PoiWrapper(wrap));
        }
    }

    public RouteItem(MyTrip.Route route) {
        this.id = route.getId();
        this.routeType = route.getType();
        this.gpxFileId = route.getGpxFileId();
        this.dateStart = DateUtils.stringToDateTime(route.getDateStart(), "yyyy-MM-dd HH:mm:ss");
        this.dateEnd = DateUtils.stringToDateTime(route.getDateEnd(), "yyyy-MM-dd HH:mm:ss");
        this.distanceInMeters = route.getDistance();
        this.durationInMins = route.getDuration();
        this.consumption = route.getConsumption() / 100.0D;
        this.consumptionAvg = route.getAvgConsumption();
        this.drivingTechnique = route.getDrivingTechnique();
        this.title = route.getTitle();
        this.description = route.getDescription();
        this.favorite = route.getFavourite();
        //this.polyLine = route.getPolyLine();
        this.likes = route.getLikes();
        this.dislikes = route.getDislikes();
    }

    public RouteItem(MyTrip.RouteFavourite route) {
        this.id = route.getRoute().getId();
        this.routeType = route.getRoute().getType();
        this.gpxFileId = route.getRoute().getGpxFileId();
        this.dateStart = DateUtils.stringToDateTime(route.getRoute().getDateStart(), "yyyy-MM-dd HH:mm:ss");
        this.dateEnd = DateUtils.stringToDateTime(route.getRoute().getDateEnd(), "yyyy-MM-dd HH:mm:ss");
        this.distanceInMeters = route.getRoute().getDistance();
        this.durationInMins = route.getRoute().getDuration();
        this.consumption = route.getRoute().getConsumption() / 100.0D;
        this.consumptionAvg = route.getRoute().getAvgConsumption();
        this.drivingTechnique = route.getRoute().getDrivingTechnique();
        this.title = route.getRoute().getTitle();
        this.description = route.getRoute().getDescription();
        this.favorite = route.getRoute().getFavourite();
        this.likes = route.getRoute().getLikes();
        this.dislikes = route.getRoute().getDislikes();
    }

    protected RouteItem(Parcel in) {
        id = in.readLong();
        routeType = (General.RouteType) in.readValue(General.RouteType.class.getClassLoader());
        gpxFileId = in.readLong();
        dateStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(in.readLong()), ZoneId.systemDefault());
        dateEnd = LocalDateTime.ofInstant(Instant.ofEpochMilli(in.readLong()), ZoneId.systemDefault());
        distanceInMeters = in.readDouble();
        durationInMins = in.readInt();
        consumption = in.readDouble();
        consumptionAvg = in.readDouble();
        drivingTechnique = in.readDouble();
        title = in.readString();
        description = in.readString();
        polyLine = in.readString();
        favorite = in.readByte() != 0;
        likes = in.readInt();
        dislikes = in.readInt();
        try {
            in.readList(latLngList, LatLng.class.getClassLoader());
            in.readList(poiList, General.POI.class.getClassLoader());
        } catch (Exception ex) {
            //Log.e("RouteItem", "Exception", ex);
        }
    }

    public RouteItem copy() {
        return new RouteItem(this.toString());
    }

    @Override
    public String toString() {
        try {
            Gson gson = new Gson();
            return gson.toJson(this);
        } catch (Exception e) {
            return "";
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public General.RouteType getRouteType() {
        return routeType;
    }

    public boolean isAutosave() {
        return routeType == General.RouteType.AUTOSAVED;
    }

    public boolean isPlanned() {
        return routeType == General.RouteType.PLANNED;
    }

    public void setRouteType(General.RouteType routeType) {
        this.routeType = routeType;
    }

    public long getGpxFileId() {
        return gpxFileId;
    }

    public void setGpxFileId(long gpxFileId) {
        this.gpxFileId = gpxFileId;
    }

    public boolean hasGpx() {
        return getGpxFileId() != 0;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
        calcDurationInMinutes();
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
        calcDurationInMinutes();
    }

    private void calcDurationInMinutes() {
        if (dateEnd != null && dateStart != null) {
            long minutes = ChronoUnit.MINUTES.between(dateStart, dateEnd);
            this.durationInMins = (int)minutes;
        }
    }

    public double getDistanceInMeters() {
        return distanceInMeters;
    }

    public double getDistanceInKms() {
        if ((distanceInMeters / 1000) > 100000){
            return 100000;
        }

        return distanceInMeters / 1000;
    }

    public void setDistanceInMeters(double distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public int getDurationInMins() {
        return durationInMins;
    }

    public String getDurationInMinsFormatted() {
        int hours = (durationInMins/60) ;
        String h = String.valueOf(hours);
        int mins = (durationInMins % 60);
        String m = String.valueOf(mins);
        if (mins < 10) {
            m = "0" + m;
        }
        return h + ":" + m + "h";
    }

    public void setDurationInMins(int durationInMins) {
        this.durationInMins = durationInMins;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public double getConsumptionAvg() {
        return consumptionAvg;
    }

    public void setConsumptionAvg(double consumptionAvg) {
        this.consumptionAvg = consumptionAvg;
    }

    public double getDrivingTechnique() {
        return drivingTechnique;
    }

    public void setDrivingTechnique(double drivingTechnique) {
        this.drivingTechnique = drivingTechnique;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPolyLine(String polyLine) {
        this.polyLine = polyLine;
    }

    public String getPolyLine() {
        return polyLine;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public List<LatLng> getLatLngList() {
        return latLngList;
    }

    public void setLatLngList(List<LatLng> latLngList) {
        this.latLngList = latLngList;
    }

    public List<PoiWrapper> getPoiList() {
        return poiList;
    }

    public List<General.POI> getRawPoiList() {
        ArrayList<General.POI> pois = new ArrayList<>();
        Stream.of(poiList).forEach(p -> pois.add(p.getPoi()));
        return pois;
    }

    public void setPoiList(List<PoiWrapper> poiList) {
        this.poiList = poiList;
    }
}
