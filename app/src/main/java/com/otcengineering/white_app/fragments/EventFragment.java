package com.otcengineering.white_app.fragments;

import android.os.Bundle;
import android.os.SystemClock;

import com.otcengineering.white_app.MyApp;

public class EventFragment extends BaseFragment {
    static long exitTime;
    static String exitTag;
    private String mActivityName;
    private long mTimeStart;

    public EventFragment(String name) {
        mActivityName = name;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimeStart = SystemClock.elapsedRealtime();

        if (exitTag != null && !exitTag.equals(mActivityName)) {
            long elapsedTime = mTimeStart - exitTime;

            Bundle bundle = new Bundle();
            bundle.putLong("elapsedTime", elapsedTime);
            bundle.putString("source", exitTag);
            bundle.putString("destination", mActivityName);
            MyApp.sendEvent("ScreenTransition", bundle);

            exitTag = null;
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

        exitTime = timeEnd;
        exitTag = mActivityName;
    }
}
