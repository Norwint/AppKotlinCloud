package com.otcengineering.white_app.fragments;

import android.os.Bundle;
import android.os.SystemClock;

import com.otcengineering.white_app.MyApp;

public class MapEventFragment extends MapBaseFragment {
    //static long exitTime;
    //static String exitTag;
    private String mActivityName;
    private long mTimeStart;

    public MapEventFragment(String name) {
        mActivityName = name;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimeStart = SystemClock.elapsedRealtime();

        if (EventFragment.exitTag != null && !EventFragment.exitTag.equals(mActivityName)) {
            long elapsedTime = mTimeStart - EventFragment.exitTime;

            Bundle bundle = new Bundle();
            bundle.putLong("elapsedTime", elapsedTime);
            bundle.putString("source", EventFragment.exitTag);
            bundle.putString("destination", mActivityName);
            MyApp.sendEvent("ScreenTransition", bundle);

            EventFragment.exitTag = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        long timeEnd = SystemClock.elapsedRealtime();

        Bundle bundle = new Bundle();
        bundle.putString("screenName", mActivityName);
        bundle.putLong("elapsedTime", (timeEnd - mTimeStart) / 1000);
        MyApp.sendEvent("ActivityTime", bundle);

        EventFragment.exitTime = timeEnd;
        EventFragment.exitTag = mActivityName;
    }
}
