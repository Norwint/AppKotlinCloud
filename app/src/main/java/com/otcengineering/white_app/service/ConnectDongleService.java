package com.otcengineering.white_app.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.otc.alice.api.model.General;
import com.otcengineering.apible.CarStatus;
import com.otcengineering.apible.Constants;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.NetworkManager;
import com.otcengineering.white_app.utils.Utils;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectDongleService extends IntentService {
    public ConnectDongleService() {
        super("ConnectDongleService");
    }
    private static final int TIME = 1000 * 60 * 60 * 12;
    public static boolean firstTime = false;
    public static boolean shouldUpdateFast = false;
    public static boolean shouldRun = true;
    private long mTimeoutConn;

    private int m_iterations = 0;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, createNotification());
        }
        shouldRun = true;
        AtomicBoolean shouldStopTryingToConnect = new AtomicBoolean(false);
        while (shouldRun) {
            // Check every 5s
            if (m_iterations % 20 == 0) {
                if (MySharedPreferences.createLogin(getApplicationContext()).contains("token")) {
                    long lastLogin = MySharedPreferences.createLogin(MyApp.getContext()).getLong("token_date");
                    if (lastLogin + TIME < Calendar.getInstance().getTime().getTime()) {
                        new Thread(() -> NetworkManager.login(MyApp.getContext(), null, null), "LoginThread").start();
                    }

                    if (OtcBle.getInstance().getBleLibrary() == null) {
                        OtcBle.getInstance().setContext(getApplicationContext());
                        OtcBle.getInstance().createBleLibrary();
                    }
                    String macBLE = MySharedPreferences.createLogin(getApplicationContext()).getString("macBLE");
                    if (Utils.isValidMAC(macBLE) && OtcBle.getInstance().getBleLibrary().checkBluetoothAdapter()) {
                        OtcBle.getInstance().setDeviceMac(macBLE);

                        if (!OtcBle.getInstance().isConnected()) {
                            if (!OtcBle.getInstance().isConnecting()) {
                                if (mTimeoutConn <= System.currentTimeMillis()) {
                                    if (OtcBle.getInstance().getBleLibrary().getStatus() == 2 && OtcBle.getInstance().getBleLibrary().getConnGatt() != null) {
                                        OtcBle.getInstance().getBleLibrary().getConnGatt().disconnect();
                                    }
                                    OtcBle.getInstance().disconnect();
                                    OtcBle.getInstance().status = false;
                                    if (OtcBle.getInstance().getBleLibrary().isScanning()) {
                                        OtcBle.getInstance().stopScan();
                                    }
                                    Utils.runOnMainThread(() -> OtcBle.getInstance().connect());
                                    //Utils.runOnMainThread(() -> OtcBle.getInstance().connectByScan());
                                    mTimeoutConn = System.currentTimeMillis() + 5000;
                                }
                            }
                        } else if (OtcBle.getInstance().isConnected() || OtcBle.getInstance().isConnecting()) {
                            if (OtcBle.getInstance().getBleLibrary().isScanning()) {
                                OtcBle.getInstance().stopScan();
                            }
                        }
                        BaseActivity.wasConnected = OtcBle.getInstance().isConnected();
                    }
                }
            }
            if (OtcBle.getInstance().isConnected() && HeartBeatService.isRunning) {
                mTimeoutConn = 0;
                if (shouldUpdateFast || m_iterations % 4 == 0) {
                    //OtcBle.getInstance().getBleLibrary().readCharacteristic(9);

                    Utils.runOnBackThread(() -> OtcBle.getInstance().updateCarData());

                    firstTime = true;
                }
                if (m_iterations % 8 == 0) {
                    OtcBle.getInstance().getBleLibrary().readCharacteristic(10);
                }
                OtcBle.getInstance().getBleLibrary().readCharacteristic(11);

                if (m_iterations % 2 == 0) {
                    OtcBle.getInstance().getBleLibrary().readCharacteristic(9);
                }
                // Desa en cache lo del BLE cada 1s (abans cada 6.25s)
                if (m_iterations % 4 == 0) {
                    CarStatus cs = OtcBle.getInstance().carStatus;
                    if (cs != null && cs.contains(Constants.KL15)) {
                        try {
                            boolean vc = cs.getBitVar(Constants.ENGINE_ALARM) || cs.getBitVar(Constants.EPS_ALARM) || cs.getBitVar(Constants.ASC_ALARM) ||
                                    cs.getBitVar(Constants.BRAKE_SYSTEM_ALARM) || cs.getBitVar(Constants.ABS_ALARM) || cs.getBitVar(Constants.IMMOBILIZER_ALARM) ||
                                    cs.getBitVar(Constants.KOS_ALARM) || cs.getBitVar(Constants.SRS_ALARM) || cs.getBitVar(Constants.AT_ALARM) ||
                                    cs.getBitVar(Constants.OIL_ALARM) || cs.getBitVar(Constants.CHARGER_ALARM) ||  cs.getBitVar(Constants.BRAKE_FLUID_ALARM) ||
                                    cs.getBitVar(Constants.ELECTRIC_ALARM) || cs.getBitVar(Constants.STEERING_ALARM);
                            General.VehicleStatus vs = General.VehicleStatus.newBuilder()
                                    .setDate(DateUtils.getUtcString("yyyy-MM-dd HH:mm:ss"))
                                    .setFuelLevel(cs.getByteVar(Constants.FUEL)).setBit5(cs.getBitVar(Constants.DOORS)).setBit2(cs.getBitVar(Constants.HIGH_BEAM))
                                    .setBit3(cs.getBitVar(Constants.LOW_BEAM)).setBit4(cs.getBitVar(Constants.POSITION_LIGHTS)).setBit7(cs.getBitVar(Constants.HAZARDS))
                                    .setBit0(cs.getBitVar("EngineOnoffNotif")).setOdometer(cs.getIntVar(Constants.ODOMETER)).setBit8(vc).setBit1(cs.getBitVar(Constants.LOW_FUEL))
                                    .setBit9(cs.getBitVar(Constants.DONGLE)).build();
                            Gson gson = new Gson();
                            final String json = gson.toJson(vs);
                            MySharedPreferences.createLogin(getApplicationContext()).putString("StatusCache", json);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            ++m_iterations;
            com.otcengineering.apible.Utils.wait(this, 250);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel("ConnecTechForeground", "ConnecTechForeground", NotificationManager.IMPORTANCE_MIN);
        channel.setDescription(getString(R.string.app_name));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        Notification.Builder builder = new Notification.Builder(this, "ConnecTechForeground");
        builder.setContentTitle("ConnecTech");

        return builder.build();
    }
}
