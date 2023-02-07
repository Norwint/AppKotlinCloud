package com.otcengineering.white_app.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.apible.CarStatus;
import com.otcengineering.apible.Constants;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.Utils;
import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.interfaces.INotificable;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.service.ConnectDongleService;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDateTime;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

public class CarStatusFragment extends EventFragment implements INotificable {
    private TextView engine, fuel, hbeam, lbeam, positionLights, doors, warnings, odometer, vehicleCondition, dongle;
    private ConstraintLayout fuelLayout, doorsLayout, vehicleConditionLayout, warningsLayout, dongleLayout;
    private TextView statusWorking;

    private ImageView statusAlert;

    private LinearLayout bottomLayout;
    private TextView timeText;
    private Button checkButton;

    private Timer refreshTimer = new Timer("CarStatusTimer");
    private Timer refreshNetworkTimer = new Timer("CarStatusNetworkTimer");
    private Handler handler;
    private Runnable runnable;

    public CarStatusFragment() {
        super("CarStatusActivity");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(this::loadCached, "CarStatusThread").start();
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshTimer.cancel();
        refreshTimer = null;
        refreshNetworkTimer.cancel();
        refreshNetworkTimer = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (refreshTimer == null) {
            refreshTimer = new Timer("CarStatusTimer");
        }
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getContext() != null && !com.otcengineering.white_app.utils.Utils.isActivityFinish(getContext())) {
                    if (OtcBle.getInstance().isConnected() && HeartBeatService.isRunning) {
                        Utils.runOnUiThread(() -> {
                            if (getContext() != null && !com.otcengineering.white_app.utils.Utils.isActivityFinish(getContext())) {
                                bottomLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                checkButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.check_button)));
                            }
                        });
                        setTime(DateUtils.getLocalDate());

                        if (!ConnectDongleService.firstTime) {
                            return;
                        }
                        CarStatus cs = OtcBle.getInstance().carStatus;
                        if (cs.getBitVar(Constants.KL15) != null) {
                            boolean vc = cs.getBitVar("EngineNotif") || cs.getBitVar("EpsNotif") || cs.getBitVar("AscNotif") || cs.getBitVar("BrakeSystemNotif") ||
                                    cs.getBitVar("AbsNotif") || cs.getBitVar("OssImmoNotif") || cs.getBitVar("KosNotif") || cs.getBitVar("SrsNotif") ||
                                    cs.getBitVar("AtNotif") || cs.getBitVar("OilNotif") || cs.getBitVar("ChargeNotif") ||  cs.getBitVar("BrakeFluidNotif") ||
                                    cs.getBitVar("OssElecNotif") || cs.getBitVar("OssSteeNotif");
                            setValues(cs.getBitVar("EngineOnoffNotif"), cs.getBitVar("LowFuel"),
                                    cs.getBitVar(Constants.HIGH_BEAM), cs.getBitVar(Constants.LOW_BEAM), cs.getBitVar(Constants.POSITION_LIGHTS), cs.getBitVar(Constants.DOORS),
                                    cs.getBitVar(Constants.HAZARDS), cs.getIntVar(Constants.ODOMETER), vc, cs.getBitVar(Constants.DONGLE));
                        }
                    } else {
                        Utils.runOnUiThread(() -> {
                            if (getContext() != null && !com.otcengineering.white_app.utils.Utils.isActivityFinish(getContext())) {
                                bottomLayout.setBackgroundColor(getResources().getColor(R.color.error));
                                checkButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.check_button_error)));
                            }
                        });
                    }
                }
            }
        }, 0, 250);

        if (refreshNetworkTimer == null) {
            refreshNetworkTimer = new Timer("CarStatusNetworkTimer");
        }
        refreshNetworkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!OtcBle.getInstance().isConnected()) {
                    getVehicleDataFromServer();
                }
            }
        }, 0, 30000);

        this.timeText.setText("");

        new Thread(this::loadCached, "CarStatusThread2").start();
        if (OtcBle.getInstance().isConnected()) {
            bottomLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            checkButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.check_button)));
        } else {
            bottomLayout.setBackgroundColor(getResources().getColor(R.color.error));
            checkButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.check_button_error)));
        }
        handler = new Handler();
        runnable = () -> {
            if (getContext() != null && !com.otcengineering.white_app.utils.Utils.isActivityFinish(getContext())) {
                if (OtcBle.getInstance().isConnected()) {
                    CarStatus cs = OtcBle.getInstance().carStatus;
                    boolean vc = cs.getBitVar("EngineNotif") || cs.getBitVar("EpsNotif") || cs.getBitVar("AscNotif") || cs.getBitVar("BrakeSystemNotif") ||
                            cs.getBitVar("AbsNotif") || cs.getBitVar("OssImmoNotif") || cs.getBitVar("KosNotif") || cs.getBitVar("SrsNotif") ||
                            cs.getBitVar("AtNotif") || cs.getBitVar("OilNotif") || cs.getBitVar("ChargeNotif") ||  cs.getBitVar("BrakeFluidNotif") ||
                            cs.getBitVar("OssElecNotif") || cs.getBitVar("OssSteeNotif");
                    setValues(cs.getBitVar(Constants.KL15), cs.getBitVar("LowFuel"),
                            cs.getBitVar(Constants.HIGH_BEAM), cs.getBitVar(Constants.LOW_BEAM), cs.getBitVar(Constants.POSITION_LIGHTS), cs.getBitVar(Constants.DOORS),
                            cs.getBitVar(Constants.HAZARDS), cs.getIntVar(Constants.ODOMETER), vc, cs.getBitVar(Constants.DONGLE));
                    setTime(LocalDateTime.now(Clock.systemUTC()));
                    bottomLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    checkButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.check_button)));
                } else {
                    bottomLayout.setBackgroundColor(getResources().getColor(R.color.error));
                    checkButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.check_button_error)));
                }
                handler.postDelayed(runnable, 200);
            }
        };
        //handler.post(runnable);
    }

    private void loadCached() {
        String json = MySharedPreferences.createLogin(getContext()).getString("StatusCache");
        if (!json.isEmpty()) {
            try {
                General.VehicleStatus vs = new Gson().fromJson(json, General.VehicleStatus.class);
                if (vs != null) {
                    setValues(vs.getBit0(), vs.getBit1(), vs.getBit2(), vs.getBit3(),
                            vs.getBit4(), vs.getBit5(),vs.getBit7(), vs.getOdometer(), vs.getBit8(), vs.getBit9());
                    setTime(DateUtils.stringToDateTime(vs.getDate(), "yyyy-MM-dd HH:mm:ss"));
                }
            } catch (JsonSyntaxException jse) {
                //Log.e("CarStatusFragment", "JsonSyntaxException", jse);
            }
        }
    }

    private void getVehicleDataFromServer() {
        if (!OtcBle.getInstance().isConnected()) {
            TypedTask<General.VehicleStatus> getVehicleStatus = new TypedTask<>(Endpoints.VEHICLE_STATUS, null, true, General.VehicleStatus.class, new TypedCallback<General.VehicleStatus>() {
                @Override
                public void onSuccess(@Nonnull @NonNull General.VehicleStatus vs) {
                    final Context ctx = getContext();
                    String json = MySharedPreferences.createLogin(ctx).getString("StatusCache");
                    General.VehicleStatus cached = new Gson().fromJson(json, General.VehicleStatus.class);
                    if (cached != null) {
                        try {
                            Date serverDate = DateUtils.parseStringDate(vs.getDate());
                            Date ourDate = DateUtils.parseStringDate(cached.getDate());
                            if (ourDate == null) {
                                setValues(vs.getBit0(), vs.getBit1(), vs.getBit2(), vs.getBit3(),
                                        vs.getBit4(), vs.getBit5(),vs.getBit7(), vs.getOdometer(), vs.getBit8(), vs.getBit9());
                                setTime(DateUtils.stringToDateTime(vs.getDate(), "yyyy-MM-dd HH:mm:ss"));
                                String j = new Gson().toJson(vs);
                                MySharedPreferences.createLogin(ctx).putString("StatusCache", j);
                            } else {
                                if (serverDate.getTime() >= ourDate.getTime()) {
                                    setValues(vs.getBit0(), vs.getBit1(), vs.getBit2(), vs.getBit3(),
                                            vs.getBit4(), vs.getBit5(),vs.getBit7(), vs.getOdometer(), vs.getBit8(), vs.getBit9());
                                    setTime(DateUtils.stringToDateTime(vs.getDate(), "yyyy-MM-dd HH:mm:ss"));
                                    String j = new Gson().toJson(vs);
                                    MySharedPreferences.createLogin(ctx).putString("StatusCache", j);
                                } else {
                                    setValues(cached.getBit0(), cached.getBit1(), cached.getBit2(), cached.getBit3(),
                                            cached.getBit4(), cached.getBit5(), cached.getBit7(), cached.getOdometer(), cached.getBit8(), cached.getBit9());
                                    setTime(DateUtils.stringToDateTime(cached.getDate(), "yyyy-MM-dd HH:mm:ss"));
                                }
                            }
                        } catch (NullPointerException e) {
                            //Log.e("CarStatusFragment", "NPE", e);
                        }
                    } else {
                        setValues(vs.getBit0(), vs.getBit1(), vs.getBit2(), vs.getBit3(),
                                vs.getBit4(), vs.getBit5(),vs.getBit7(), vs.getOdometer(), vs.getBit8(), vs.getBit9());
                        setTime(DateUtils.stringToDateTime(vs.getDate(), "yyyy-MM-dd HH:mm:ss"));
                        String j = new Gson().toJson(vs);
                        MySharedPreferences.createLogin(ctx).putString("StatusCache", j);
                    }
                }

                @Override
                public void onError(@NonNull Shared.OTCStatus status, String str) {
                    loadCached();
                }
            });
            getVehicleStatus.execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_fragment_security, container, false);

        loadViews(v);
        new Thread(this::loadCached, "CarStatusThread3").start();

        return v;
    }

    private void loadViews(final View v) {
        engine = v.findViewById(R.id.Engine);
        fuel = v.findViewById(R.id.Fuel);
        hbeam = v.findViewById(R.id.HBeam);
        lbeam = v.findViewById(R.id.LBeam);
        positionLights = v.findViewById(R.id.PositionLights);
        doors = v.findViewById(R.id.Doors);
        warnings = v.findViewById(R.id.Warnings);
        odometer = v.findViewById(R.id.totalOdometer);
        vehicleCondition = v.findViewById(R.id.vehicleCondition);
        dongle = v.findViewById(R.id.dongle);

        fuelLayout = v.findViewById(R.id.fuelLayout);
        doorsLayout = v.findViewById(R.id.doorsLayout);
        vehicleConditionLayout = v.findViewById(R.id.vehicleConditionLayout);
        warningsLayout = v.findViewById(R.id.warningsLayout);
        dongleLayout = v.findViewById(R.id.dongleLayout);

        bottomLayout = v.findViewById(R.id.bottomBar);
        timeText = v.findViewById(R.id.dateBottom);
        checkButton = v.findViewById(R.id.checkButton);
        checkButton.setOnClickListener(btn -> getVehicleDataFromServer());

        statusWorking = v.findViewById(R.id.statusWorking);

        statusAlert = v.findViewById(R.id.statusAlert);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ConstraintLayout cl6 = v.findViewById(R.id.constraintLayout6);
            cl6.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

            ConstraintLayout cl = v.findViewById(R.id.constraintLayout);
            cl.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        }
    }

    // LocalDateTime in UTC
    private void setTime(final LocalDateTime date) {
        Utils.runOnUiThread(() -> {
            if (date.isAfter(LocalDateTime.now())) {
                timeText.setText(DateUtils.getLocalString("dd/MM/yyyy - HH:mm:ss"));
            } else {
                String d = DateUtils.utcToString(date, "dd/MM/yyyy - HH:mm:ss");
                timeText.setText(d);
            }
        });
    }

    private void setValues(boolean engine, boolean fuel, boolean hbeam, boolean lbeam,
                           boolean positionLight, boolean doors, boolean warnings, int odometer, boolean faultCar, boolean dongleStatus) {
        try {
            Utils.runOnUiThread(() -> {
                if (getContext() == null) {
                    return;
                }
                String on = com.otcengineering.white_app.utils.Utils.capitalizeFirst(getString(R.string.on));
                String off = com.otcengineering.white_app.utils.Utils.capitalizeFirst(getString(R.string.off));
                this.engine.setText(engine ? on : off);
                this.fuel.setText(fuel ? getString(R.string.low) : getString(R.string.high));
                this.hbeam.setText(hbeam ? on : off);
                this.lbeam.setText(lbeam ? on : off);
                this.positionLights.setText(positionLight ? on : off);
                this.doors.setText(doors ? getString(R.string.open) : getString(R.string.closed));
                this.warnings.setText(warnings ? on : off);
                this.odometer.setText(String.format(Locale.US, "%d km", odometer));
                this.vehicleCondition.setText(faultCar ? getString(R.string.error_default) : getString(R.string.ok));
                this.dongle.setText(dongleStatus ? getString(R.string.ok) : getString(R.string.error_default));

                statusWorking.setText(getStatusWorkingText(faultCar ? CarStatusText.faultCar : CarStatusText.working));

                doorsLayout.setBackgroundColor(getResources().getColor(doors ? R.color.colorPrimary : android.R.color.white));
                fuelLayout.setBackgroundColor(getResources().getColor(fuel ? R.color.colorPrimary : android.R.color.white));
                vehicleConditionLayout.setBackgroundColor(getResources().getColor(faultCar ? R.color.colorPrimary : android.R.color.white));
                warningsLayout.setBackgroundColor(getResources().getColor(warnings ? R.color.colorPrimary : android.R.color.white));
                dongleLayout.setBackgroundColor(getResources().getColor(dongleStatus ? android.R.color.white : R.color.colorPrimary));

                this.statusAlert.setVisibility(faultCar ? View.VISIBLE : View.GONE);
            });
        } catch (RuntimeException re) {
            //Log.e("CarStatusFragment", "Runtime Exception", re);
        }
    }

    @Override
    public void onNotificationReceived() {
        getVehicleDataFromServer();
    }

    enum CarStatusText {
        working, faultCar
    }

    private String getStatusWorkingText(final CarStatusText status) {
        switch (status) {
            case working:
                return getString(R.string.correctly_working);
            case faultCar:
                return getString(R.string.fault_car);
        }
        return "";
    }
}
