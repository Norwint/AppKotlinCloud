package com.otcengineering.white_app.utils;

import androidx.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cenci7
 */

public class MapUtils {
    @Keep
    @Root(name = "gpx", strict = false)
    public static class Gpx {
        @Element(name = "trk")
        private Track track;

        Track getTrack() {
            return track;
        }

        public void setTrack(Track track) {
            this.track = track;
        }
    }

    @Keep
    public static class Track {
        @Element(name = "name")
        private String name;

        @ElementList(name = "trkpt", inline = true)
        private List<TrackPoint> trackPointList;

        public List<LatLng> getLatLngList() {
            List<LatLng> points = new ArrayList<>();
            for (TrackPoint trackPoint : trackPointList) {
                points.add(trackPoint.getLatLng());
            }
            return points;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTrackPointList(List<TrackPoint> trackPointList) {
            this.trackPointList = trackPointList;
        }
    }

    @Keep
    @Root(name = "trkpt", strict = false)
    public static class TrackPoint {
        @Attribute
        private double lon;

        @Attribute
        private double lat;

        public LatLng getLatLng() {
            return new LatLng(lat, lon);
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }
    }

    public static List<LatLng> getGpxInfo(byte[] gpxBytes) {
        try {
            String gpxXml = IOUtils.toString(gpxBytes, "UTF_8");
            Serializer serializer = new Persister();
            Gpx gpxInfo = serializer.read(Gpx.class, gpxXml);
            return gpxInfo.getTrack().getLatLngList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
