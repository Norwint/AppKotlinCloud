package com.otcengineering.white_app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.Route;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.TranslucentTransition;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import static com.otcengineering.white_app.utils.Utils.showEnableLocationDialog;


public class EmergencyFragment extends MapEventFragment {
    private FrameLayout btnLocation;
    private Button addressButton;
    private TextView address;
    private ConstraintLayout addressLayout;
    private TranslucentTransition translucentTransition;
    private Polyline pl;

    public EmergencyFragment() {
        super("RoadsideAssistanceActivity");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_emergency, container, false);

        retrieveViews(v);
        setEvents();

        startGoogleMap(savedInstanceState);

        getDealers();

        return v;
    }

    private void getDealers() {
        Welcome.Dealerships dsp = Welcome.Dealerships.newBuilder().setCountryId(3).build();
        TypedTask<Welcome.DealershipsResponse> task = new TypedTask<>(Endpoints.DEALERSHIPS, dsp, false, Welcome.DealershipsResponse.class, new TypedCallback<Welcome.DealershipsResponse>() {
            @Override
            public void onSuccess(@Nonnull Welcome.DealershipsResponse value) {
                for (Welcome.DealershipsResponse.Dealership dea : value.getDealershipsList()) {
                    if (dea.getLatitude() != 0 && dea.getLongitude() != 0) {
                        drawMarker(new LatLng(dea.getLatitude(), dea.getLongitude()), R.drawable.icon, dea.getName())
                                .showInfoWindow();
                    }
                }
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {

            }
        });
        task.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (LocationAndSecurityFragment.location == null) {
                showEnableLocationDialog(this.getContext());
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationAndSecurityFragment.location, 15));
            }
        });
        googleMap.setOnMarkerClickListener(marker -> {
            LatLng pos = marker.getPosition();
            Runnable run = () -> {
                try {
                    Utils.showGoogleMapsRoute(getContext(), pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            DialogYesNo dyn = new DialogYesNo(getContext());
            dyn.setTitle(marker.getTitle());
            dyn.setMessage("Do you want to show a route to the dealer in Google Maps?");
            dyn.setYesButtonClickListener(run);
            dyn.show();
            return true;
        });
    }


    private void retrieveViews(final View v){
        mapView =  v.findViewById(R.id.emergency_map);

        btnLocation = v.findViewById(R.id.emergency_btnLocation);
        addressButton = v.findViewById(R.id.addressButton);
        address = v.findViewById(R.id.address);
        addressLayout = v.findViewById(R.id.addressLayout);
        translucentTransition = new TranslucentTransition(addressLayout, 0.6f, 0.6f, 8f);
    }

    private void setEvents(){
        btnLocation.setOnClickListener(view -> centerMapInMyLocation());
        addressButton.setOnClickListener(v -> getAddress());
    }

    private void getAddress() {
        if (getMyLocation() != null) {
            Utils.getAddress(getMyLocation().getLatitude(), getMyLocation().getLongitude(), getContext(), s -> {
                addressLayout.setVisibility(View.VISIBLE);
                address.setText(s);
                translucentTransition.start();
            });
        } else {
            Toast.makeText(getContext(), getString(R.string.cannot_get_location), Toast.LENGTH_LONG).show();
        }
        /*
        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

        String[] items = new String[] {
                "1- GMaps", "1- MapBox", "2- GMaps", "2- MapBox", "3- GMaps", "3- MapBox"
        };

        ad.setItems(items, (dialog, which) -> {
            String url = "http://192.168.1.190:8080/v1";
            new NetTask(url, "/routes/polylines/" + (int) Math.ceil((which + 1) / 2.0), null, false, new NetworkCallback<NetTask.JsonResponse>() {
                @Override
                public void onSuccess(NetTask.JsonResponse response) throws Exception {
                    HashMap<String, String> map = response.getResponse(new TypeToken<HashMap<String, String>>() {}.getType());

                    Route mapbox = Utils.getGson().fromJson(map.get("polyline_mapbox"), Route.class);
                    Route gmaps = Utils.getGson().fromJson(map.get("polyline_gmaps"), Route.class);

                    if (pl != null) {
                        pl.remove();
                    }

                    if (items[which].contains("GMaps")) {
                        pl = googleMap.addPolyline(new PolylineOptions().addAll(gmaps.getLatLngPoints()).color(0xFF00FF00));
                    } else {
                        pl = googleMap.addPolyline(new PolylineOptions().addAll(mapbox.getLatLngPoints()).color(0xFFFF0000));
                    }

                    double lat = (gmaps.getPoints().get(0).getLatitude() + gmaps.getPoints().get(gmaps.getPoints().size() - 1).getLatitude()) / 2;
                    double lon = (gmaps.getPoints().get(0).getLongitude() + gmaps.getPoints().get(gmaps.getPoints().size() - 1).getLongitude()) / 2;

                    LatLng ll = new LatLng(lat, lon);
                    CameraUpdate cu = CameraUpdateFactory.newLatLng(ll);
                    googleMap.animateCamera(cu);
                }

                @Override
                public void onFailure(int code, String errorMsg) throws Exception {

                }
            }).execute();
        });
        ad.show();
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getMyLocation()!=null){
            centerMapInMyLocation();
        }
    }
}
