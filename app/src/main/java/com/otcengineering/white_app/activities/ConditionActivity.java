package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.DashboardAndStatus;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.apible.Utils;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.ConditionAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.interfaces.INotificable;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

public class ConditionActivity extends BaseActivity implements INotificable {

    private TitleBar titleBar;
    private ConditionAdapter adapter;
    private DashboardAndStatus.VehicleConditionDescription vehCondDesc;

    private Timer m_bleTimer, m_netTimer;

    public ConditionActivity() {
        super("ConditionActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition);

        retrieveViews();
        setEvents();
        loadDescriptions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_bleTimer = new Timer("ConditionTimer");
        m_bleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (OtcBle.getInstance().isConnected()) {
                    String json = MySharedPreferences.createDashboard(ConditionActivity.this).getString("ConditionCache");
                    DashboardAndStatus.VehicleCondition vc = new Gson().fromJson(json, DashboardAndStatus.VehicleCondition.class);
                    if (vc != null) {
                        runOnUiThread(() -> {
                            adapter.clear();

                            adapter.addCondition(ConditionAdapter.Conditions.ImmobilizerSystem, vc.getDateImmobilizer(), getSignal("OssImmo"));
                            adapter.addCondition(ConditionAdapter.Conditions.ElectricalSystem, vc.getDateElectric(), getSignal("OssElec"));
                            adapter.addCondition(ConditionAdapter.Conditions.KeylessOperationSystem, vc.getDateKeyless(), getSignal("Kos"));
                            adapter.addCondition(ConditionAdapter.Conditions.AirbagSystem, vc.getDateAirbag(), getSignal("Srs"));
                            adapter.addCondition(ConditionAdapter.Conditions.BrakeSystem, vc.getDateBrakeSystem(), getSignal("BrakeSystem"));
                            adapter.addCondition(ConditionAdapter.Conditions.BrakeFluid, vc.getDateBrakeFluid(), getSignal("BrakeFluid"));
                            adapter.addCondition(ConditionAdapter.Conditions.ChargingSystem, vc.getDateCharger(), getSignal("Charge"));
                            adapter.addCondition(ConditionAdapter.Conditions.OilPressure, vc.getDateOilPressure(), getSignal("Oil"));
                            adapter.addCondition(ConditionAdapter.Conditions.Steering, vc.getDateSteering(), getSignal("OssStee"));
                            adapter.addCondition(ConditionAdapter.Conditions.PowerSteering, vc.getDatePowerSteering(), getSignal("Eps"));
                            adapter.addCondition(ConditionAdapter.Conditions.AutoTransmission, vc.getDateTransmission(), getSignal("At"));
                            adapter.addCondition(ConditionAdapter.Conditions.AscSystem, vc.getDateASC(), getSignal("Asc"));
                            adapter.addCondition(ConditionAdapter.Conditions.Abs, vc.getDateABS(), getSignal("Abs"));
                            adapter.addCondition(ConditionAdapter.Conditions.Engine, vc.getDateMIL(), getSignal("Engine"));

                            adapter.notifyDataSetChanged();
                        });
                    } else {
                        runOnUiThread(() -> {
                            adapter.clear();

                            adapter.addCondition(ConditionAdapter.Conditions.ImmobilizerSystem, "", getSignal("OssImmo"));
                            adapter.addCondition(ConditionAdapter.Conditions.ElectricalSystem, "", getSignal("OssElec"));
                            adapter.addCondition(ConditionAdapter.Conditions.KeylessOperationSystem, "", getSignal("Kos"));
                            adapter.addCondition(ConditionAdapter.Conditions.AirbagSystem, "", getSignal("Srs"));
                            adapter.addCondition(ConditionAdapter.Conditions.BrakeSystem, "", getSignal("BrakeSystem"));
                            adapter.addCondition(ConditionAdapter.Conditions.BrakeFluid, "", getSignal("BrakeFluid"));
                            adapter.addCondition(ConditionAdapter.Conditions.ChargingSystem, "", getSignal("Charge"));
                            adapter.addCondition(ConditionAdapter.Conditions.OilPressure, "", getSignal("Oil"));
                            adapter.addCondition(ConditionAdapter.Conditions.Steering, "", getSignal("OssStee"));
                            adapter.addCondition(ConditionAdapter.Conditions.PowerSteering, "", getSignal("Eps"));
                            adapter.addCondition(ConditionAdapter.Conditions.AutoTransmission, "", getSignal("At"));
                            adapter.addCondition(ConditionAdapter.Conditions.AscSystem, "", getSignal("Asc"));
                            adapter.addCondition(ConditionAdapter.Conditions.Abs, "", getSignal("Abs"));
                            adapter.addCondition(ConditionAdapter.Conditions.Engine, "", getSignal("Engine"));

                            adapter.notifyDataSetChanged();
                        });
                    }
                }
            }
        }, 0, 1000);

        m_netTimer = new Timer("ConditionNetworkTimer");
        m_netTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                networkUpdate();
            }
        }, 0, 30000);
    }

    private void networkUpdate() {
        GenericTask gt = new GenericTask(Endpoints.VEHICLE_CONDITION, null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                DashboardAndStatus.VehicleCondition vc = otcResponse.getData().unpack(DashboardAndStatus.VehicleCondition.class);
                MySharedPreferences.createDashboard(ConditionActivity.this).putString("ConditionCache", new Gson().toJson(vc));
                if (!OtcBle.getInstance().isConnected()) {
                    runOnUiThread(() -> {
                        adapter.clear();

                        adapter.addCondition(ConditionAdapter.Conditions.ImmobilizerSystem, vc.getDateImmobilizer(), vc.getStatusImmobilizer());
                        adapter.addCondition(ConditionAdapter.Conditions.ElectricalSystem, vc.getDateElectric(), vc.getStatusElectric());
                        adapter.addCondition(ConditionAdapter.Conditions.KeylessOperationSystem, vc.getDateKeyless(), vc.getStatusKeyless());
                        adapter.addCondition(ConditionAdapter.Conditions.AirbagSystem, vc.getDateAirbag(), vc.getStatusAirbag());
                        adapter.addCondition(ConditionAdapter.Conditions.BrakeSystem, vc.getDateBrakeSystem(), vc.getStatusBrakeSystem());
                        adapter.addCondition(ConditionAdapter.Conditions.BrakeFluid, vc.getDateBrakeFluid(), vc.getStatusBrakeFluid());
                        adapter.addCondition(ConditionAdapter.Conditions.ChargingSystem, vc.getDateCharger(), vc.getStatusCharger());
                        adapter.addCondition(ConditionAdapter.Conditions.OilPressure, vc.getDateOilPressure(), vc.getStatusOilPressure());
                        adapter.addCondition(ConditionAdapter.Conditions.Steering, vc.getDateSteering(), vc.getStatusSteering());
                        adapter.addCondition(ConditionAdapter.Conditions.PowerSteering, vc.getDatePowerSteering(), vc.getStatusPowerSteering());
                        adapter.addCondition(ConditionAdapter.Conditions.AutoTransmission, vc.getDateTransmission(), vc.getStatusTransmission());
                        adapter.addCondition(ConditionAdapter.Conditions.AscSystem, vc.getDateASC(), vc.getStatusASC());
                        adapter.addCondition(ConditionAdapter.Conditions.Abs, vc.getDateABS(), vc.getStatusABS());
                        adapter.addCondition(ConditionAdapter.Conditions.Engine, vc.getDateMIL(), vc.getStatusMIL());

                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
        gt.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        m_netTimer.cancel();
        m_bleTimer.cancel();
    }

    private General.SignalMode getSignal(String variableName) {
        try {
            Boolean err = OtcBle.getInstance().carStatus.getBitVar(String.format("%sNotif", variableName));
            Boolean enable = OtcBle.getInstance().carStatus.getBitVar(String.format("%sEnable", variableName));

            return !enable ? General.SignalMode.DISABLED : err ? General.SignalMode.PROBLEM : General.SignalMode.WORKING;
        } catch (Exception e) {
            return General.SignalMode.UNRECOGNIZED;
        }
    }

    private void loadDescriptions() {
        byte[] bytes = MySharedPreferences.createDashboard(this).getBytes(Constants.Prefs.VEHICLE_CONDICION_DESCRIPTION);
        if (bytes != null) {
            try {
                vehCondDesc = DashboardAndStatus.VehicleConditionDescription.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        } else {
            TypedTask<DashboardAndStatus.VehicleConditionDescription> getDescription = new TypedTask<>(Endpoints.VEHICLE_CONDITION_DESCRIPTION, null,
                    true, DashboardAndStatus.VehicleConditionDescription.class, new TypedCallback<DashboardAndStatus.VehicleConditionDescription>() {
                @Override
                public void onSuccess(@Nonnull DashboardAndStatus.VehicleConditionDescription value) {
                    vehCondDesc = value;
                }

                @Override
                public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                    Utils.errorLog("ConditionActivity", status.name());
                }
            });
            getDescription.execute();
        }
    }

    private void setEvents() {
        if (titleBar != null) {
            titleBar.setListener(new TitleBar.TitleBarListener() {
                @Override
                public void onLeftClick() {
                    onBackPressed();
                }

                @Override
                public void onRight1Click() {

                }

                @Override
                public void onRight2Click() {

                }
            });
        }
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.condition_titleBar);
        RecyclerView conditionTableView = findViewById(R.id.conditionTableView);
        adapter = new ConditionAdapter(this);
        adapter.setOnItemClickListener(item -> {
            String desc = "";
            switch (item.condition) {
                case Abs: desc = vehCondDesc.getABSFaultDescription(); break;
                case AirbagSystem: desc = vehCondDesc.getAirbagFaultDescription(); break;
                case AscSystem: desc = vehCondDesc.getASCFaultDescription(); break;
                case AutoTransmission: desc = vehCondDesc.getTransmissionFaultDescription(); break;
                case BrakeFluid: desc = vehCondDesc.getBrakeFluidDescription(); break;
                case BrakeSystem: desc = vehCondDesc.getBrakeSystemFaultDescription(); break;
                case ChargingSystem: desc = vehCondDesc.getChargerFaultDescription(); break;
                case ElectricalSystem: desc = vehCondDesc.getElectricFaultDescription(); break;
                case Engine: desc = vehCondDesc.getEngineFaultDescription(); break;
                case ImmobilizerSystem: desc = vehCondDesc.getImmobilizerFaultDescription(); break;
                case KeylessOperationSystem: desc = vehCondDesc.getKeylessFaultDescription(); break;
                case OilPressure: desc = vehCondDesc.getOilPressureDescription(); break;
                case PowerSteering: desc = vehCondDesc.getPowerSteeringFaultDescription(); break;
                case Steering: desc = vehCondDesc.getSteeringFaultDescription(); break;
            }

            Intent intent = new Intent(this, ConditionDescriptionActivity.class);
            intent.putExtra("Item", item.condition.ordinal());
            intent.putExtra("Desc", desc);
            intent.putExtra("State", item.status.ordinal());
            intent.putExtra("Date", item.getDate());

            startActivity(intent);
        });
        conditionTableView.setAdapter(adapter);
        conditionTableView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = getHeight() / 14;
                return true;
            }
        });
        setCacheCondition();
    }

    @UiThread
    private void setCacheCondition() {
        String json = MySharedPreferences.createDashboard(ConditionActivity.this).getString("ConditionCache");
        DashboardAndStatus.VehicleCondition vc = new Gson().fromJson(json, DashboardAndStatus.VehicleCondition.class);

        if (vc != null) {
            adapter.clear();

            adapter.addCondition(ConditionAdapter.Conditions.ImmobilizerSystem, vc.getDateImmobilizer(), vc.getStatusImmobilizer());
            adapter.addCondition(ConditionAdapter.Conditions.ElectricalSystem, vc.getDateElectric(), vc.getStatusElectric());
            adapter.addCondition(ConditionAdapter.Conditions.KeylessOperationSystem, vc.getDateKeyless(), vc.getStatusKeyless());
            adapter.addCondition(ConditionAdapter.Conditions.AirbagSystem, vc.getDateAirbag(), vc.getStatusAirbag());
            adapter.addCondition(ConditionAdapter.Conditions.BrakeSystem, vc.getDateBrakeSystem(), vc.getStatusBrakeSystem());
            adapter.addCondition(ConditionAdapter.Conditions.BrakeFluid, vc.getDateBrakeFluid(), vc.getStatusBrakeFluid());
            adapter.addCondition(ConditionAdapter.Conditions.ChargingSystem, vc.getDateCharger(), vc.getStatusCharger());
            adapter.addCondition(ConditionAdapter.Conditions.OilPressure, vc.getDateOilPressure(), vc.getStatusOilPressure());
            adapter.addCondition(ConditionAdapter.Conditions.Steering, vc.getDateSteering(), vc.getStatusSteering());
            adapter.addCondition(ConditionAdapter.Conditions.PowerSteering, vc.getDatePowerSteering(), vc.getStatusPowerSteering());
            adapter.addCondition(ConditionAdapter.Conditions.AutoTransmission, vc.getDateTransmission(), vc.getStatusTransmission());
            adapter.addCondition(ConditionAdapter.Conditions.AscSystem, vc.getDateASC(), vc.getStatusASC());
            adapter.addCondition(ConditionAdapter.Conditions.Abs, vc.getDateABS(), vc.getStatusABS());
            adapter.addCondition(ConditionAdapter.Conditions.Engine, vc.getDateMIL(), vc.getStatusMIL());

            adapter.notifyDataSetChanged();
        } else {
            adapter.clear();

            adapter.addCondition(ConditionAdapter.Conditions.ImmobilizerSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.ElectricalSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.KeylessOperationSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.AirbagSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.BrakeSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.BrakeFluid, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.ChargingSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.OilPressure, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.Steering, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.PowerSteering, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.AutoTransmission, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.AscSystem, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.Abs, "---", General.SignalMode.WORKING);
            adapter.addCondition(ConditionAdapter.Conditions.Engine, "---", General.SignalMode.WORKING);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNotificationReceived() {
        networkUpdate();
    }
}
