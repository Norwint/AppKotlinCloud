package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.General;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ConditionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context ctx;
    private ArrayList<Condition> m_conditions;
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public enum Conditions {
        Abs, AirbagSystem, AscSystem, AutoTransmission, BrakeFluid, BrakeSystem, ChargingSystem, ElectricalSystem, Engine, ImmobilizerSystem,
        KeylessOperationSystem, OilPressure, PowerSteering, Steering;

        public String getTitle() {
            switch (this) {
                case Abs: return MyApp.getContext().getString(R.string.abs_fault);
                case AirbagSystem: return MyApp.getContext().getString(R.string.airbag_fault);
                case AscSystem: return MyApp.getContext().getString(R.string.asc_fault);
                case AutoTransmission: return MyApp.getContext().getString(R.string.transmission_fault);
                case BrakeFluid: return MyApp.getContext().getString(R.string.low_brake_fluid);
                case BrakeSystem: return MyApp.getContext().getString(R.string.brake_fault);
                case ChargingSystem: return MyApp.getContext().getString(R.string.charging_fault);
                case ElectricalSystem: return MyApp.getContext().getString(R.string.electrical_fault);
                case Engine: return MyApp.getContext().getString(R.string.engine_fault);
                case ImmobilizerSystem: return MyApp.getContext().getString(R.string.immobilizer_fault);
                case KeylessOperationSystem: return MyApp.getContext().getString(R.string.keyless_fault);
                case OilPressure: return MyApp.getContext().getString(R.string.oil_pressure);
                case PowerSteering: return MyApp.getContext().getString(R.string.steering_fault);
                case Steering: return MyApp.getContext().getString(R.string.steering_lock);
                default: return "";
            }
        }

        public static Conditions forNumber(int element) {
            for (Conditions conds : values()) {
                if (conds.ordinal() == element) {
                    return conds;
                }
            }
            return null;
        }
    }

    public ConditionAdapter(final Context ctx) {
        this.ctx = ctx;
        m_conditions = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_condition, parent, false);
        return new ConditionHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            Condition cond = m_conditions.get(position);
            ConditionHolder ch = (ConditionHolder)holder;

            ch.setContent(cond);
            ch.bind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCondition(Conditions condition, String date, General.SignalMode status) {
        Condition cond = new Condition(condition, date, status);
        m_conditions.add(cond);
    }

    public static int getEnabledCondition(Conditions condition) {
        switch (condition) {
            case Abs: return R.drawable.vehicle_condition_16;
            case AirbagSystem: return R.drawable.vehicle_condition_28;
            case AscSystem: return R.drawable.vehicle_condition_43;
            case AutoTransmission: return R.drawable.vehicle_condition_40;
            case BrakeFluid: case BrakeSystem: return R.drawable.vehicle_condition_25;
            case ChargingSystem: return R.drawable.vehicle_condition_22;
            case ElectricalSystem: return R.drawable.vehicle_condition_34;
            case Engine: return R.drawable.vehicle_condition_46;
            case ImmobilizerSystem: return R.drawable.vehicle_condition_13;
            case KeylessOperationSystem: return R.drawable.vehicle_condition_31;
            case OilPressure: return R.drawable.vehicle_condition_19;
            case PowerSteering: return R.drawable.vehicle_condition_38;
            case Steering: return R.drawable.vehicle_condition_7;
        }
        return 0;
    }

    private static int getErrorCondition(Conditions condition) {
        switch (condition) {
            case Abs: return R.drawable.vehicle_condition_17;
            case AirbagSystem: return R.drawable.vehicle_condition_29;
            case AscSystem: return R.drawable.vehicle_condition_44;
            case AutoTransmission: return R.drawable.vehicle_condition_41;
            case BrakeFluid: case BrakeSystem: return R.drawable.vehicle_condition_26;
            case ChargingSystem: return R.drawable.vehicle_condition_23;
            case ElectricalSystem: return R.drawable.vehicle_condition_35;
            case Engine: return R.drawable.vehicle_condition_47;
            case ImmobilizerSystem: return R.drawable.vehicle_condition_14;
            case KeylessOperationSystem: return R.drawable.vehicle_condition_32;
            case OilPressure: return R.drawable.vehicle_condition_20;
            case PowerSteering: return R.drawable.vehicle_condition_37;
            case Steering: return R.drawable.vehicle_condition_8;
        }
        return 0;
    }

    private static int getDisabledCondition(Conditions condition) {
        switch (condition) {
            case Abs: return R.drawable.vehicle_condition_15;
            case AirbagSystem: return R.drawable.vehicle_condition_27;
            case AscSystem: return R.drawable.vehicle_condition_42;
            case AutoTransmission: return R.drawable.vehicle_condition_39;
            case BrakeFluid: case BrakeSystem: return R.drawable.vehicle_condition_24;
            case ChargingSystem: return R.drawable.vehicle_condition_21;
            case ElectricalSystem: return R.drawable.vehicle_condition_33;
            case Engine: return R.drawable.vehicle_condition_45;
            case ImmobilizerSystem: return R.drawable.vehicle_condition_12;
            case KeylessOperationSystem: return R.drawable.vehicle_condition_30;
            case OilPressure: return R.drawable.vehicle_condition_18;
            case PowerSteering: return R.drawable.vehicle_condition_36;
            case Steering: return R.drawable.vehicle_condition_6;
        }
        return 0;
    }

    public static int getResourceForSignalMode(General.SignalMode sm) {
        if (sm == null) {
            return R.drawable.vehicle_condition_3;
        }
        switch (sm) {
            case WORKING: return R.drawable.vehicle_condition_3;
            case PROBLEM: return R.drawable.vehicle_condition_5;
            case DISABLED: return R.drawable.vehicle_condition_48;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return m_conditions.size();
    }

    public void clear() {
        m_conditions.clear();
    }

    public class Condition {
        public String title, description;
        public Date timestamp;
        int iconRes;
        public General.SignalMode status;
        public Conditions condition;

        Condition(Conditions condition, String timestamp, General.SignalMode status) {
            this.condition = condition;
            this.title = condition.getTitle();

            int icon = 0;
            String description = "";
            switch (status) {
                case DISABLED:
                    icon = getDisabledCondition(condition);
                    description = ctx.getString(R.string.not_available);
                    break;
                case WORKING:
                    icon = getEnabledCondition(condition);
                    break;
                case PROBLEM:
                    icon = getErrorCondition(condition);
                    description = ctx.getString(R.string.service_required);
                    break;
                case UNRECOGNIZED:
                    break;
            }

            this.description = description;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                this.timestamp = sdf.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.iconRes = icon;
            this.status = status;
        }

        public String getDate() {
            try {
                if (status != General.SignalMode.DISABLED) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getDefault());

                    return sdf.format(timestamp);
                } else {
                    return "---";
                }
            } catch (Exception e) {
                return "---";
            }
        }
    }

    protected class ConditionHolder extends RecyclerView.ViewHolder {
        ImageView icon, status;
        public TextView title, description, date;
        public ConstraintLayout layout;

        private Condition m_condition;

        ConditionHolder(View v) {
            super(v);

            icon = v.findViewById(R.id.icon);
            status = v.findViewById(R.id.status);

            title = v.findViewById(R.id.title);
            description = v.findViewById(R.id.description);
            date = v.findViewById(R.id.date);

            layout = v.findViewById(R.id.layout);
        }

        public void setContent(Condition cond) {
            m_condition = cond;

            date.setText(cond.getDate());
            description.setText(cond.description);
            description.setVisibility(cond.description.isEmpty() ? View.GONE : View.VISIBLE);

            icon.setImageResource(cond.iconRes);
            status.setImageResource(getResourceForSignalMode(cond.status));

            if (cond.status == General.SignalMode.PROBLEM) {
                description.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite));
                date.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite));
                title.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite));
                layout.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
            } else if (cond.status == General.SignalMode.WORKING) {
                title.setTextColor(ContextCompat.getColor(ctx, R.color.colorGrayDark));
                date.setTextColor(ContextCompat.getColor(ctx, R.color.colorGrayDark));
                layout.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWhite));
            } else {
                date.setTextColor(ContextCompat.getColor(ctx, android.R.color.darker_gray));
                title.setTextColor(ContextCompat.getColor(ctx, android.R.color.darker_gray));
                layout.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorWhite));
            }
            title.setText(cond.title);
        }

        void bind() {
            layout.setOnClickListener(v -> listener.onItemClick(this.m_condition));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Condition item);
    }
}
