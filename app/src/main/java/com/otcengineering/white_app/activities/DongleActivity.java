package com.otcengineering.white_app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.Utils;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class DongleActivity extends AppCompatActivity {
    public static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMISSION_WAKE_LOCK = Manifest.permission.WAKE_LOCK;
    public static final String PERMISSION_BLE = Manifest.permission.BLUETOOTH;
    public static final String PERMISSION_BLE_ADMIN = Manifest.permission.BLUETOOTH_ADMIN;
    public static final String PERMISSION_BLE_PRIVI = Manifest.permission.BLUETOOTH_PRIVILEGED;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_READ_EXT = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_EXT = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_READ_PHONE = Manifest.permission.READ_PHONE_STATE;

    public static String installationNumber;

    private String[] PERMISSIONS = {
            PERMISSION_WAKE_LOCK,
            PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION, PERMISSION_BLE, PERMISSION_BLE_ADMIN, PERMISSION_BLE_PRIVI, PERMISSION_CAMERA, PERMISSION_READ_EXT, PERMISSION_WRITE_EXT, PERMISSION_READ_PHONE};

    private ListView list;

    private ProgressDialog progressDialog;

    private Timer myTimer;

    private String tmpMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OtcBle.getInstance().setContext(this);
        OtcBle.getInstance().createBleLibrary();
        setContentView(R.layout.main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            getSupportActionBar().setTitle(Html.fromHtml("<font color='#001e67'>Dongle</font>"));
        }
        initializeUI();
        super.onCreate(savedInstanceState);

        OtcBle.getInstance().setOnDeviceFound(null);
        OtcBle.getInstance().startScan();

        if (MySharedPreferences.createLogin(this).contains("Mactmp")) {
            tmpMac = MySharedPreferences.createLogin(this).getString("Mactmp");
        }

        list.setClickable(true);
        list.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            String[] test  = list.getItemAtPosition(position).toString().split("-");
            OtcBle.getInstance().setDeviceMac(test[0].trim());
            OtcBle.getInstance().connect();

            progressDialog.show();

            registerToDongle();
        });

        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Utils.runOnUiThread(() -> addBleLists());
            }
        }, 1000, 1000);
    }

    private void addBleLists() {
        Set<String> claus = OtcBle.getInstance().getScanMap().keySet();
        String[] listItems = new String[claus.size()];
        int x = 0;
        for (String clau : claus) {
            listItems[x] = clau + " - " + OtcBle.getInstance().getScanMap().get(clau).getDevice().getName();
             x++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        list.setAdapter(adapter);
    }

    boolean run = true;
    private Semaphore m_sem = new Semaphore(1, false);

    private void registerToDongle(){
        if(OtcBle.getInstance().getDeviceMac().isEmpty() || !m_sem.tryAcquire()) {
            return;
        }

        com.otcengineering.white_app.utils.Utils.runOnMainThread(() -> OtcBle.getInstance().stopScan());

        Handler hndl = new Handler();
        final Runnable runnable = () -> runOnUiThread(() -> {
            m_sem.release();
            progressDialog.dismiss();
            AlertDialog al = new AlertDialog.Builder(DongleActivity.this)
                    .setTitle(getApplicationContext().getString(R.string.no_connect_dongle))
                    .setMessage("Timeout")
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                    .create();
            if (!com.otcengineering.white_app.utils.Utils.isActivityFinish(DongleActivity.this)) {
                al.show();
            }
        });
        hndl.postDelayed(runnable, 200000);

        new Thread(() -> {
            long timeout = System.currentTimeMillis() + 15000;
            int tries = 3;
            runOnUiThread(() -> progressDialog.setMessage(getApplicationContext().getString(R.string.connecting_dongle)));
            while (!OtcBle.getInstance().isConnected()) {
                if (System.currentTimeMillis() >= timeout) {
                    --tries;
                    timeout = System.currentTimeMillis() + 15000;
                    runOnUiThread(() -> OtcBle.getInstance().status = false);
                    runOnUiThread(() -> OtcBle.getInstance().connect());
                    if (tries == 0) {
                        m_sem.release();
                        hndl.removeCallbacks(runnable);
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            OtcBle.getInstance().clearDeviceMac();
                            AlertDialog al = new AlertDialog.Builder(DongleActivity.this)
                                    .setTitle(getApplicationContext().getString(R.string.no_connect_dongle))
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                                    .create();
                            if (!com.otcengineering.white_app.utils.Utils.isActivityFinish(DongleActivity.this)) {
                                al.show();
                            }
                        });
                        return;
                    }
                }
                Utils.wait(this, 100);
            }
            Utils.wait(this, 1000);
            runOnUiThread(() -> progressDialog.setMessage(getApplicationContext().getString(R.string.getting_dongle_state)));

            if (tmpMac == null || !OtcBle.getInstance().getDeviceMac().equals(tmpMac)) {
                byte[] b_regproc_r = OtcBle.getInstance().readLongTag("REGPROC_R", false);
                if (b_regproc_r == null) {
                    m_sem.release();
                    hndl.removeCallbacks(runnable);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        OtcBle.getInstance().clearDeviceMac();
                        OtcBle.getInstance().disconnect();
                        AlertDialog al = new AlertDialog.Builder(DongleActivity.this)
                                .setTitle(getApplicationContext().getString(R.string.no_connect_dongle))
                                .setCancelable(false)
                                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                                .create();
                        if (!com.otcengineering.white_app.utils.Utils.isActivityFinish(DongleActivity.this)) {
                            al.show();
                        }
                    });
                    return;
                }
                int regproc_r = ((b_regproc_r[0] & 255) << 8) | (b_regproc_r[1] & 255);
                if (regproc_r >= 0x100) {
                    runOnUiThread(() -> progressDialog.dismiss());
                    OtcBle.getInstance().clearDeviceMac();
                    OtcBle.getInstance().disconnect();
                    runOnUiThread(() -> new AlertDialog.Builder(DongleActivity.this)
                            .setTitle(getApplicationContext().getString(R.string.dongle_registered))
                            .setPositiveButton("ok", (v, i) -> runOnUiThread(() -> OtcBle.getInstance().disconnect()))
                            .setCancelable(false).show());
                    m_sem.release();
                    hndl.removeCallbacks(runnable);
                    return;
                }
            }

            runOnUiThread(() -> progressDialog.setMessage(getApplicationContext().getString(R.string.reading_data)));
            for (int i = 0; i < 5; ++i) {
                if (!OtcBle.getInstance().isConnected()) {
                    OtcBle.getInstance().connect();
                }
                OtcBle.getInstance().readVin();
                OtcBle.getInstance().readSN();
                String sn = OtcBle.getInstance().serialNumber;
                if (sn != null && !sn.isEmpty()) {
                    MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                    if (tmpMac != null && OtcBle.getInstance().getDeviceMac().equals(tmpMac)) {
                        msp.putString("SN", OtcBle.getInstance().serialNumber);
                        msp.putString("Vin", msp.getString("Vintmp"));

                        runOnUiThread(() -> {
                            try {
                                run = false;
                                m_sem.release();
                                hndl.removeCallbacks(runnable);
                                finish();
                                try {
                                    progressDialog.dismiss();
                                } catch (IllegalArgumentException ignored) {
                                }
                                DongleActivity.this.onBackPressed();
                            } catch (Exception e) {
                                //Log.e("DongleActivity", "Exception", e);
                            }
                        });
                        return;
                    }
                    if(OtcBle.getInstance().vin != null && OtcBle.getInstance().vin.length() == 17) {
                        msp.putString("Vin", OtcBle.getInstance().vin.substring(0, 17));
                        msp.putString("SN", OtcBle.getInstance().serialNumber);
                        msp.putString("macBLE", OtcBle.getInstance().getDeviceMac());
                        String inst = OtcBle.getInstance().getStringValue("autenticate01");
                        if (inst != null) {
                            installationNumber = inst;
                        }
                        runOnUiThread(() -> {
                            try {
                                run = false;
                                m_sem.release();
                                hndl.removeCallbacks(runnable);
                                finish();
                                try {
                                    progressDialog.dismiss();
                                } catch (IllegalArgumentException ignored) {
                                }
                                DongleActivity.this.onBackPressed();
                            } catch (Exception e) {
                                //Log.e("DongleActivity", "Exception", e);
                            }
                        });
                        return;
                    }

                }
                Utils.wait(this, 1000);
            }
            OtcBle.getInstance().disconnect();
            m_sem.release();
            hndl.removeCallbacks(runnable);
            runOnUiThread(() -> {
                String message = "";
                if (OtcBle.getInstance().serialNumber == null || OtcBle.getInstance().serialNumber.isEmpty()) {
                    message = "Invalid Serial Number";
                } else if (OtcBle.getInstance().vin == null || OtcBle.getInstance().vin.length() != 17) {
                    message = "Invalid VIN";
                }
                progressDialog.dismiss();
                OtcBle.getInstance().clearDeviceMac();
                AlertDialog al = new AlertDialog.Builder(DongleActivity.this)
                        .setTitle(getApplicationContext().getString(R.string.no_connect_dongle))
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                        .create();
                if (!com.otcengineering.white_app.utils.Utils.isActivityFinish(DongleActivity.this)) {
                    al.show();
                }
            });
        }, "DongleThread").start();
    }

    private void initializeUI() {
        int PERMISSION_ALL = 1;

        ActivityCompat.requestPermissions(DongleActivity.this, PERMISSIONS, PERMISSION_ALL);

        Button btnScan = findViewById(R.id.refresh);
        list =  findViewById(R.id.list);
        progressDialog = new ProgressDialog(DongleActivity.this);
        progressDialog.setCancelable(false);

        btnScan.setOnClickListener(v -> {
            OtcBle.getInstance().stopScan();

            btnScan.setEnabled(false);
            btnScan.setTextColor(ContextCompat.getColor(this, R.color.gray));

            list.setAdapter(null);
            OtcBle.getInstance().getScanMap().clear();

            Handler hnd = new Handler();
            hnd.postDelayed(() -> {
                OtcBle.getInstance().startScan();
                btnScan.setTextColor(ContextCompat.getColor(this, R.color.textButton));
                btnScan.setEnabled(true);
            }, 500);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerToDongle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        run = false;
        stopTimer();
    }


    private void stopTimer() {
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
    }

}
