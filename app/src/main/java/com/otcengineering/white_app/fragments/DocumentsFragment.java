package com.otcengineering.white_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomTabLayout;

public class DocumentsFragment extends BaseFragment {
    private static final int TAB_WALLET = 1;
    private static final int TAB_MANUAL = 2;

    private WalletFragment walletFragment;
    private ManualFragment manualFragment;

    private CustomTabLayout customTabLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_documents, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    private void retrieveViews(View v) {
        customTabLayout = v.findViewById(R.id.documents_customTabLayout);
    }

    private void setEvents() {
        customTabLayout.configure(this::manageTabChanged, TAB_WALLET, TAB_MANUAL);
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case TAB_WALLET:
                if (!isWalletVisible()) {
                    walletFragment = new WalletFragment();
                    changeFragment(walletFragment);
                }
                break;
            case TAB_MANUAL:
                if (!isManualVisible()) {
                    manualFragment = new ManualFragment();
                    changeFragment(manualFragment);
                }
                break;
        }
    }

    private boolean isWalletVisible() {
        return walletFragment != null && walletFragment.isVisible();
    }

    private boolean isManualVisible() {
        return manualFragment != null && manualFragment.isVisible();
    }

    private void changeFragment(Fragment fragment) {
        changeFragment(fragment, R.id.documents_layoutContainer);
    }

    public void doActionsAfterGallery(Intent data) {
        if (walletFragment != null && walletFragment.isVisible()) {
            walletFragment.doActionsAfterGallery(data);
        }
    }

    public void doActionsAfterCamera() {
        if (walletFragment != null && walletFragment.isVisible()) {
            walletFragment.doActionsAfterCamera();
        }
    }

}
