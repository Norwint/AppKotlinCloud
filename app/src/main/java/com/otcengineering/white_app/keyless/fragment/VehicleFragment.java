package com.otcengineering.white_app.keyless.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.fragments.BaseFragment;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.keyless.activity.NFCActivity;
import com.otcengineering.white_app.keyless.activity.SharedKeysActivity;
import com.otcengineering.white_app.keyless.activity.VehicleSchedulerActivity;
import com.otcengineering.white_app.keyless.activity.VehicleSchedulerManagerActivity;
import com.otcengineering.white_app.serialization.BookingInfo;
import com.otcengineering.white_app.serialization.BookingKeys;
import com.otcengineering.white_app.serialization.SharedKey;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalTime;

import java.util.ArrayList;

public class VehicleFragment extends BaseFragment {
    private Button mSharedKeys;
    private ImageButton mVehicleScheduler, mSendKey, mManageSchedules;
    private Long mSharedKeyId = null;
    private SharedKey mKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_vehicle, container, false);

        getViews(view);
        setEvents();
        getListForToday();

        return view;
    }

    private void setEvents() {
        mVehicleScheduler.setOnClickListener(v -> startActivity(new Intent(getContext(), VehicleSchedulerActivity.class)));
        mManageSchedules.setOnClickListener(v -> startActivity(new Intent(getContext(), VehicleSchedulerManagerActivity.class)));
        mSharedKeys.setOnClickListener(v -> startActivity(new Intent(getContext(), SharedKeysActivity.class)));
        mSendKey.setOnClickListener(v -> startActivity(new Intent(getContext(), NFCActivity.class)));
    }

    private void getViews(View view) {
        mVehicleScheduler = view.findViewById(R.id.button7);
        mManageSchedules = view.findViewById(R.id.button8);
        mSharedKeys = view.findViewById(R.id.button10);
        mSendKey = view.findViewById(R.id.button9);
    }

    private void getListForToday() {
        String date = DateUtils.getUtcString("yyyy-MM-dd");
        NetTask nt = new NetTask("v2/user/booking-list/" + date, true, new NetworkCallback<NetTask.JsonResponse>() {
            @Override
            public void onSuccess(NetTask.JsonResponse response) {
                int keys = 10;
                ArrayList<BookingInfo> list = response.getResponse(new TypeToken<ArrayList<BookingInfo>>(){}.getType());
                if (list != null && list.size() > 0) {
                    LocalTime lt = LocalTime.now();
                    int time = lt.getHour() * 100 + lt.getMinute();
                    for (BookingInfo bi : list) {
                        if (bi.getStartDateInMilitarFormat() <= time && bi.getEndDateInMilitarFormat() >= time) {
                            int length = SharedKey.count(bi.getId());
                            if (length < 10) {
                                getKeys(bi.getId(), keys - length);
                            } else {
                                mKey = SharedKey.getKey(bi.getId());
                            }
                            return;
                        }
                    }
                }
                setButtonState();
            }

            @Override
            public void onFailure(int code, String errorMsg) {
                showCustomDialogError(errorMsg);
                setButtonState();
            }
        });
        nt.execute();
    }

    private void setButtonState() {
        mSendKey.setImageTintList(ColorStateList.valueOf(mSharedKeyId != null ? Color.argb(255, 51, 255, 51) : Color.argb(255, 255, 51, 51)));
    }

    private void getKeys(long id, int no) {
        NetTask nt1 = new NetTask("v2/booking/shared-key/" + id + "/" + no, NetTask.JsonRequest.create(1), true, "POST", new NetworkCallback<NetTask.JsonResponse>() {
            @Override
            public void onSuccess(NetTask.JsonResponse response) {
                // HUE HUE HUE HUE HUE
                BookingKeys keys = response.getResponse(BookingKeys.class);

                for (BookingKeys.BookingSharedKey key : keys.getBookingKeys()) {
                    NetTask getSharedKeyInfo = new NetTask("v2/shared-key/" + key.getId(), true, new NetworkCallback<NetTask.JsonResponse>() {
                        @Override
                        public void onSuccess(NetTask.JsonResponse response) {
                            SharedKey key = response.getResponse(SharedKey.class);
                            Toast.makeText(getContext(), key.getCode() + " " + key.getExpirationDate(), Toast.LENGTH_LONG).show();
                            SharedKey.addSharedKey(id, mKey);
                            if (SharedKey.count(id) == 10) {
                                mKey = SharedKey.getKey(id);
                                mSharedKeyId = id;
                                setButtonState();
                            }
                        }

                        @Override
                        public void onFailure(int code, String errorMsg) {
                            showCustomDialogError(errorMsg);
                        }
                    });
                    getSharedKeyInfo.execute();
                }
                setButtonState();
            }

            @Override
            public void onFailure(int code, String errorMsg) {
                showCustomDialogError(errorMsg);
                setButtonState();
            }
        });
        nt1.execute();
    }
}
