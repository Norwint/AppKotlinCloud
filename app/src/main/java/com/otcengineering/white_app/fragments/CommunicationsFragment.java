package com.otcengineering.white_app.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.interfaces.ShowMenuPostsListener;
import com.otcengineering.white_app.utils.PrefsManager;


public class CommunicationsFragment extends EventFragment {
    private static final int TAB_SEARCH = 1;
    private static final int TAB_POSTS = 2;
    private static final int TAB_CONNECTECH = 3;
    private static final int TAB_DEALER = 4;

    private int tabToChange = -1;
    private int subtab = -1;

    private CustomTabLayout customTabLayout;

    private ShowMenuPostsListener listener;
    public CommunicationsFragment() {
        super("CommunicationsActivity");
    }

    public void setListener(ShowMenuPostsListener listener) {
        this.listener = listener;
    }

    public void setTab(int tab) {
        tabToChange = tab;
    }

    public void setSubTab(int tab) {
        subtab = tab;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_communications, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    private void retrieveViews(View v) {
        try {
            customTabLayout = v.findViewById(R.id.communications_customTabLayout);
        } catch (Exception e) {
            //Log.e("CommunicationsFragment", "RuntimeException", e);
        }
    }

    private void setEvents() {
        customTabLayout.configure(this::manageTabChanged, TAB_SEARCH, TAB_POSTS, TAB_CONNECTECH, TAB_DEALER);
        if (tabToChange != -1) {
            customTabLayout.clickTab(tabToChange);
            tabToChange = -1;
        }
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case TAB_SEARCH:
                hideKeyboard();
                changeFragment(new SearchFragment());
                break;
            case TAB_POSTS:
                hideKeyboard();
                setNewContentToFalse();
                PostsFragment postsFragment = new PostsFragment();
                postsFragment.configure(null, listener);
                if (subtab != -1) {
                    postsFragment.setMode(subtab);
                    subtab = -1;
                }
                changeFragment(postsFragment);
                break;
            case TAB_CONNECTECH:
                hideKeyboard();
                DealerFragment oemFragment = new DealerFragment();
                oemFragment.oem = true;
                if (subtab != -1) {
                    oemFragment.setMode(subtab);
                    subtab = -1;
                }
                oemFragment.setListener(listener);
                changeFragment(oemFragment);
                break;
            case TAB_DEALER:
                hideKeyboard();
                DealerFragment dealerFragment = new DealerFragment();
                dealerFragment.oem = false;
                if (subtab != -1) {
                    dealerFragment.setMode(subtab);
                    subtab = -1;
                }
                dealerFragment.setListener(listener);
                changeFragment(dealerFragment);
                break;
        }
    }

    private void setNewContentToFalse() {
        PrefsManager.getInstance().setHasNewContent(false, getContext());

        customTabLayout.manageIndicatorUI();

        Intent intent = new Intent(Constants.Prefs.HAS_NEW_CONTENT);
        Activity act = getActivity();
        if (act != null) {
            LocalBroadcastManager.getInstance(act).sendBroadcast(intent);
        }
    }

    private void changeFragment(Fragment fragment) {
        changeFragment(fragment, R.id.communications_layoutContainer);
    }

    private void hideKeyboard(){
        Activity act = getActivity();
        if (act != null) {
            View view = act.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

}
