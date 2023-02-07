package com.otcengineering.white_app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;

import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

@SuppressLint("Registered")
public class MapBaseActivity extends BaseActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;
    private static final int REQUEST_CODE_DIALOG_ACTIVATE_GPS = 200;

    private static final float ZOOM_DEFAULT = 12F;

    protected MapView mapView;

    protected GoogleMap googleMap;

    private LocationRequest locationRequest;
    private LocationCallback locationListener;
    private Location myLocation;

    private boolean isGpsEnable = false;

    public MapBaseActivity(final String name) {
        super(name);
        TAG = "MapBaseActivity";
    }

    protected void startGoogleMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        configureGoogleMapSettings();
        //setMapStyle();
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
        int accessFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            String[] accessFineLocationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, accessFineLocationPermissions, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }

    protected void drawRouteInfo(List<LatLng> latLngList, Double distanceInKm) {
        if (latLngList != null && latLngList.size() > 0) {
            LatLng start = latLngList.get(0);
            LatLng end;
            if (latLngList.size() > 1) {
                end = latLngList.get(latLngList.size() - 1);
            } else {
                end = start;
            }
            drawMarkerCircle(start);
            drawMarkerCircle(end);
            drawMarker(end, R.drawable.marker_pin);
            drawRoute(latLngList);
            centerMapInRoute(start, end, distanceInKm);
        }
    }

    protected void drawMarkerCircle(LatLng latLng) {
        Bitmap bitmap = createBitmap();
        if (bitmap != null) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5F, 0.5F)
                    .icon(icon));
        }
    }

    protected void drawMarker(LatLng latLng, int iconRes) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(iconRes);
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(icon));
        }
    }

    protected void drawMarker(LatLng latLng, int iconRes, String title) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(iconRes);
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions()
                    .title(title)
                    .position(latLng)
                    .icon(icon));
        }
    }

    private Bitmap createBitmap() {
        if (!Utils.isActivityFinish(this)) {
            int px = getResources().getDimensionPixelSize(R.dimen.marker_circle_size);
            Bitmap bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Drawable shape = getResources().getDrawable(R.drawable.marker_circle);
            shape.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            shape.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    private void drawRoute(List<LatLng> latLngList) {
        googleMap.addPolyline(new PolylineOptions()
                .addAll(latLngList)
                .width(7)
                .color(getResources().getColor(R.color.line_dotted)));
    }

    private void centerMapInRoute(LatLng start, LatLng end, Double distance) {
        LatLngBounds latLngBounds = new LatLngBounds.Builder().include(start).include(end).build();
        int zoom;
        if (distance < 20) {
            zoom = 12;
        } else if (distance < 65) {
            zoom = 10;
        } else if (distance < 200) {
            zoom = 8;
        } else if (distance < 400) {
            zoom = 7;
        } else if (distance < 600) {
            zoom = 6;
        } else if (distance < 800) {
            zoom = 5;
        } else {
            zoom = 4;
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), zoom);
        googleMap.animateCamera(cameraUpdate);
    }

    protected void centerMapInMyLocationWithAnimation() {
        centerMapInMyLocation();
    }

    protected void centerMapInMyLocation() {
        if (myLocation != null) {
            LatLng myPoint = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPoint, ZOOM_DEFAULT);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    public Location getMyLocation() {
        return myLocation;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeLocationRequest();
        requestLocationUpdates();
    }

    private void initializeLocationRequest() {
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
                PrefsManager.getInstance().saveLastLocation(myLocation, getApplicationContext());
                printLocationValue();
            }
        };
    }

    private void printLocationValue() {
        String location = String.format(
                Locale.getDefault(),
                "(%s, %s)",
                myLocation.getLongitude(),
                myLocation.getLatitude());
    }

    private void showDialogActivateGPSifNecessary() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                if (response.getLocationSettingsStates().isLocationUsable()) {
                    isGpsEnable = true;
                    requestLocationUpdates();
                }
            } catch (ApiException ex) {
                switch (ex.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(this, REQUEST_CODE_DIALOG_ACTIVATE_GPS);
                        } catch (IntentSender.SendIntentException | ClassCastException e) {

                        }
                    }
                    break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                        break;
                    }
                }
            }
        });
    }

    protected void requestLocationUpdates() {
        int accessFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            String[] accessFineLocationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, accessFineLocationPermissions, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            try {
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnCompleteListener(complete -> myLocation = complete.getResult());
                FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
                flpc.getLastLocation().addOnSuccessListener(location -> myLocation = location);
                flpc.requestLocationUpdates(locationRequest, locationListener, null);
            } catch (IllegalStateException ise) {
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CODE_LOCATION_PERMISSION:
                    requestLocationUpdates();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DIALOG_ACTIVATE_GPS) {
            if (resultCode == RESULT_OK) {
                isGpsEnable = true;
                requestLocationUpdates();
            } else {
                showDialogGPSisNecessary();
            }
        }
    }

    private void showDialogGPSisNecessary() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    showDialogActivateGPSifNecessary();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.gps_is_necessary)
                .setPositiveButton(R.string.activate, dialogClickListener)
                .setNegativeButton(R.string.cancel, dialogClickListener)
                .show();
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
        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(this);
        flpc.removeLocationUpdates(locationListener);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
