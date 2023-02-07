package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.network.ProfileNetwork;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.pushnotifications.MyFirebaseMessagingService;

import static com.otcengineering.white_app.utils.pushnotifications.MyFirebaseMessagingService.NotificationType.Badges;

public class NotificationActivity extends EventActivity {
    public NotificationActivity() {
        super("Notification");
    }

    private Bundle m_extras;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean log = MySharedPreferences.createLogin(getApplicationContext()).getBoolean("loggin 2.0");
        // L'usuari no està loguejat
        if (!log) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        m_extras = getIntent().getExtras();
        long notificationID = m_extras.getLong("NotificationID", -1);
        if (notificationID != -1) {
            ProfileNetwork.setNotificationRead(notificationID);
        }

        MyFirebaseMessagingService.NotificationType type = MyFirebaseMessagingService.NotificationType.valueOf(m_extras.getString("NotificationType", "Normal"));

        Class cls = Home2Activity.class;
        if (type == MyFirebaseMessagingService.NotificationType.Route) {
            cls = DrivingActivity.class;
        } else if (type == MyFirebaseMessagingService.NotificationType.Fota) {
            Utils.sendFotaResponse(true);
        } else if (type == Badges) {
            cls = BadgesActivity.class;
        }

        Bundle notifBundle = new Bundle();
        if (type == MyFirebaseMessagingService.NotificationType.ConnecTech || type == MyFirebaseMessagingService.NotificationType.ConnecTechMsg
                || type == MyFirebaseMessagingService.NotificationType.Dealer || type == MyFirebaseMessagingService.NotificationType.DealerMsg) {
            notifBundle.putString("notificationType", "POST");
            notifBundle.putString("postId", m_extras.getString("id"));
        } else {
            notifBundle.putString("notificationType", "NOTIFICATION");
        }

        notifBundle.putString("userAction", "CLICKED");

        MyApp.sendEvent("NotificationAction", notifBundle);

        Intent intent = new Intent(this, cls);
        if (isTaskRoot()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtras(m_extras);
        startActivity(intent);
        if (!isTaskRoot()) {
            finish();
        }
    }
}
