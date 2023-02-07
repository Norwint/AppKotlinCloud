package com.otcengineering.white_app.utils.pushnotifications;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.otc.alice.api.model.MyTrip;
import com.otc.alice.api.model.Shared;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.Home2Activity;
import com.otcengineering.white_app.activities.NotificationActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.interfaces.INotificable;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.ProfileNetwork;
import com.otcengineering.white_app.tasks.BadgesTask;
import com.otcengineering.white_app.tasks.SendPushTokenTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nonnull;

/**
 * Created by cenci7
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "FirebaseMsgService";

    public enum NotificationType {
        Normal, Geofencing, Dealer, ConnecTech, DealerMsg, ConnecTechMsg, Route, Fota, FriendPost, FriendInvitation, Badges, Status, VehicleCondition
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        MySharedPreferences.createDefault(getApplicationContext()).putString("token", s);
        Utils.runOnMainThread(() -> new SendPushTokenTask().execute(this));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (!MySharedPreferences.createLogin(getApplicationContext()).contains("token")) {
            return;
        }
        System.out.println("Received notification!");
        Map<String, String> data = remoteMessage.getData();
        String messagePush = data.get("message");
        try {
            JSONObject obj = new JSONObject(messagePush);

            String message;
            long id = -1;
            if (obj.has("id")) {
                id = obj.getLong("id");
            }

            String s = obj.getString("message");
            if (s.equals("NEW_FRIEND_REQUEST")) {
                message = getResources().getString(R.string.NEW_FRIEND_REQUEST);
                showNotification(null, message, id, NotificationType.FriendInvitation);
            } else if (s.equals("NEW_FRIEND_POST")) {
                message = getResources().getString(R.string.NEW_FRIEND_POST);
                showNotification(null, message, id, NotificationType.FriendPost);
            } else if (s.startsWith("NEW_CONNECTECH_POST")) {
                message = getResources().getString(R.string.NEW_CONNECTECH_POST);
                HashMap<String, String> extras = new HashMap<>();
                extras.put("id", s.replace("NEW_CONNECTECH_POST_", ""));
                showNotification(null, message, id, NotificationType.ConnecTech, extras);
            } else if (s.startsWith("NEW_CONNECTECH_MESSAGE")) {
                message = getResources().getString(R.string.NEW_CONNECTECH_MESSAGE);
                HashMap<String, String> extras = new HashMap<>();
                extras.put("id", s.replace("NEW_CONNECTECH_MESSAGE_", ""));
                showNotification(null, message, id, NotificationType.ConnecTechMsg, extras);
            } else if (s.startsWith("NEW_DEALER_POST")) {
                message = getResources().getString(R.string.NEW_DEALER_POST);
                HashMap<String, String> extras = new HashMap<>();
                extras.put("id", s.replace("NEW_DEALER_POST_", ""));
                showNotification(null, message, id, NotificationType.Dealer, extras);
            } else if (s.startsWith("NEW_DEALER_MESSAGE")) {
                message = getResources().getString(R.string.NEW_DEALER_MESSAGE);
                HashMap<String, String> extras = new HashMap<>();
                extras.put("id", s.replace("NEW_DEALER_MESSAGE_", ""));
                showNotification(null, message, id, NotificationType.DealerMsg, extras);
            } else if (s.equals("FRIEND_SHARING_LOCATION")) {
                message = getResources().getString(R.string.FRIEND_SHARING_LOCATION);
                showNotification(null, message, id);
            } else if (s.startsWith("FW_UPDATED")) {
                if (Utils.callbackFOTA != null)
                    Utils.callbackFOTA.fotaUpdated();
            } else if (s.startsWith("FW_ERROR")) {
                if (Utils.callbackFOTA != null)
                    Utils.callbackFOTA.fotaError(getString(R.string.dongle_updated_nok));
            } else if (s.equals("FW_DELETING_IMAGE")) {
                if (Utils.callbackFOTA != null)
                    Utils.callbackFOTA.fotaDeletingImage();
            } else if (s.equals("FW_SENDING_NEW_FW")) {
                if (Utils.callbackFOTA != null)
                    Utils.callbackFOTA.fotaSendingNewFw();
            } else if (s.startsWith("FW_RESTART_DONGLE")) {
                if (Utils.callbackFOTA != null)
                    Utils.callbackFOTA.fotaRestartDongle();
            } else if (s.equals("USER_EXPIRED_EXTEND_TIME")) {
                Utils.runOnMainThread(() -> {
                    DialogMultiple dm = new DialogMultiple(MyApp.getContext());
                    dm.setDescription(getString(R.string.extend_it));
                    dm.addButton("Ok", () -> {});
                    dm.show();
                });
            } else if ("GEOFENCING".equals(s)) {
                message = getString(R.string.your_vehicle_outside_notif);
                showNotification(null, message, id);
            } else if ("USER_UNLINK_MOBILE".equals(s)) {
                MySharedPreferences.createLogin(MyApp.getContext()).putBoolean("ShowPopupNewMobile", true);
                Utils.changedPhone(MyApp.getContext());
            } else if (s.regionMatches(0, "FW_UPDATE_ASK_", 0, "FW_UPDATE_ASK_".length())) {
                String version = obj.get("message").toString().substring("FW_UPDATE_ASK_".length());
                message = String.format(getString(R.string.update_dongle_question), version);
                if (Utils.callbackFOTA != null && !((MyApp) getApplication()).onBackground()) {
                    Utils.callbackFOTA.fotaUpdateAsk(version);
                } else {
                    showNotification(null, message, id, NotificationType.Fota);
                }
            } else if (s.contains("GEOFENCING_")) {
                String date = obj.getString("message").substring("GEOFENCING_".length());
                String parsedDate = parseDate(date);
                message = String.format(getString(R.string.your_vehicle_is_outside_geofencing), parsedDate);
                showNotification(null, message, id, NotificationType.Geofencing);
            } else if (s.contains("VEHICLE_STATUS")) {
                try {
                    if (!OtcBle.getInstance().isConnected()) {
                        String msg = obj.getString("message");
                        String[] split = msg.split("_");
                        // 0-> VEHICLE, 1-> STATUS
                        String whats = split[2];
                        String what;
                        if (whats.equals("ENGINE")) {
                            what = getString(R.string.engine);
                        } else if (whats.equals("DOOR")) {
                            what = getString(R.string.door);
                        } else {
                            what = whats.substring(0, 1).toUpperCase() + whats.substring(1).toLowerCase();
                        }
                        // String what = split[2].substring(0, 1).toUpperCase() + split[2].substring(1).toLowerCase();
                        StringBuilder action = new StringBuilder();
                        for (int i = 3; i < split.length - 1; ++i) {
                            action.append(split[i].toLowerCase()).append(" ");
                        }

                        String date = split[split.length - 1];
                        String parsedDate = parseDate(date);
                        showNotification(null, String.format(getString(R.string.x_x_at_x), what, action.toString().substring(0, action.length() - 1), parsedDate), id, NotificationType.Status);
                    }
                } catch (RuntimeException e) {
                    String msg = obj.getString("message");
                    String[] split = msg.split("_");
                    // 0-> VEHICLE, 1-> STATUS
                    String what = split[2].substring(0, 1).toUpperCase() + split[2].substring(1).toLowerCase();
                    StringBuilder action = new StringBuilder();
                    for (int i = 3; i < split.length - 1; ++i) {
                        action.append(split[i].toLowerCase()).append(" ");
                    }

                    String date = split[split.length - 1];
                    String parsedDate = parseDate(date);
                    showNotification(null, what + " " + action.toString().substring(0, action.length() - 1) + " at " + parsedDate, id, NotificationType.Status);
                }
            } else if (s.regionMatches(0, "BADGE_", 0, "BADGE".length())) {
                message = String.format(getString(R.string.won_badge), obj.get("message").toString().replace("_", " ").replace("BADGE ", ""));
                showNotification(null, message, id, NotificationType.Badges);

                try {
                    Utils.runOnMainThread(() -> new BadgesTask.getUserBadges().execute());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (s.startsWith("VEHICLE_IN_SERVICE_TIMING")) {
                showNotification(getString(R.string.periodic_maintenance_now),
                        getString(R.string.its_time_for_maintenance), id);
            } else if (s.startsWith("VEHICLE_CONDITION")) {
                String msg = obj.getString("message");
                String[] split = msg.split("_");

                String date = split[split.length - 1];
                String parsedDate = parseDate(date);
                String condition = msg.replace("VEHICLE_CONDITION_", "").replace("_" + date, "");

                String desc = getString(R.string.the_indicator_is_on, Utils.translateVehicleCondition(condition), parsedDate);
                showNotification("Vehicle alarm", desc, id, NotificationType.VehicleCondition);
            } else if (s.startsWith("USER_UNVALIDATED_EMAIL_ADDRESS")) {
                //USER_UNVALIDATED_EMAIL_ADDRESS_Unactivated email address: patata@mailinator.com
                String email = obj.getString("message").replace("USER_UNVALIDATED_EMAIL_ADDRESS_Unactivated email address: ", "");
                String submsg = getString(R.string.unactivated_email);
                showNotification(getString(R.string.user_email_not_validated), String.format(submsg, email), id);
            } else if (s.startsWith("USER_EXPIRED_IN_X_DAYS")) {
                String msg = obj.getString("message");
                String days = msg.replace("USER_EXPIRED_IN_X_DAYS_", "");
                Utils.runOnMainThread(() -> {
                    CustomDialog cd = new CustomDialog(MyApp.getContext());
                    if (days.equals("0")) {
                        MySharedPreferences.createLogin(MyApp.getContext()).putBoolean("HasToShowPopup", true);
                        cd.setMessage(getString(R.string.months_no_connect) + "\n" + getString(R.string.days_0));
                        cd.setOnOkListener(() -> Utils.logout(getApplicationContext()));
                    } else {
                        cd.setMessage(getString(R.string.months_no_connect) + "\n" + String.format(getString(R.string.days_n), days));
                    }
                    cd.show();
                });
                if (days.equals("0")) {
                    showNotification(getString(R.string.months_no_connect), getString(R.string.days_0), id);
                } else {
                    showNotification(getString(R.string.months_no_connect), String.format(getString(R.string.days_n), days), id);
                }
            } else if (s.startsWith("ROUTE")) {
                String identifierOfTheRoute = obj.getString("message").replace("ROUTE_", "");
                MyTrip.RouteId routeId = MyTrip.RouteId.newBuilder().setRouteId(Long.parseLong(identifierOfTheRoute)).build();
                long finalId = id;
                TypedTask<MyTrip.Route> gt = new TypedTask<>(Endpoints.GET_ROUTE, routeId, true, MyTrip.Route.class, new TypedCallback<MyTrip.Route>() {
                    @Override
                    public void onSuccess(@Nonnull @NonNull MyTrip.Route value) {
                        String txt = Utils.getRouteTextNotification(value);
                        HashMap<String, String> extras = new HashMap<>();
                        extras.put("Text", Utils.getRouteText(value));
                        MySharedPreferences.createDefault(getApplicationContext()).putString(String.format(Locale.US, "route_desc_%d", value.getId()), txt);
                        txt = Utils.translateRouteTextNotification(txt);
                        showNotification(getString(R.string.thank_you_for_driving), txt, finalId, NotificationType.Route, extras);
                    }

                    @Override
                    public void onError(@NonNull Shared.OTCStatus status, String str) {

                    }
                });
                gt.execute();
            } else if (s.equals("USER_ALL_SURVEYS_DONE")) {
                showNotification(getString(R.string.user_all_surveys_done_title), getString(R.string.user_all_surveys_done_desc), id);
            } else {
                showNotification("", obj.get("message").toString(), id);
            }
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
            showNotification("", data.toString(), 0);
        }

        ProfileNetwork.getNotificationCount(this);
        try {
            updateNotificationCenter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationCenter() {
        Activity currentActivity = MyApp.getCurrentActivity();
        if (currentActivity instanceof Home2Activity) {
            Home2Activity home = (Home2Activity) currentActivity;

            for (Fragment currentFragment : home.getSupportFragmentManager().getFragments()) {
                /*if (currentFragment instanceof NotificationFragment) {
                    NotificationFragment notificationFragment = (NotificationFragment) currentFragment;
                    if (!notificationFragment.updating) {
                        notificationFragment.page = 1;
                        Utils.runOnMainThread(() -> {
                            notificationFragment.dummyList.clear();
                            notificationFragment.getNotifications();
                        });
                    }
                }*/
                if (currentFragment instanceof INotificable) {
                    ((INotificable) currentFragment).onNotificationReceived();
                    break;
                }
            }
        } else if (currentActivity instanceof INotificable) {
            ((INotificable) currentActivity).onNotificationReceived();
        }
    }

    private static String parseDate(final String inputDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdf.parse(inputDate);
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return sdf2.format(date);
    }


    private void showNotification(String title, String message, long id) {
        showNotification(title, message, id, NotificationType.Normal);
    }

    private void showNotification(String title, String message, long id, NotificationType type) {
        showNotification(title, message, id, type, null);
    }

    private void showNotification(String title, String message, long id, NotificationType type, HashMap<String, String> extras) {
        int notificationId = calculateNotificationId();

        Bundle bundle = new Bundle();
        if (type == NotificationType.ConnecTech || type == NotificationType.ConnecTechMsg || type == NotificationType.Dealer || type == NotificationType.DealerMsg) {
            bundle.putString("notificationType", "POST");
            bundle.putString("postId", extras.get("id"));
        } else {
            bundle.putString("notificationType", "NOTIFICATION");
        }

        bundle.putString("userAction", "RECEIVED");

        MyApp.sendEvent("NotificationAction", bundle);

        Class clazz = NotificationActivity.class;
        Intent intent = new Intent(this, clazz);
        intent.putExtra("NotificationID", id);
        intent.putExtra("NotificationType", type.toString());
        if (extras != null) {
            for (Map.Entry<String, String> s : extras.entrySet()) {
                intent.putExtra(s.getKey(), s.getValue());
            }
        }
        SecureRandom sr = new SecureRandom();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, sr.nextInt(), intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (title == null) {
            title = getString(R.string.app_name);
        }

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle(title);
        bigText.bigText(message);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "connectech")
                .setSmallIcon(R.drawable.icon)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(bigText)
                .setContentTitle(title);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // In Android 8.0 and superior, the notifications needs to create a NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("connectech", "connectech", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.app_name));
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    /**
     * If ID is always the same, notifications will overwriting each other.
     * We set ID as a counter, to avoid repeating any number.
     *
     * @return id
     */
    private int calculateNotificationId() {
        MySharedPreferences msp = MySharedPreferences.createDefault(getApplicationContext());
        if (!msp.contains("PREFERENCES_PUSH_ID")) {
            msp.putInteger("PREFERENCES_PUSH_ID", 2);
        }
        int notifId = msp.getInteger("PREFERENCES_PUSH_ID");
        msp.putInteger("PREFERENCES_PUSH_ID", notifId + 1);
        return notifId;
    }
}
