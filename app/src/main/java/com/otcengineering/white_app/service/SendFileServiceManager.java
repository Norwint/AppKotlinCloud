package com.otcengineering.white_app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.MyApp;

import java.util.Calendar;

/**
 * Created by cenci7
 */

public class SendFileServiceManager {

    public static void initializeAlarm() {

        Context context =  OtcBle.getInstance().getContext();
        Calendar cal = Calendar.getInstance();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(context, SendFileService.class);
        // make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
        PendingIntent servicePendingIntent =
                PendingIntent.getService(
                        context,
                        SendFileService.SERVICE_ID, // integer constant used to identify the service
                        serviceIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);  // flag to avoid creating a second service if there's already one running

        if (am != null) {
            am.setRepeating(
                    AlarmManager.RTC_WAKEUP,//type of alarm. This one will wake up the device when it goes off, but there are others, check the docs
                    cal.getTimeInMillis(),
                    60000,
                    servicePendingIntent);
            //Log.d(SendFileService.TAG, "Alarm manager initialized");
        } else {
            //Log.d(SendFileService.TAG, "Alarm manager is null!!");
        }
    }

    public static void stopSyncAlarm() {
        Context context = MyApp.getContext();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(context, SendFileService.class);
        PendingIntent servicePendingIntent =
                PendingIntent.getService(context,
                        SendFileService.SERVICE_ID, // integer constant used to identify the service
                        serviceIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        if (am != null) {
            am.cancel(servicePendingIntent);
            //Log.d(SendFileService.TAG, "Alarm manager stopped");
        } else {
            //Log.d(SendFileService.TAG, "Alarm manager is null!!");
        }
    }

}
