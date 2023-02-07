package com.otcengineering.white_app.utils;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.tasks.GenericTask;

import java.util.Calendar;

public class NetworkManager {
    public static General.VehicleStatus getVehicleStatus(final Context ctx) {
        try {
            return ApiCaller.doCall(Endpoints.VEHICLE_STATUS, MySharedPreferences.createLogin(ctx).getBytes("token"), null, General.VehicleStatus.class);
        } catch (ApiCaller.OTCException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean login(@NonNull final Context ctx, @Nullable String username, @Nullable char[] password) {
        if (username == null) {
            username = MySharedPreferences.createLogin(ctx).getString("Nick");
        }

        if (password == null) {
            password = MySharedPreferences.createLogin(ctx).getCharArray("Pass");
        }

        Welcome.Login login = Welcome.Login.newBuilder()
                .setPassword(new String(password))
                .setUsername(username)
                .setMobileIMEI(Utils.getImei())
                .build();
        for (int i = 0; i < password.length; ++i) {
            password[i] = '\0';
        }
        try {
            Shared.OTCResponse response = ApiCaller.doCall(Endpoints.LOGIN, login, Shared.OTCResponse.class);
            Logger.d("Login", response.getStatus().name());
            if (response.getStatus() == Shared.OTCStatus.SUCCESS) {
                Welcome.LoginResponse resp = response.getData().unpack(Welcome.LoginResponse.class);
                MySharedPreferences.createLogin(ctx).putBytes("token", resp.getApiTokenBytes().toByteArray());
                MySharedPreferences.createLogin(ctx).putLong("ID", resp.getUserId());
                MySharedPreferences.createLogin(ctx).putLong("token_date", Calendar.getInstance().getTime().getTime());
                Welcome.DeviceSpecs ds = Welcome.DeviceSpecs.newBuilder()
                        .setAppVersion(BuildConfig.VERSION_NAME)
                        .setMobileSO(String.format("Android %s", Build.VERSION.RELEASE))
                        .setMobileIMEI(Utils.getImei())
                        .build();
                GenericTask gt = new GenericTask(Endpoints.DEVICE_SPECS, ds, true, (v) -> {
                });
                gt.execute();
                return true;
            } else if (response.getStatus() == Shared.OTCStatus.NEW_MOBILE) {
                // Utils.changedPhone(MyApp.getContext());
                return false;
            } else {
                return false;
            }
        } catch (InvalidProtocolBufferException | ApiCaller.OTCException | NullPointerException e) {
            return false;
        }
    }
}
