package com.otcengineering.white_app.network;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.MySharedPreferences;

public class ProfileNetwork {
    public static void getNotificationCount(@NonNull Context ctx) {
        GenericTask getNotificationCount = new GenericTask(Endpoints.NOTIFICATION_COUNT, null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                ProfileAndSettings.NotificationCount ct = otcResponse.getData().unpack(ProfileAndSettings.NotificationCount.class);
                MySharedPreferences.createLogin(ctx).putLong("NotificationCount", ct.getToRead());
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent(Constants.Prefs.HAS_NEW_CONTENT));
            }
        });
        getNotificationCount.execute();
    }

    public static void setNotificationRead(long notificationId) {
        ProfileAndSettings.IdUserNotification idu = ProfileAndSettings.IdUserNotification.newBuilder().setId(notificationId).build();
        GenericTask setNotificationRead = new GenericTask(Endpoints.READ_NOTIFICATION, idu, true, otcResponse -> {
        });
        setNotificationRead.execute();
    }

    public static void deleteNotification(long notificationId) {
        ProfileAndSettings.IdUserNotification dun = ProfileAndSettings.IdUserNotification.newBuilder().setId(notificationId).build();
        GenericTask gt = new GenericTask(Endpoints.NOTIFICATIONS_DELETE, dun, true, (resp) -> {});
        gt.execute();
    }
}
