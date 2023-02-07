package com.otcengineering.white_app.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.otcengineering.white_app.MyApp;

public class PassiveBluetoothService extends IntentService {
    public PassiveBluetoothService() {
        super("PassiveBluetoothService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("PassiveBluetoothService", "Hola");
    }

    @Override
    public void onDestroy() {
        resetService();
        Log.d("PassiveBluetoothService", "Adeu");
        super.onDestroy();
    }

    private void resetService() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(this, PassiveBluetoothService.class);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 420, serviceIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        if (am != null) {
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000, servicePendingIntent);
        }
    }
}