package com.otcengineering.white_app.activities;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_PRIVILEGED;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;

import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.apible.blecontrol.service.UpdateCarService;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.interfaces.AsyncResponse;
import com.otcengineering.white_app.network.ConfigurationNetwork;
import com.otcengineering.white_app.network.ProfileNetwork;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.pushnotifications.MyFirebaseMessagingService;
import com.otcengineering.white_app.views.activity.HomeActivity;
import com.otcengineering.white_app.views.activity.WelcomeActivity;


public class MainActivity extends EventActivity {

    private final String[] PERMISSIONS = {
            WAKE_LOCK,
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            BLUETOOTH,
            BLUETOOTH_ADMIN,
            BLUETOOTH_PRIVILEGED,
            CAMERA,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE,
            READ_PHONE_STATE,
            CALL_PHONE,
            BIND_ACCESSIBILITY_SERVICE};

    int PERMISSION_ALL = 1;

    public MainActivity() {
        super("MainActivity");
    }
    MyFirebaseMessagingService.NotificationType type;
    String extraValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean log = MySharedPreferences.createLogin(getApplicationContext()).getBoolean("loggin 2.0");
        setContentView(R.layout.activity_main);

        if (getIntent() != null && getIntent().getExtras() != null) {
            long id = getIntent().getExtras().getLong("NotificationID", -1);
            if (id != -1) {
                ProfileNetwork.setNotificationRead(id);
            }
            String newString;

            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    newString = "";
                } else {
                    newString = extras.getString("FOTA");
                    type = MyFirebaseMessagingService.NotificationType.valueOf(extras.getString("NotificationType", "Normal"));
                    if (extras.containsKey("NotificationExtras")) {
                        extraValues = extras.getString("NotificationExtras");
                    }
                }
            } else {
                newString = (String) savedInstanceState.getSerializable("FOTA");
            }
            if (newString != null && newString.equals("update")) {
                if (Utils.callbackFOTA != null)
                    Utils.callbackFOTA.fotaUpdateAskYes();
            }

            if (log) {
                Intent i = new Intent(MainActivity.this, Home2Activity.class);
                PrefsManager.getInstance().getMyUserIDAsync(this, new AsyncResponse<Long>() {
                    @Override
                    public void onResponse(Long val) {
                    }

                    @Override
                    public void onFailure() {

                    }
                });
                if (type != null) {
                    i.putExtra("NotificationType", type.toString());
                }
                if (extraValues != null) {
                    i.putExtra("NotificationExtras", extraValues);
                }
                startActivity(i);
                finish();
            }
        }

        //startService(new Intent(MainActivity.this, SimpleMainService.class));
        try {
            // startService(new Intent(MainActivity.this, FileTransfer.class));
            //startService(new Intent(MainActivity.this, UpdateCarService.class));
            startService(new Intent(MainActivity.this, HeartBeatService.class));
        } catch (IllegalStateException ise) {
            // Si inicies l'app amb la pantalla apagada, peta aquí.
            // això no pot succeir en circumstancies reals ja que es necessari que l'usuari obri l'app
            // aixi que poso un try catch perque no faci spam el crashlytics
            ise.printStackTrace();
        }
        UpdateCarService.delay = 1000;

        // HeartBeatService.heartbeatEnabled = false;

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);

        /*if (Utils.usesXposed(this)) {
            CustomDialog customDialog = new CustomDialog(this, getString(R.string.detected_prog, "Xposed"), true);
            customDialog.show();
        }
        if (Utils.usesDrozer(this)) {
            CustomDialog customDialog = new CustomDialog(this, getString(R.string.detected_prog, "Drozer"), true);
            customDialog.show();
        }
        if (Utils.usesInspeckage(this)) {
            CustomDialog customDialog = new CustomDialog(this, getString(R.string.detected_prog, "Inspeckage"), true);
            customDialog.show();
        }
        if (Utils.usesFrida(this)) {
            CustomDialog customDialog = new CustomDialog(this, getString(R.string.detected_prog, "Frida"), true);
            customDialog.show();
        }
        if (Utils.isEmulator(this)) {
            CustomDialog customDialog = new CustomDialog(this, getString(R.string.emu_detected), true);
            customDialog.setOnDismissListener(v -> ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL));
            customDialog.show();
        } else if (Utils.isRooted(this)) {
            CustomDialog customDialog = new CustomDialog(this, getString(R.string.root_detect), true);
            customDialog.setOnDismissListener(v -> ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL));
            customDialog.show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
        }*/
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ALL) {
            //startActivity(new Intent(MainActivity.this, DongleActivity.class));
            if (!MySharedPreferences.createLogin(getApplicationContext()).contains("loggin 2.0")) {
                Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(MainActivity.this, HomeActivity.class);
                PrefsManager.getInstance().getMyUserIDAsync(this, new AsyncResponse<Long>() {

                    @Override
                    public void onResponse(Long val) {

                    }

                    @Override
                    public void onFailure() {

                    }
                });
                ConfigurationNetwork.fetchBluetoothConfiguration();
                if (type != null) {
                    i.putExtra("NotificationType", type.toString());
                }
                if (extraValues != null) {
                    i.putExtra("NotificationExtras", extraValues);
                }
                startActivity(i);
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
