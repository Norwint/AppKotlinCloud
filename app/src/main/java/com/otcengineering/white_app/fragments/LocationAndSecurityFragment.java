package com.otcengineering.white_app.fragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;


public class LocationAndSecurityFragment extends BaseFragment {
    private static final int TAB_LOCATION = 1;
    private static final int TAB_EMERGENCY = 2;

    private CustomTabLayout customTabLayout;

    protected static LatLng location;
    private Timer m_timer;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location_and_security, container, false);
        retrieveViews(v);
        setEvents();

        m_timer = new Timer("LocationAndSecurityTimer");
        m_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Task<Location> t = Utils.getLocation(getActivity());
                if (t != null) {
                    t.addOnSuccessListener((loc) -> {
                        if (loc != null) {
                            location = new LatLng(loc.getLatitude(), loc.getLongitude());
                        }
                    });
                }
            }
        }, 0, 2000);

        return v;
    }

    private void retrieveViews(View v) {
        customTabLayout =  v.findViewById(R.id.dash_customTabLayout);
    }

    private void setEvents() {
        customTabLayout.configure(TAB_LOCATION, this::manageTabChanged, TAB_LOCATION, TAB_EMERGENCY);
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case TAB_LOCATION:
                changeFragment(new LocationFragment());
                break;
            case TAB_EMERGENCY:
                changeFragment(new EmergencyFragment());
                break;
        }
    }

    private void changeFragment(Fragment fragment) {
        changeFragment(fragment, R.id.dash_layoutContainer);
    }

    @Override
    public void onDestroy() {
        m_timer.cancel();
        super.onDestroy();
    }
}
