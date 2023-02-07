package com.otcengineering.white_app.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.Utils;
import com.otcengineering.apible.blecontrol.utils.Crc32;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.utils.interfaces.OnUpdate;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by OTC_100 on 5/4/2018.
 */

public final class FOTA {
    public enum VersionState {
        Equals, OldA, OldB, Undefined, UpToDate, Count, Invalid
    }

    public enum FotaCommand {
        Initial, CheckA, CheckB, EraseA, EraseB, ExecuteBoot, ConfirmVersion, Reset, Count, Invalid
    }

    private static final String TAG = "OtcFota";
    private byte[] m_file;
    private int m_crc32;
    private int lastVersion = 22;
    private int m_aVersion = -1, m_bVersion = -1;

    private static final int ADDRESS_A = 0xF83000, ADDRESS_B = 0xFC1800;
    private VersionState m_state;

    private static OtcBle ble = OtcBle.getInstance();

    public static void setOnProgress(OnUpdate onProgress) {
        ble.onFileTransferUpdate = onProgress::onUpdate;
    }

    public boolean isConnected() {
        return OtcBle.getInstance().isConnected();
    }

    public FOTA(final Context context, final String mac) {
        if (!EasyPermissions.hasPermissions(context, "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.BLUETOOTH_PRIVILEGED")) {
            EasyPermissions.requestPermissions((Activity) context, "Bluetooth Permissions", 1000, "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.BLUETOOTH_PRIVILEGED");
        }
        OtcBle.usesCrypt = true;
        if (OtcBle.getInstance().getContext() == null)
            OtcBle.getInstance().setContext(context);
        if (!OtcBle.getInstance().isConnected()) {
            OtcBle.getInstance().setDeviceMac(mac);
            OtcBle.getInstance().createBleLibrary();
            OtcBle.getInstance().connect();
        }
    }

    public boolean isNewer(final String version) {
        String[] vs = version.split("\\.");
        int major = Integer.parseInt(vs[0], 16);
        int minor = Integer.parseInt(vs[1], 16);

        return OtcBle.getInstance().gpsStatus.s12FwMajor < major || OtcBle.getInstance().gpsStatus.s12FwMinor < minor;
    }

    public boolean loadFile(final String file) {
        try {
            m_file = loadFile(file, OtcBle.getInstance().getContext());
            return processFile(file, m_file);
        } catch (IOException e) {
            Log.e(TAG, "Cannot load file.", e);
            return false;
        }
    }

    public boolean processFile(String file, byte[] data) {
        m_file = data;
        int length = m_file.length;
        byte[] lengthByte = Utils.intToDword(length);
        byte[] arrToCRC = new byte[m_file.length + lengthByte.length];
        System.arraycopy(lengthByte, 0, arrToCRC, 0, lengthByte.length);
        System.arraycopy(m_file, 0, arrToCRC, lengthByte.length, m_file.length);
        m_crc32 = Crc32.computeBuffer(arrToCRC);
        Log.d(TAG, "File: " + file + " Size: " + length + " CRC32: " + m_crc32);
        runOnUiThread(() -> Toast.makeText(OtcBle.getInstance().getContext(), "File: " + file + " Size: " + length + " CRC32: " + m_crc32, Toast.LENGTH_LONG).show());
        int crc = Crc32.computeBuffer(m_file);
        Log.d(TAG, "" + crc);
        return true;
    }

    public final int checkFotaResult() {
        byte[] response = OtcBle.getInstance().readLongTag("fotaResult", false);
        if (response == null) {
            Log.e(TAG, "FotaResult is not accessible or an error has occurred.");
            ((Activity) OtcBle.getInstance().getContext()).runOnUiThread(() ->
                    Toast.makeText(OtcBle.getInstance().getContext(), "FotaResult is not accessible or an error has occurred.", Toast.LENGTH_LONG).show());
            return -1;
        }
        int fotaState = response[0] & 255;
        final String msg;
        if (fotaState == 0) {
            msg = "No operation";
        } else if (fotaState == 0x1 || fotaState == 0x2) {
            msg = (fotaState == 0x1 ? "A" : "B") + " area without data.";
        } else if (fotaState == 0x3 || fotaState == 0x4) {
            msg = (fotaState == 0x3 ? "A" : "B") + " FW correctly loaded.";
        } else if ((fotaState & 0x30) == 0x30) {
            msg = ("A image error: " + (fotaState & 0xf) + ".");
            fotaError((byte) (fotaState & 0xf), 'A', OtcBle.getInstance().getContext());
        } else if ((fotaState & 0x40) == 0x40) {
            msg = ("B image error: " + (fotaState & 0xf) + ".");
            fotaError((byte) (fotaState & 0xf), 'B', OtcBle.getInstance().getContext());
        } else if (fotaState == 0x5 || fotaState == 0x6) {
            msg = (fotaState == 0x5 ? "A" : "B") + " area evaluating.";
        } else if (fotaState == 0x7 || fotaState == 0x8) {
            msg = (fotaState == 0x7 ? "A" : "B") + " area erasing.";
        } else if (fotaState == 0x09) {
            msg = "Starting boot...";
        } else if (fotaState == 0x0A) {
            msg = "Cannot boot";
        } else if (fotaState == 0xFF) {
            msg = "Fota applied";
        } else if (fotaState == 0x10) {
            msg = ("Device not ready to do the FOTA operation.");
        } else msg = "";
        Log.d(TAG, msg);
        try {
            Utils.runOnUiThread(() -> Toast.makeText(OtcBle.getInstance().getContext(), msg, Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Log.e("OtcFota", "Exception at: checkFotaResult", e);
        }
        return response[0] & 255;
    }

    private void fotaError(final byte i, final char img, final Context context) {
        final String msg;
        switch (i) {
            case 3:
                msg = "Unloaded Image";
                break;
            case 4:
                msg = "File Not Found";
                break;
            case 5:
                msg = "File Over Sized";
                break;
            case 6:
                msg = "Row Not Found";
                break;
            case 7:
                msg = "End Not Found";
                break;
            case 8:
                msg = "Check Error";
                break;
            case 9:
                msg = "CRC Error";
                break;
            case 10:
                msg = "Version Error";
                break;
            case 11:
                msg = "Command Error";
                break;
            default:
                msg = "";
                break;
        }

        try {
            ((Activity) context).runOnUiThread(() -> {
                Log.e(TAG, "Error with Image " + img + ": " + msg);
                Toast.makeText(context, "Error with Image " + img + ": " + msg, Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    public boolean getFirmwareVersions() {
        byte[] tmp = OtcBle.getInstance().readLongTag("fotaImgA", false);
        if (tmp == null) {
            Log.e(TAG, "Cannot get Image A version.");
            return false;
        }
        m_aVersion = Utils.wordToInt(tmp[0], tmp[1]);
        tmp = OtcBle.getInstance().readLongTag("fotaImgB", false);
        if (tmp == null) {
            Log.e(TAG, "Cannot get Image B version.");
            return false;
        }
        m_bVersion = Utils.wordToInt(tmp[0], tmp[1]);
        //m_state = checkVersionsFirmware();
        m_state = VersionState.OldA;
        Log.d(TAG, String.format("State: %s Version A: %d.%d Version B: %d.%d", m_state, (m_aVersion >> 8), m_aVersion & 255, (m_bVersion >> 8), m_bVersion & 255));
        runOnUiThread(() -> Toast.makeText(OtcBle.getInstance().getContext(), String.format(Locale.getDefault(), "State: %s Version A: %d.%d Version B: %d.%d", m_state, (m_aVersion >> 8), m_aVersion & 255, (m_bVersion >> 8), m_bVersion & 255), Toast.LENGTH_LONG).show());
        return true;
    }

    private void runOnUiThread(final Runnable run) {
        try {
            ((Activity) OtcBle.getInstance().getContext()).runOnUiThread(run);
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    public void downloadFirmware(final Context ctx) {
        //new Thread(this::downloadFirmwareFTP).start();
        try {
            InputStream isr = ctx.getAssets().open("firmware.s19");
            FileOutputStream fos = OtcBle.getInstance().getContext().openFileOutput("firmware.s19", Context.MODE_PRIVATE);
            int size = isr.available();
            byte[] buffer = new byte[size];
            isr.read(buffer);
            fos.write(buffer);
            isr.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void downloadFirmwareFTP() {
        FTPClient client = new FTPClient();
        try {
            FileOutputStream fos = OtcBle.getInstance().getContext().openFileOutput("firmware.s19", Context.MODE_PRIVATE);
            client.connect("nuram.es");
            client.login("jns@nuram.es", "Datsun.OTC.123");
            boolean status = client.retrieveFile("/Firmware/firmware.s19", fos);
            Log.d(TAG, "Status = " + status);
            Log.d(TAG, "reply = " + client.getReplyString());

            ((Activity) OtcBle.getInstance().getContext()).runOnUiThread(() -> Toast.makeText(OtcBle.getInstance().getContext(), "Downloaded firmware", Toast.LENGTH_SHORT).show());

            fos.close();
            client.disconnect();
        } catch (IOException e) {
            Log.e(TAG, "Download from FTP exception: ", e);
        }
    }

    public static byte[] loadFile(String filename, Context context) throws IOException {
        FileInputStream fis = context.openFileInput(filename);
        File f = context.getFileStreamPath(filename);
        byte[] arr = new byte[(int)f.length()];
        final int read = fis.read(arr);
        if(read == 0) Log.e(TAG, "LoadFile: Empty File");
        /*byte tmp = 0;
        int i = 0;
        while (tmp != 10 && i < arr.length) {
            tmp = arr[i];
            ++i;
        }

        byte[] newArr = new byte[arr.length - i];
        System.arraycopy(arr, i, newArr, 0, arr.length - i);

        return newArr;*/
        return arr;
    }

    public boolean clearMemory() {
        // Check if the fota is possible
        checkFotaResult();
        // Send cmmdFota to clear
        if (m_state == VersionState.OldB)
            sendFotaCommand(FotaCommand.EraseB);
        else if (m_state == VersionState.OldA || m_state == VersionState.Equals)
            sendFotaCommand(FotaCommand.EraseA);
        // Check state
        sendFotaCommand(m_state == VersionState.OldB ? FotaCommand.CheckB : FotaCommand.CheckA);
        int fotaState = checkFotaResult();
        return fotaState == 0x1 || fotaState == 0x2;
    }

    public void sendFotaCommand(final FotaCommand command) {
        byte cmd = 0;
        switch (command) {
            case Initial:
                break;
            case CheckA:
                cmd = 1;
                break;
            case CheckB:
                cmd = 2;
                break;
            case EraseA:
                cmd = 3;
                break;
            case EraseB:
                cmd = 4;
                break;
            case ExecuteBoot:
                cmd = 5;
                break;
            case ConfirmVersion:
                cmd = 6;
                break;
            case Reset:
                cmd = -1;
                break;
            case Invalid:
            default:
                return;
        }
        OtcBle.getInstance().writeTag("cmmdFota", cmd, false);
    }

    public VersionState checkVersionsFirmware() {
        if (m_aVersion == -1 || m_bVersion == -1 || lastVersion == -1)
            return VersionState.Undefined;
        if (m_aVersion == lastVersion || m_bVersion == lastVersion)
            return VersionState.UpToDate;
        int A = (m_aVersion >> 8);
        int a = m_aVersion & 255;
        int B = (m_bVersion >> 8);
        int b = (m_bVersion & 255);

        if (B < A || B == A && b < a)
            return VersionState.OldB;
        else return VersionState.OldA;
    }

    public boolean writeImage() {
        int address;
        if (!MySharedPreferences.createFota(OtcBle.getInstance().getContext()).getBoolean("started")) {
            if (m_state == VersionState.Equals || m_state == VersionState.OldA)
                address = ADDRESS_A;
            else if (m_state == VersionState.OldB)
                address = ADDRESS_B;
            else {
                /*((Activity) OtcBle.getInstance().getContext()).runOnUiThread(() ->
                        Toast.makeText(OtcBle.getInstance().getContext(), "Firmware version not checked. Please check the version before updating the firmware.",
                                Toast.LENGTH_lonG).show());*/
                return false;
            }
            MySharedPreferences.createFota(OtcBle.getInstance().getContext()).putInteger("address", address);
        } else {
            address = MySharedPreferences.createFota(OtcBle.getInstance().getContext()).getInteger("address");
        }

        if (!MySharedPreferences.createFota(OtcBle.getInstance().getContext()).getBoolean("started")) {
            /*VarEx.getInstance().write(4, address, Utils.intToDword(m_file.length));
            VarEx.getInstance().write(4, address + 4 + m_file.length, Utils.intToDword(m_crc32));*/
        }

        MySharedPreferences.createFota(OtcBle.getInstance().getContext()).putBoolean("started", true);
        MySharedPreferences.createFota(OtcBle.getInstance().getContext()).putLong("length", m_file.length);

        int bytes = 0;
        if (MySharedPreferences.createFota(OtcBle.getInstance().getContext()).getInteger("sent") > 0) {
            bytes = MySharedPreferences.createFota(OtcBle.getInstance().getContext()).getInteger("sent");
        }
        while (bytes < m_file.length) {
            int toS = 1024;
            if (toS + bytes > m_file.length) {
                toS = m_file.length - bytes;
            }
            byte[] toSend = new byte[toS];
            System.arraycopy(m_file, bytes, toSend, 0, toS);
            try {
                if (!OtcBle.getInstance().writeLong(4, address + 4 + bytes, toSend, false)) {
                    Utils.wait(this, 5000);
                    continue;
                }
            } catch (Exception e) {
                Utils.wait(this, 5000);
                continue;
            }
            bytes += toS;
            MySharedPreferences.createFota(OtcBle.getInstance().getContext()).putInteger("sent", bytes);
            Log.d("FOTA", String.format("%d / %d", bytes, m_file.length));
        }

        Utils.wait(this, 10000);

        // Check state
        sendFotaCommand(m_state == VersionState.OldB ? FotaCommand.CheckB : FotaCommand.CheckA);
        int state = checkFotaResult();
        return state == 0x3 || state == 0x4;
    }

    public boolean bootLoad() {
        byte[] valid = validate();
        if (valid != null) {
            OtcBle.getInstance().writeTag("fotaNewFW", (short)Utils.wordToInt(valid[0], valid[1]), false);
            ((Activity) OtcBle.getInstance().getContext()).runOnUiThread(() -> {
                Toast.makeText(OtcBle.getInstance().getContext(), String.format(Locale.getDefault(), "Image Version: %d.%d", (valid[0]), valid[1]), Toast.LENGTH_LONG).show();
                Log.d(TAG, String.format(Locale.getDefault(), "Image Version: %d.%d", (valid[0]), valid[1]));
            });
            sendFotaCommand(FotaCommand.ConfirmVersion);
            Utils.wait(this, 1000);
            sendFotaCommand(FotaCommand.ExecuteBoot);
            return true;
        }
        MySharedPreferences.createFota(MyApp.getContext()).clear();

        return false;
    }

    public byte[] validate() {
        if (m_state == VersionState.OldB) sendFotaCommand(FotaCommand.CheckB);
        else sendFotaCommand(FotaCommand.CheckA);

        int result = checkFotaResult();
        while (result == 5 || result == 6) result = checkFotaResult();
        if (result == 0x3) {
            return OtcBle.getInstance().readLongTag("fotaImgA", false);
        } else if (result == 0x4) {
            return OtcBle.getInstance().readLongTag("fotaImgB", false);
        } else {
            return null;
        }
    }
}
