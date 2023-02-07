package com.otcengineering.white_app.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {
    private static ProgressDialog pd;

    public static void create(final Context ctx) {
        pd = new ProgressDialog(ctx);
    }

    public static void show() {
        pd.show();
    }

    public static void setCancelable(boolean cancelable) {
        pd.setCancelable(cancelable);
    }

    public static void setTitle(final String title) {
        pd.setTitle(title);
    }

    public static void setMessage(final String mes) {
        pd.setMessage(mes);
    }

    public static void hide() {
        try {
            if (pd != null) pd.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
