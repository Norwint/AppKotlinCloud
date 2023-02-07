package com.otcengineering.white_app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FotaServiceRestart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Log.d("TransmitterServiceR", "onReceiveRestart");
        context.startService(new Intent(context, FotaService.class));
    }
}
