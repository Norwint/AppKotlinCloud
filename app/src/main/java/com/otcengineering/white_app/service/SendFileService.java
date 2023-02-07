package com.otcengineering.white_app.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.Utils;
import com.otcengineering.apible.blecontrol.service.FileTransfer;

/**
 * Created by cenci7
 */

public class SendFileService extends IntentService {

    public static final String TAG = "SendFileService";

    public static final int SERVICE_ID = 1;


    public SendFileService() {
        super("SendFileService");
    }

    public SendFileService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.d(TAG, "onHandleIntent");
        Handler handlerUpload = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new connectDongleTask().execute();

                handlerUpload.postDelayed(this, 5000);
            }
        };
        handlerUpload.postDelayed(runnable, 1000);

        new connectDongleTask().execute();
        new generalReadTask().execute();

    }

    @SuppressLint("StaticFieldLeak")
    private class connectDongleTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            //Log.d("CONNECT", "Exec");
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                String macBLE = msp.getString("macBLE");
                //Log.d(TAG, macBLE);

                if (!macBLE.equals("") && !OtcBle.getInstance().isConnected()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        OtcBle.getInstance().setContext(getApplicationContext());
                        OtcBle.getInstance().createBleLibrary();
                        OtcBle.getInstance().setDeviceMac(macBLE);
                        OtcBle.getInstance().connect();
                        if (!Utils.appInBackground(getApplicationContext())) {
                            startService(new Intent(getApplicationContext(), FileTransfer.class));
                        }
                        FileTransfer.hasToRead = true;
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class generalReadTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                //Log.d("UPLOAD", "Exec");
                // OtcBle.getInstance().updateCarData();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
