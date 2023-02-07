package com.otcengineering.white_app.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.otcengineering.white_app.MyApp;

import java.util.Locale;
import java.util.TimeZone;

public class LanguageUtils {

    private static final String PREFERENCES_TIMEZONE = "USER_TIMEZONE";
    private static final String PREFERENCES_LANGUAGE = "USER_LANGUAGE";
    public static final String PREFERENCES_LANG_CHANGE = "LANGUAGE_CHANGED";

    public static void saveLanguage() {
        Context context = MyApp.getContext();
        MySharedPreferences msp = MySharedPreferences.createLanguage(context);
        Locale locale = Locale.getDefault();
        //msp.putString(PREFERENCES_LANGUAGE, locale.getLanguage());
        msp.putString(PREFERENCES_TIMEZONE, TimeZone.getDefault().getID());
        msp.putBoolean(PREFERENCES_LANG_CHANGE, true);
    }

    public static void loadLanguage() {
        Context context = MyApp.getContext();
        MySharedPreferences msp = MySharedPreferences.createLanguage(context);
        String language = msp.getString(PREFERENCES_LANGUAGE);
        if (language.isEmpty()) {
            language = Locale.getDefault().getLanguage();
        }
        Locale locale = Locale.forLanguageTag(language);
        setLocale(locale);
    }

    public static void setLocale(Locale locale) {
        MyApp.setUserLocale(locale);
        Locale.setDefault(locale);
        Resources resources = MyApp.getContext().getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static String getLanguage() {
        Context context = MyApp.getContext();
        MySharedPreferences msp = MySharedPreferences.createLanguage(context);
        return msp.getString(PREFERENCES_LANGUAGE);
    }
}
