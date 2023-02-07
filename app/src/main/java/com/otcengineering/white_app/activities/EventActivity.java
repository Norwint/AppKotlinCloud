package com.otcengineering.white_app.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.otcengineering.white_app.MyApp;

import java.util.Locale;

import static com.otcengineering.white_app.activities.BaseActivity.retainCount;

public class EventActivity extends AppCompatActivity {
    private final String mActivityName;
    private long mTimeStart;
    protected static String TAG = "EventActivity";

    static long exitTime;
    static String exitTag;

    public EventActivity(final String name) {
        mActivityName = name;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        retainCount--;
        long timeEnd = SystemClock.elapsedRealtime();

        Bundle bundle = new Bundle();
        bundle.putString("screenName", mActivityName);
        bundle.putLong("elapsedTime", (timeEnd - mTimeStart) / 1000);
        MyApp.sendEvent("ActivityTime", bundle);

        exitTime = timeEnd;
        exitTag = mActivityName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        retainCount++;
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(wrap(newBase, MyApp.getUserLocale().toLanguageTag()));
    }

    private static ContextWrapper wrap(Context context, String language) {
        Resources res = context.getResources();
        android.content.res.Configuration configuration = res.getConfiguration();
        Locale newLocale = Locale.forLanguageTag(language);
        Locale.setDefault(newLocale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale);
            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            context = context.createConfigurationContext(configuration);

        } else {
            configuration.setLocale(newLocale);
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            context = context.createConfigurationContext(configuration);
        }

        return new ContextWrapper(context);
    }
}
