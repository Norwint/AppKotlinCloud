package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.Task;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.LocationAndSecurity;
import com.otc.alice.api.model.Shared;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MyProgressDialog;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import javax.annotation.Nonnull;


public class LocationFragment extends MapEventFragment {
    private LatLng carLocation;

    private FrameLayout centerMap;

    private SeekBar geofencingArea;

    private Geofencing geoFencing;
    private TextView alarm;
    private Marker carMarker;

    public LocationFragment() {
        super("LocationActivity");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location, container, false);
        retrieveViews(v);
        setEvents();

        Button btnLocation = v.findViewById(R.id.findMyCar);

        Utils.runOnBackground(() -> {
            if (OtcBle.getInstance().isConnected()) {
                getBlePosition();
            } else {
                TypedTask<General.Point> tt = new TypedTask<>(Endpoints.CAR, null, true, General.Point.class, new TypedCallback<General.Point>() {
                    @Override
                    public void onSuccess(@Nonnull @NonNull General.Point value) {
                        carLocation = new LatLng(value.getLatitude(), value.getLongitude());
                    }

                    @Override
                    public void onError(@NonNull Shared.OTCStatus status, String str) {

                    }
                });
                tt.execute();
            }
        });

        alarm = v.findViewById(R.id.tvAlarm);
        geofencingArea = v.findViewById(R.id.geofencingArea);
        geofencingArea.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                geoFencing.updatePoints();
                drawGeofencing();
            }
        });
        geoFencing = new Geofencing();

        btnLocation.setOnClickListener(view -> {
            try {
                MyProgressDialog.create(getContext());
                MyProgressDialog.setMessage(getString(R.string.fetching_car_loc));
                MyProgressDialog.show();
            } catch (WindowManager.BadTokenException bte) {
                bte.printStackTrace();
            }
            getCarLocation();
        });

        v.findViewById(R.id.centerMap).setOnClickListener(btn -> centerMapInMyLocation());

        startGoogleMap(savedInstanceState, () -> {
            googleMap.setOnMapClickListener(latLng -> {
                if (geoFencing.radius > 0) {
                    geoFencing.center = new GeoPoint(latLng);
                    this.progressChanged(geofencingArea);
                    geoFencing.updatePoints();
                    drawGeofencing();
                }
            });
            googleMap.setOnMapLongClickListener(latLng -> {
                if (geoFencing.radius > 0) {
                    geoFencing.center = new GeoPoint(latLng);
                    this.progressChanged(geofencingArea);
                    geoFencing.updatePoints();
                    drawGeofencing();
                }
            });
        });

        /*geoFencing.loadValuesFromCache();
        geofencingArea.setProgress((int)geoFencing.radius);*/

        return v;
    }

    @UiThread
    private void drawGeofencing() {
        if (geoFencing.isPrepared()) {
            drawPolygon(geoFencing.p1.toLatLng(), geoFencing.p2.toLatLng(), geoFencing.p4.toLatLng(), geoFencing.p3.toLatLng());
            float l2 = (float) (Math.log10(geofencingArea.getProgress()) / Math.log10(2));
            centerMap(geoFencing.center.toLatLng(), 15.0f - l2);
        }
    }

    private void getCarLocation() {
        GenericTask gt = new GenericTask(Endpoints.CAR, null, true, response -> {
            MyProgressDialog.hide();
            if (response.getStatus() == Shared.OTCStatus.SUCCESS) {
                General.Point pt = response.getData().unpack(General.Point.class);
                carLocation = new LatLng(pt.getLatitude(), pt.getLongitude());
                drawFindMyCar();
            } else {
                try {
                    showCustomDialogError(CloudErrorHandler.handleError(response.getStatus()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (OtcBle.getInstance().isConnected()) {
            getBlePosition();
            if (carLocation != null) {
                MyProgressDialog.hide();
                drawFindMyCar();
            } else {
                if (ConnectionUtils.isOnline(getContext())) {
                    gt.execute();
                } else {
                    ConnectionUtils.showOfflineToast();
                }
            }
        } else {
            if (ConnectionUtils.isOnline(getContext())) {
                gt.execute();
            } else {
                MyProgressDialog.hide();
                ConnectionUtils.showOfflineToast();
            }
        }
    }

    private void progressChanged(final SeekBar seekBar) {
        float progress = seekBar.getProgress();
        if (progress == 0) {
            geoFencing.radius = -1.0;
        } else {
            geoFencing.radius = progress;
        }
        if (geoFencing.radius != -1.0) {
            alarm.setText(getString(R.string.geo_alarm_n, (int) geoFencing.radius));
        } else {
            alarm.setText(getString(R.string.geo_alarm_off));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        geoFencing.loadValuesFromCache();
        geofencingArea.setProgress((int) geoFencing.radius);
        progressChanged(geofencingArea);
        if (geoFencing.isPrepared()) {
            drawPolygon(geoFencing.p1.toLatLng(), geoFencing.p2.toLatLng(), geoFencing.p4.toLatLng(), geoFencing.p3.toLatLng());
            if (MySharedPreferences.createLocationSecurity(getContext()).contains("HasToCheckCar")) {
                MySharedPreferences.createLocationSecurity(getContext()).remove("HasToCheckCar");
                getCarLocation();
            } else {
                float l2 = (float) (Math.log10(geoFencing.radius) / Math.log10(2));
                centerMap(geoFencing.center.toLatLng(), 15.0f - l2);
            }
        } else {
            Utils.runOnMainThread(() -> {
                if (LocationAndSecurityFragment.location == null) {
                    //showEnableLocationDialog(this.getContext());
                    Task<Location> tsk = Utils.getLocation(this.getActivity());
                    if (tsk != null) {
                        tsk.addOnSuccessListener(location -> {
                            if (location != null) {
                                LocationAndSecurityFragment.location = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationAndSecurityFragment.location, 15));
                            }
                        });
                    }
                } else {
                    if (MySharedPreferences.createLocationSecurity(getContext()).contains("HasToCheckCar")) {
                        MySharedPreferences.createLocationSecurity(getContext()).remove("HasToCheckCar");
                        getCarLocation();
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationAndSecurityFragment.location, 15));
                    }
                }
            });
        }
    }

    private void setEvents() {
        centerMap.setOnClickListener(v -> centerMapInMyLocation());
    }

    private void retrieveViews(View v) {
        mapView = v.findViewById(R.id.location_map);
        centerMap = v.findViewById(R.id.centerMap);
    }

    private void getBlePosition() {
        byte[] latB, lonB;
        try {
            latB = OtcBle.getInstance().carStatus.getByteArrayVar("Latitude");
            lonB = OtcBle.getInstance().carStatus.getByteArrayVar("Longitude");
        } catch (Exception e) {
            e.printStackTrace();
            latB = null;
            lonB = null;
        }
        float lat, lon;
        if (latB == null || latB.length < 4) {
            lat = 0.0f;
        } else {
            lat = com.otcengineering.apible.Utils.calculateGps(latB);
        }

        if (lonB == null || lonB.length < 4) {
            lon = 0.0f;
        } else {
            lon = com.otcengineering.apible.Utils.calculateGps(lonB);
        }

        if (lat != 0.0f && lon != 0.0f) {
            carLocation = new LatLng(lat, lon);
        }
    }

    private void drawFindMyCar() {
        if (carLocation == null) {
            final Context context = getContext();
            if (context == null) {
                return;
            }
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context);

            builder.setTitle(getResources().getString(R.string.location_error))
                    .setMessage(getResources().getString(R.string.unable_locate_car))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // continue with delete
                    })
                    .show();
        } else {
            if (carMarker != null) {
                carMarker.remove();
            }
            carMarker = drawMarker(carLocation, R.drawable.caricon);
            float zoom = 15;
            if (geoFencing.isPrepared()) {
                float l2 = (float) (Math.log10(geoFencing.radius) / Math.log10(2));
                zoom -= l2;
            }
            centerMap(carLocation, zoom);
        }
    }

    //thread ble
    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    public class GeoPoint {
        double x, y;

        GeoPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        GeoPoint(LatLng latLng) {
            this.x = latLng.longitude;
            this.y = latLng.latitude;
        }

        LatLng toLatLng() {
            return new LatLng(y, x);
        }
    }

    public static class BoundingBox {
        public double minLat, minLon, maxLat, maxLon;
    }

    private static BoundingBox getBoundingBox(final double pLatitude, final double pLongitude, final double pDistanceInMeters) {
        final BoundingBox boundingBox = new BoundingBox();

        final double latRadian = Math.toRadians(pLatitude);

        final double degLatKm = 110.574235;
        final double degLongKm = 110.572833 * Math.cos(latRadian);
        final double deltaLat = pDistanceInMeters / 1000.0 / degLatKm;
        final double deltaLong = pDistanceInMeters / 1000.0 / degLongKm;

        final double minLat = pLatitude - deltaLat;
        final double minLong = pLongitude - deltaLong;
        final double maxLat = pLatitude + deltaLat;
        final double maxLong = pLongitude + deltaLong;

        boundingBox.minLat = minLat;
        boundingBox.minLon = minLong;
        boundingBox.maxLat = maxLat;
        boundingBox.maxLon = maxLong;

        return boundingBox;
    }

    public class Geofencing {
        public GeoPoint center;
        GeoPoint p1, p2, p3, p4;
        public double radius;

        private void send(LocationAndSecurity.Geofencing geofencing) {
            GenericTask gt = new GenericTask(Endpoints.GEOFENCING, geofencing, true, (otcResponse) -> {
                if (otcResponse != null) {
                    try {
                        LocationAndSecurity.CarPosition carPos = LocationAndSecurity.CarPosition.parseFrom(otcResponse.getData().getValue());
                        if (carPos != null) {
                            System.out.println(carPos.getInsideOutside());
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            });
            gt.execute();
        }

        private void updatePointsInServer() {
            LocationAndSecurity.Geofencing geo = LocationAndSecurity.Geofencing.newBuilder()
                    .setPoint1Latitude(p1.y).setPoint1Longitude(p1.x)
                    .setPoint2Latitude(p2.y).setPoint2Longitude(p2.x)
                    .setPoint3Latitude(p4.y).setPoint3Longitude(p4.x)
                    .setPoint4Latitude(p3.y).setPoint4Longitude(p3.x)
                    .build();
            send(geo);
        }

        private void removeGeofencingInServer() {
            LocationAndSecurity.Geofencing geo = LocationAndSecurity.Geofencing.newBuilder()
                    .setPoint1Latitude(0).setPoint1Longitude(0)
                    .setPoint2Latitude(0).setPoint2Longitude(0)
                    .setPoint3Latitude(0).setPoint3Longitude(0)
                    .setPoint4Latitude(0).setPoint4Longitude(0)
                    .build();
            send(geo);
        }

        public void getPointsFromServer() {
            TypedTask<LocationAndSecurity.Geofencing> getPoints = new TypedTask<>(Endpoints.GEOFENCING, null, true, LocationAndSecurity.Geofencing.class,
                    new TypedCallback<LocationAndSecurity.Geofencing>() {
                        @Override
                        public void onSuccess(@Nonnull LocationAndSecurity.Geofencing value) {
                            if (Utils.allZeroes(value.getPoint1Latitude(), value.getPoint1Longitude(), value.getPoint2Latitude(), value.getPoint2Longitude(),
                                    value.getPoint3Latitude(), value.getPoint3Longitude(), value.getPoint4Latitude(), value.getPoint4Longitude())) {
                                if (carLocation != null) {
                                    center = new GeoPoint(carLocation.longitude, carLocation.latitude);
                                    radius = -1;
                                }
                            } else {
                                double distance = Utils.distance(value.getPoint1Longitude(), value.getPoint1Latitude(), value.getPoint2Longitude(), value.getPoint2Latitude());
                                radius = Math.rint(distance);

                                p1 = new GeoPoint(value.getPoint1Longitude(), value.getPoint1Latitude());
                                p2 = new GeoPoint(value.getPoint2Longitude(), value.getPoint2Latitude());
                                p3 = new GeoPoint(value.getPoint3Longitude(), value.getPoint3Latitude());
                                p4 = new GeoPoint(value.getPoint4Longitude(), value.getPoint4Latitude());

                                double lonCenter = (p2.x + p1.x) / 2;
                                double latCenter = (p2.y + p3.y) / 2;

                                center = new GeoPoint(lonCenter, latCenter);

                                geofencingArea.setProgress((int) radius);
                            }
                            updatePoints();
                            drawGeofencing();
                        }

                        @Override
                        public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                            // Capturar, Doh!
                            Log.e("LocationFragment", "Error fetching geofencing: " + status.name());
                        }
                    });
            getPoints.execute();
        }

        void updatePoints() {
            MySharedPreferences msp = MySharedPreferences.createLocationSecurity(getContext());
            if (radius < 0) {
                msp.remove("geofencingRadius");
                p1 = p2 = p3 = p4 = null;
                if (geofencingPolygon != null) {
                    geofencingPolygon.remove();
                }
                removeGeofencingInServer();
                return;
            }
            if (center == null) {
                if (carLocation != null) {
                    center = new GeoPoint(carLocation.longitude, carLocation.latitude);
                } else {
                    return;
                }
            }
            msp.putDouble("geofencingRadius", radius);
            msp.putDouble("geofencingCLat", center.x);
            msp.putDouble("geofencingClon", center.y);

            final double halfRadii = radius / 2;
            /*p1 = calcPoint(center.x, center.y, -halfRadii, halfRadii);
            p2 = calcPoint(center.x, center.y, halfRadii, halfRadii);
            p3 = calcPoint(center.x, center.y, -halfRadii, -halfRadii);
            p4 = calcPoint(center.x, center.y, halfRadii, -halfRadii);*/
            BoundingBox bb = getBoundingBox(center.y, center.x, 1000 * halfRadii);
            p1 = new GeoPoint(bb.minLon, bb.maxLat);
            p2 = new GeoPoint(bb.maxLon, bb.maxLat);
            p3 = new GeoPoint(bb.minLon, bb.minLat);
            p4 = new GeoPoint(bb.maxLon, bb.minLat);
            updatePointsInServer();
        }

        void loadValuesFromCache() {
            Utils.runOnBackground(() -> {
                MySharedPreferences msp = MySharedPreferences.createLocationSecurity(getContext());
                if (msp.contains("geofencingCLat") && msp.contains("geofencingClon")) {
                    center = new GeoPoint(0, 0);
                    center.x = msp.getDouble("geofencingCLat");
                    center.y = msp.getDouble("geofencingClon");
                    if (msp.contains("geofencingRadius")) {
                        radius = msp.getDouble("geofencingRadius");
                        Utils.runOnMainThread(() -> {
                            alarm.setText(getString(R.string.geo_alarm_n, (int) radius));
                            geofencingArea.setProgress((int) radius);
                            Utils.delay(500, LocationFragment.this::drawGeofencing);
                        });
                        updatePoints();
                    }
                } else {
                    getPointsFromServer();
                }
            });
        }

        private GeoPoint calcPoint(double lon, double lat, double latDist, double lonDist) {
            final double rLat = latDist / 110.57;
            final double rlon = lonDist / (111.32 * Math.cos(Math.PI * (lat + rLat) / 180));
            return new GeoPoint(lon + rlon, lat + rLat);
        }

        private boolean isPrepared() {
            return p1 != null && p2 != null && p3 != null && p4 != null && center != null;
        }
    }
}
