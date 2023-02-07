package com.otcengineering.white_app.network;

import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.Configuration;
import com.otc.alice.api.model.FileProto;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.Crypt;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigurationNetwork {
    public static void fetchBluetoothConfiguration() {
        if (!MySharedPreferences.createLogin(MyApp.getContext()).contains("token")) {
            return;
        }
        if (MySharedPreferences.createLogin(MyApp.getContext()).contains("paramBle")) {
            byte[] bs = MySharedPreferences.createLogin(MyApp.getContext()).getBytes("paramBle");
            if (bs != null && bs.length > 0) {
                Crypt.setKeySpec(bs);
                Arrays.fill(bs, Utils.ZERO);
            } else {
                MySharedPreferences.createLogin(MyApp.getContext()).remove("paramBle");
                Log.e("ConfigurationNetwork", "Failed to obtain key.");
                Timer tmr = new Timer("ConfigurationTimer2");
                tmr.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ConfigurationNetwork.fetchBluetoothConfiguration();
                    }
                }, 15000);
            }
        }
        GenericTask gt = new GenericTask(Endpoints.BLUETOOTH_SETTINGS, null, true, otcResponse -> {
            if (otcResponse != null && otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                try {
                    Configuration.BluetoothSettings ble = otcResponse.getData().unpack(Configuration.BluetoothSettings.class);
                    byte[] bs = ble.getSetting1().toByteArray();
                    ble = null;
                    MySharedPreferences.createLogin(MyApp.getContext()).putBytes("paramBle", bs);
                    Crypt.setKeySpec(bs);
                    Arrays.fill(bs, Utils.ZERO);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    byte[] bs = MySharedPreferences.createLogin(MyApp.getContext()).getBytes("paramBle");
                    if (bs != null && bs.length > 0) {
                        Crypt.setKeySpec(bs);
                        Arrays.fill(bs, Utils.ZERO);
                    } else {
                        MySharedPreferences.createLogin(MyApp.getContext()).remove("paramBle");
                        Timer tmr = new Timer("ConfigurationTimer");
                        tmr.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ConfigurationNetwork.fetchBluetoothConfiguration();
                            }
                        }, 15000);
                    }
                }
            } else {
                byte[] bs = MySharedPreferences.createLogin(MyApp.getContext()).getBytes("paramBle");
                if (bs != null && bs.length > 0) {
                    Crypt.setKeySpec(bs);
                    Arrays.fill(bs, Utils.ZERO);
                } else {
                    MySharedPreferences.createLogin(MyApp.getContext()).remove("paramBle");
                    Log.e("ConfigurationNetwork", "Failed to obtain key.");
                    Timer tmr = new Timer("ConfigurationTimer2");
                    tmr.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ConfigurationNetwork.fetchBluetoothConfiguration();
                        }
                    }, 15000);
                }
            }
        });
        gt.execute();
    }

    @WorkerThread
    public static Boolean isReportingEnabled() {
        MySharedPreferences msp = MySharedPreferences.createLogin(MyApp.getContext());
        if (msp.getString("Nick").isEmpty()) {
            return null;
        } else {
            Configuration.BluetoothReportingIsEnabled brie = Configuration.BluetoothReportingIsEnabled.newBuilder().setUserName(msp.getString("Nick")).build();
            try {
                Shared.OTCResponse resp = ApiCaller.doCall(Endpoints.Internal.IS_REPORTING_ENABLED, true, brie, Shared.OTCResponse.class);
                return resp != null && resp.getStatus() == Shared.OTCStatus.SUCCESS;
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @WorkerThread
    public static boolean uploadReport(String report) {
        FileProto.UploadFile uf = FileProto.UploadFile.newBuilder().setFileData(ByteString.copyFrom(report.getBytes())).build();
        try {
            Shared.OTCResponse resp = ApiCaller.doCall(Endpoints.Internal.REPORT_ISSUE, true, uf, Shared.OTCResponse.class);
            return resp.getStatus() == Shared.OTCStatus.SUCCESS;
        } catch (ApiCaller.OTCException e) {
            e.printStackTrace();
            return false;
        }
    }
}
