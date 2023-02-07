package com.otcengineering.white_app.utils;

import android.util.Log;

import com.otcengineering.white_app.BuildConfig;

import javax.annotation.Nonnull;

public class Logger {
    public static void v(@Nonnull String title, @Nonnull String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(title, msg);
        }
    }

    public static void d(@Nonnull String title, @Nonnull String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(title, msg);
        }
    }

    public static void i(@Nonnull String title, @Nonnull String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(title, msg);
        }
    }

    public static void w(@Nonnull String title, @Nonnull String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(title, msg);
        }
    }

    public static void e(@Nonnull String title, @Nonnull String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(title, msg);
        }
    }
}
