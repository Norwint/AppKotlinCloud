package com.otcengineering.white_app.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.utils.PrefsManager;

/**
 * Created by cenci7
 */

public class MapBaseFragment extends BaseFragment implements OnMapReadyCallback {
    Polygon geofencingPolygon;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;

    private static final float ZOOM_DEFAULT = 15F;

    MapView mapView;

    GoogleMap googleMap;

    private LocationRequest locationRequest;
    private LocationCallback locationListener;
    private Location myLocation;

    private Runnable onComplete;

    void startGoogleMap(Bundle savedInstanceState) {
        try {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        } catch (Exception ignored) {

        }
    }

    void startGoogleMap(Bundle savedInstance, Runnable onComplete) {
        startGoogleMap(savedInstance);
        this.onComplete = onComplete;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        configureGoogleMapSettings();
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void configureGoogleMapSettings() {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        googleMap.setBuildingsEnabled(false);
        showMyLocationInMap();
    }

    private void showMyLocationInMap() {
        Activity act = getActivity();
        if (act == null) {
            act = MyApp.getCurrentActivity();
        }
        int accessFineLocationPermission = ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            String[] accessFineLocationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(act, accessFineLocationPermissions, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }

    void drawPolygon(LatLng p1, LatLng p2, LatLng p3, LatLng p4) {
        if (geofencingPolygon != null) {
            geofencingPolygon.remove();
        }
        geofencingPolygon = googleMap.addPolygon(new PolygonOptions().add(p1, p2, p3, p4, p1).fillColor(0x400000FF).strokeColor(0xFF0000FF));
    }

    Marker drawMarker(LatLng latLng, int iconRes) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(iconRes);
        return googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(icon));
    }

    Marker drawMarker(LatLng latLng, int iconRes, String title) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(iconRes);
        return googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(icon));
    }

    void centerMap(LatLng point, float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, zoom);
        googleMap.animateCamera(cameraUpdate);
    }

    void centerMapInMyLocation() {
        try{
            if (myLocation == null) {
                myLocation = PrefsManager.getInstance().getLastLocation(getContext());
            }
            if (myLocation.getLatitude() != 0 && myLocation.getLongitude() != 0) {
                try {

                    LatLng myPoint = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPoint, ZOOM_DEFAULT);
                    googleMap.moveCamera(cameraUpdate);
                } catch (Exception e) {
                    //Log.e(TAG, e.getLocalizedMessage());
                }
            }
        } catch (Exception e){
            //Log.e(TAG, e.getLocalizedMessage());
        }
    }

    Location getMyLocation() {
        return myLocation;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLocationRequest();
    }

    private void initializeLocationRequest() {
        try {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(60000); //10sec
            locationRequest.setFastestInterval(10000); //5sec
            locationRequest.setSmallestDisplacement(10); //10m
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            locationListener = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    myLocation = locationResult.getLastLocation();
                    PrefsManager.getInstance().saveLastLocation(locationResult.getLastLocation(), getContext());
                }
            };
            requestLocationUpdates();
        } catch (Exception e){
            //Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void requestLocationUpdates() {
        try  {
            Activity act = getActivity();
            if (act == null) {
                act = MyApp.getCurrentActivity();
            }
            int accessFineLocationPermission = ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION);

            if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                String[] accessFineLocationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(act, accessFineLocationPermissions, REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(act);
                flpc.getLastLocation().addOnSuccessListener(location -> myLocation = location);
                flpc.requestLocationUpdates(locationRequest, locationListener, null);
            }
        } catch (Exception e) {
            //Log.e(TAG, e.getLocalizedMessage());
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        mapView.onCancelPendingInputEvents();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        Context ctx = getContext();
        if (ctx == null) {
            ctx = MyApp.getContext();
        }
        LocationServices.getFusedLocationProviderClient(ctx).removeLocationUpdates(locationListener);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
