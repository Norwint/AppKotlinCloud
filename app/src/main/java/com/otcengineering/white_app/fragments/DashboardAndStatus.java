package com.otcengineering.white_app.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.interfaces.INotificable;
import com.otcengineering.white_app.service.ConnectDongleService;

public class DashboardAndStatus extends BaseFragment implements INotificable {
    private CustomTabLayout customTabLayout;
    private int selectedTab = 1;
    private boolean m_vehCondition = false;
    private Fragment currentFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard_and_status, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectDongleService.shouldUpdateFast = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ConnectDongleService.shouldUpdateFast = false;
    }

    private void retrieveViews(View v) {
        customTabLayout =  v.findViewById(R.id.dash_customTabLayout);
    }

    private void setEvents() {
        customTabLayout.configure(selectedTab, this::manageTabChanged, 1, 2);
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case 1:
                NewDashboardFragment ndf = new NewDashboardFragment();
                ndf.setVehicleCondition(m_vehCondition);
                m_vehCondition = false;
                changeFragment(ndf);
                break;
            case 2:
                changeFragment(new CarStatusFragment());
                break;
        }
    }

    private void changeFragment(Fragment fragment) {
        changeFragment(fragment, R.id.dash_layoutContainer);
        currentFragment = fragment;
    }

    public void setTab(int tab) {
        selectedTab = tab;
    }

    public void setVehCondition(boolean b) {
        this.m_vehCondition = b;
    }

    @Override
    public void onNotificationReceived() {
        if (currentFragment instanceof INotificable) {
            ((INotificable) currentFragment).onNotificationReceived();
        }
    }
}
