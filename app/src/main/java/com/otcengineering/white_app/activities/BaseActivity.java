package com.otcengineering.white_app.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Configuration;
import com.otc.alice.api.model.FileProto;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.blecontrol.interfaces.OnFileTransferComplete;
import com.otcengineering.apible.blecontrol.service.FileTransfer;
import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.interfaces.FOTA;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.service.ConnectDongleService;
import com.otcengineering.white_app.utils.LanguageUtils;
import com.otcengineering.white_app.utils.Logger;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.otcengineering.apible.Utils.tryStartService;


/**
 * Created by cenci7
 */

public class BaseActivity extends EventActivity {
    // variables bluetooth
    private BluetoothAdapter bAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    protected Timer timerBluetooth = new Timer("BluetoothTimer");
    private static boolean isPopUpOpenBluetooth = false;
    public static boolean wasConnected = false;
    // fi variables bluetooth

    // variables fota
    private int fotaSteps = 4;
    // fi variables fota

    protected Timer timer = new Timer("BaseTimer");
    protected Timer timerUpload = new Timer("UploadTimer");
    protected Timer timerServices = new Timer("ServicesTimer");

    public BaseActivity(final String name) {
        super(name);
    }

    public static int retainCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtils.setLocale(MyApp.getUserLocale());

        FileTransfer.onFileTransferComplete = (f, ptr, type) -> {
            File file = new File(getFilesDir(), f);
            try {
                byte[] fileBytes = Utils.readFile(file);
                FileProto.UploadFile uf = FileProto.UploadFile.newBuilder().setFileName(f).setFileData(ByteString.copyFrom(fileBytes)).build();
                for (int i = 0; i < 3; ++i) {
                    try {
                        Shared.OTCResponse resp = ApiCaller.doCall(Endpoints.FILE_UPLOAD, true, uf, Shared.OTCResponse.class);
                        if (resp == null || resp.getStatus() != Shared.OTCStatus.SUCCESS) {
                            com.otcengineering.apible.Utils.wait(this, 1000);
                            Logger.e("FileTransfer", "File " + f + " failed! Try " + i);
                        } else {
                            if (fileBytes.length == 21 && fileBytes[2] == 1) {
                                Logger.e("FileTransfer", "File " + f + " is derived from a 0 initial pointer. Skip pointer increase.");
                            } else {
                                String tag = type == OnFileTransferComplete.FileType.Tracklog ? "TRACKLOG_00" : "statlog_00";
                                OtcBle.getInstance().writeTag(tag, (byte) (ptr + 1), false);
                            }
                            Logger.e("FileTransfer", "File " + f + " uploaded succesfully!");
                            deleteFile(f);
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("FileTransfer", "File " + f + " failed! Try " + i);
                    }
                }
                Logger.e("FileTransfer", "File " + f + " failed! Returning to the function...");
                deleteFile(f);
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        };

        FileTransfer.onFileTransferStarted = () -> {

        };
        HeartBeatService.heartbeatEnabled = true;

        // bluetooth
        createBluetooth();
        uploadsEvry2secs();
        // checkServices();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, ConnectDongleService.class));
        } else {
            startService(new Intent(this, ConnectDongleService.class));
        }

        //fota
        //fota();

        /*fbarcons*/
        Utils.progressDialogRegister = new ProgressDialog(BaseActivity.this);
        Utils.progressDialogRegister.setCancelable(false);
        Utils.progressDialogRegister.setCanceledOnTouchOutside(false);

        Utils.dialogUpdated = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_ok), false);
        Utils.dialogError = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_nok), true);

        /*if (Utils.FOTArunning) {
            Utils.progressDialogRegister.setTitle(getResources().getString(R.string.fota_title));
            Utils.progressDialogRegister.setMessage("Step " + Utils.fotaStep + " of " + fotaSteps);
            Utils.progressDialogRegister.show();

            Handler handler = new Handler();
            Runnable runnable = () -> {
                if (Utils.fotaStep < 5 && Utils.fotaStep > 0) {
                    if (Utils.callbackFOTA != null)
                        Utils.callbackFOTA.fotaError("TIMEOUT EXCEPTION");
                }
            };
            handler.postDelayed(runnable, 20 * 60 * 1_000); //timeout de 20 minuts
        }*/

        Utils.callbackFOTA = new FOTA() {
            @Override
            public void initFotaCallback() {
                runOnUiThread(() -> {
                    try {
                        Utils.progressDialogRegister = new ProgressDialog(MyApp.getContext());
                        Utils.progressDialogRegister.setMessage(getResources().getString(R.string.starting_update));
                        Utils.progressDialogRegister.setCancelable(false);
                        Utils.progressDialogRegister.setCanceledOnTouchOutside(false);

                        Utils.progressDialogRegister.setMessage(getString(R.string.please_keep_car_stopped));
                        Utils.progressDialogRegister.setTitle(getResources().getString(R.string.fota_title));

                        Utils.progressDialogRegister.show();

                        Utils.FOTArunning = true;
                        Utils.FOTAerror = false;
                        Utils.FOTAupdated = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                Handler handler = new Handler();
                Runnable runnable = () -> {
                    if (Utils.fotaStep < 5 && Utils.fotaStep > 0) {
                        if (Utils.callbackFOTA != null) {
                            Utils.callbackFOTA.fotaError("TIMEOUT EXCEPTION");
                            Utils.FOTArunning = false;
                            Utils.FOTAerror = true;
                            Utils.FOTAupdated = false;
                        }
                    }
                };
                handler.postDelayed(runnable, 30 * 60 * 1_000 /*10_000*/); //timeout de 30 minuts

                Utils.fotaStep = 1;

                if (Utils.dialogUpdated.isShowing()) {
                    runOnUiThread(() -> Utils.dialogUpdated.dismiss());
                }

                if (Utils.dialogError.isShowing()) {
                    runOnUiThread(() -> Utils.dialogError.dismiss());
                }
            }

            @Override
            public void fotaUpdateAsk(String version) {
                runOnUiThread(() -> {
                    try {
                        DialogYesNo dyn = new DialogYesNo(MyApp.getContext(), String.format(getString(R.string.update_dongle_) + "\n" + getString(R.string.the_car_should_be_stopped), version),
                                () -> new sendAnswer(true).execute(), () -> new sendAnswer(false).execute());
                        dyn.show();
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            public void fotaUpdateAskYes() {
                (new sendAnswer(true)).execute();
            }

            @Override
            public void fotaUpdated() {
                Utils.fotaStep = 5;

                if (Utils.progressDialogRegister.isShowing()) {
                    runOnUiThread(() -> Utils.progressDialogRegister.dismiss());
                }

                if (Utils.dialogError.isShowing()) {
                    runOnUiThread(() -> Utils.dialogError.dismiss());
                }

                runOnUiThread(() -> {
                    Utils.FOTAupdated = true;
                    Utils.FOTArunning = false;
                    Utils.FOTAerror = false;
                    try {
                        Utils.dialogUpdated = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_ok), false);
                        Utils.dialogUpdated.show();
                    } catch (Exception ignored) {
                        Utils.dialogUpdated = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_ok), false);
                        Utils.dialogError = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_nok), true);
                        try {
                            Utils.dialogUpdated.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void fotaError(String message) {
                Utils.fotaStep = -1;

                if (Utils.progressDialogRegister.isShowing()) {
                    runOnUiThread(() -> Utils.progressDialogRegister.dismiss());
                }

                if (Utils.dialogUpdated.isShowing()) {
                    runOnUiThread(() -> Utils.dialogUpdated.dismiss());
                }

                Utils.dialogError.setMessage(message);

                runOnUiThread(() -> {
                    Utils.FOTAerror = true;
                    Utils.FOTArunning = false;
                    Utils.FOTAupdated = false;
                    try {
                        Utils.dialogError = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_nok), true);
                        Utils.dialogError.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.dialogUpdated = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_ok), false);
                        Utils.dialogError = new CustomDialog(MyApp.getContext(), getString(R.string.dongle_updated_nok), true);
                        try {
                            Utils.dialogError.show();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void fotaDeletingImage() {
                Utils.fotaStep = 2;
                runOnUiThread(() -> Utils.progressDialogRegister.setMessage(getString(R.string.step_x_y, Utils.fotaStep, fotaSteps)));

                if (Utils.dialogUpdated.isShowing()) {
                    runOnUiThread(() -> Utils.dialogUpdated.dismiss());
                }

                if (Utils.dialogError.isShowing()) {
                    runOnUiThread(() -> Utils.dialogError.dismiss());
                }

                if (!Utils.progressDialogRegister.isShowing()) {
                    Utils.progressDialogRegister.setTitle(getResources().getString(R.string.fota_title));
                    runOnUiThread(() -> {
                        Utils.FOTArunning = true;
                        Utils.FOTAerror = false;
                        Utils.FOTAupdated = false;
                        try {
                            if (!Utils.isActivityFinish(BaseActivity.this)) {
                                Utils.progressDialogRegister.show();
                            }
                        } catch (Exception ignored) {

                        }
                    });
                }
            }

            @Override
            public void fotaSendingNewFw() {
                Utils.fotaStep = 3;
                runOnUiThread(() -> Utils.progressDialogRegister.setMessage(getString(R.string.step_x_y, Utils.fotaStep, fotaSteps)));

                if (Utils.dialogUpdated.isShowing()) {
                    runOnUiThread(() -> Utils.dialogUpdated.dismiss());
                }

                if (Utils.dialogError.isShowing()) {
                    runOnUiThread(() -> Utils.dialogError.dismiss());
                }

                if (!Utils.progressDialogRegister.isShowing()) {
                    Utils.progressDialogRegister.setTitle(getResources().getString(R.string.fota_title));
                    runOnUiThread(() -> {
                        Utils.FOTArunning = true;
                        Utils.FOTAerror = false;
                        Utils.FOTAupdated = false;
                        try {
                            if (!Utils.isActivityFinish(BaseActivity.this)) {
                                Utils.progressDialogRegister.show();
                            }
                        } catch (Exception ignored) {

                        }
                    });
                }
            }

            @Override
            public void fotaRestartDongle() {
                Utils.fotaStep = 4;
                runOnUiThread(() -> {
                    try {
                        if (Utils.progressDialogRegister == null) {
                            Utils.progressDialogRegister = new ProgressDialog(MyApp.getContext());
                            Utils.progressDialogRegister.setCancelable(false);
                            Utils.progressDialogRegister.setCanceledOnTouchOutside(false);
                            Utils.progressDialogRegister.show();
                        }
                        Utils.progressDialogRegister.setMessage(getResources().getString(R.string.restarting_dongle));
                    } catch (Exception e) {
                        try {
                            Utils.progressDialogRegister = new ProgressDialog(MyApp.getContext());
                            Utils.progressDialogRegister.setCancelable(false);
                            Utils.progressDialogRegister.setCanceledOnTouchOutside(false);
                            Utils.progressDialogRegister.show();
                            Utils.progressDialogRegister.setMessage(getResources().getString(R.string.restarting_dongle));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    try {
                        if (!Utils.progressDialogRegister.isShowing()) {
                            Utils.progressDialogRegister.setTitle(getResources().getString(R.string.fota_title));
                            Utils.FOTArunning = true;
                            Utils.FOTAerror = false;
                            Utils.FOTAupdated = false;
                            try {
                                if (!Utils.isActivityFinish(BaseActivity.this)) {
                                    Utils.progressDialogRegister.show();
                                }
                            } catch (Exception ignored) {

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                if (Utils.dialogUpdated.isShowing()) {
                    runOnUiThread(() -> Utils.dialogUpdated.dismiss());
                }

                if (Utils.dialogError.isShowing()) {
                    runOnUiThread(() -> Utils.dialogError.dismiss());
                }
            }
        };
        /*fi fbarcons*/
    }

    private void checkServices() {
        timerServices.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tryStartService(FileTransfer.class, BaseActivity.this);
                //tryStartService(UpdateCarService.class, BaseActivity.this);
                tryStartService(HeartBeatService.class, BaseActivity.this);

                /*if (OtcBle.getInstance().isConnected() && !fotaTried) {
                    new Thread(() -> fota()).start();
                }*/
            }
        }, 0, 10000);
    }

    private void uploadsEvry2secs() {
        timerUpload.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (bAdapter != null && !isPopUpOpenBluetooth && !bAdapter.isEnabled()) {
                            activateBlue();
                            isPopUpOpenBluetooth = true;
                        } else if (bAdapter != null && bAdapter.isEnabled()) {
                            isPopUpOpenBluetooth = false;
                        }
                    }
                },
                0,
                2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timerUpload.cancel();
        timerServices.cancel();
        isPopUpOpenBluetooth = true; // testing
        timerBluetooth.cancel();
    }

    protected void showCustomDialog(int messageRes, DialogInterface.OnDismissListener listener) {
        if (!this.isFinishing()) {
            CustomDialog customDialog = new CustomDialog(this, messageRes, false);
            customDialog.setOnDismissListener(listener);
            customDialog.show();
        }
    }

    protected void showCustomDialog(int messageRes) {
        if (!this.isFinishing()) {
            CustomDialog customDialog = new CustomDialog(this, messageRes, false);
            customDialog.show();
        }
    }

    protected void showCustomDialog(String msg) {
        if (!this.isFinishing()) {
            CustomDialog customDialog = new CustomDialog(this, msg, false);
            customDialog.show();
        }
    }

    protected void showCustomDialogError() {
        showCustomDialogError("Error");
    }

    protected void showCustomDialogError(String message) {
        if (!this.isFinishing()) {
            CustomDialog customDialog = new CustomDialog(this, message, true);
            customDialog.show();
        }
    }
    // funcions afegides bluetooth

    private void createBluetooth() {
        OtcBle.getInstance().setContext(this);
        OtcBle.getInstance().createBleLibrary();
        bAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void activateBlue() {
        isPopUpOpenBluetooth = true;
        if (!bAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // fi funcions afegides bluetooth

    static boolean fotaTried = false;

    // funcions fota
    protected void fota() {
        //Get dongle firmware versions
        //String fmVersion = OtcBle.getInstance();
        //OtcBle.getInstance().register();
        fotaTried = true;
        MySharedPreferences msp = MySharedPreferences.createLogin(getBaseContext());
        MySharedPreferences fta = MySharedPreferences.createFota(getApplicationContext());

        Log.d("FOTA", String.format("%d.%d", OtcBle.getInstance().gpsStatus.s12FwMajor, OtcBle.getInstance().gpsStatus.s12FwMinor));

        com.otcengineering.white_app.utils.FOTA fota = new com.otcengineering.white_app.utils.FOTA(this, OtcBle.getInstance().getDeviceMac());

        if (fta.getBoolean("started")) {
            fota.loadFile("firmware.bin");
            fota.writeImage();
            startBootloader(fota);
        } else {
            fota.getFirmwareVersions();
            Configuration.Firmware fm;
            try {
                Configuration.FirmwareVersion.Builder fmVersion = Configuration.FirmwareVersion.newBuilder();
                fmVersion.setVersion(String.format(Locale.US, "%d.%d", OtcBle.getInstance().gpsStatus.s12FwMajor, OtcBle.getInstance().gpsStatus.s12FwMinor));
                fm = ApiCaller.doCall(Endpoints.FIRMWARE, msp.getBytes("token"), fmVersion.build(), Configuration.Firmware.class);
                Log.d("Fota", fm.getDate() + " " + fm.getVersion() + " " + fm.getFileId());
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
                return;
            }

            String[] parts = fm.getVersion().split("\\.");
            int M = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);

            if (OtcBle.getInstance().gpsStatus.s12FwMajor <= M && (OtcBle.getInstance().gpsStatus.s12FwMajor != M || OtcBle.getInstance().gpsStatus.s12FwMinor < m)) {
                try {
                    byte[] file = ApiCaller.getImage(Endpoints.FILE_GET + fm.getFileId(), MySharedPreferences.createLogin(getApplicationContext()).getString("token"));
                    if (fota.processFile("firmware", file)) {
                        saveFile(file);
                        fota.loadFile("firmware.bin");
                    }
                } catch (ApiCaller.OTCException e) {
                    e.printStackTrace();
                }

                fota.clearMemory();
                fota.writeImage();
                startBootloader(fota);
            }
        }
    }

    private void startBootloader(com.otcengineering.white_app.utils.FOTA ft) {
        runOnUiThread(() -> {
            DialogYesNo dyn = new DialogYesNo(BaseActivity.this, getString(R.string.update_dongle), ft::bootLoad, () -> {});
            dyn.show();
        });
    }

    private void saveFile(@NonNull final byte[] bs) {
        try {
            FileOutputStream fos = openFileOutput("firmware.bin", MODE_PRIVATE);
            fos.write(bs);
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static class sendAnswer extends AsyncTask<Object, Object, Shared.OTCResponse> {
        boolean ans;

        sendAnswer(boolean answer) {
            ans = answer;
        }
        @Override
        protected Shared.OTCResponse doInBackground(Object... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(MyApp.getContext());

                Configuration.FirmwareAnswer.Builder firmwareAnswer = Configuration.FirmwareAnswer.newBuilder();
                firmwareAnswer.setAnswer(ans);
                return ApiCaller.doCall(Endpoints.FIRMWARE_ANSWER, msp.getBytes("token"), firmwareAnswer.build(), Shared.OTCResponse.class);
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Shared.OTCResponse response) {
            super.onPostExecute(response);
            try {
                if (ans) {
                    if (response.getStatus() == Shared.OTCStatus.SUCCESS) {
                        if (Utils.callbackFOTA != null)
                            Utils.callbackFOTA.initFotaCallback();
                    } else {
                        if (Utils.callbackFOTA != null)
                            Utils.callbackFOTA.fotaError(response.getStatus().getValueDescriptor().getFullName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
