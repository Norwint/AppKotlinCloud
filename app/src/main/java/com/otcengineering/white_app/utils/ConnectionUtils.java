package com.otcengineering.white_app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;

/**
 * Created by cenci7
 */

public class ConnectionUtils {
    // When was disconnected last time
    private static long sm_millis;

    // Popup shown?
    private static boolean sm_shownPopup = false;

    public synchronized static boolean isOnline(Context ctx) {
        if (ctx == null) {
            ctx = MyApp.getContext();
        }
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean online = netInfo != null && netInfo.isConnected();
        if (!online && sm_millis == 0) {
            sm_millis = System.currentTimeMillis();
        } else if (online) {
            sm_shownPopup = false;
            sm_millis = 0;
        }
        return online;
    }

    public synchronized static void showOfflineToast() {
        if (sm_millis > 0 && !sm_shownPopup && System.currentTimeMillis() - sm_millis >= 3000) {
            sm_shownPopup = true;
            Context context = MyApp.getContext();
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.offline), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
