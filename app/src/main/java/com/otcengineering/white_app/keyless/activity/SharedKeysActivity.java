package com.otcengineering.white_app.keyless.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.serialization.BookingInfo;
import com.otcengineering.white_app.serialization.BookingKeys;
import com.otcengineering.white_app.serialization.SharedKey;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalTime;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SharedKeysActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private ImageButton mSendKey;
    private Long mSharedKeyId = null;
    private SharedKey mKey;

    public SharedKeysActivity() {
        super("SharedKeysActivity>");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_keys);

        getViews();
        setEvents();
        getListForToday();
    }

    // ;)
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
                            Toast.makeText(SharedKeysActivity.this, key.getCode() + " " + key.getExpirationDate(), Toast.LENGTH_LONG).show();
                            SharedKey.addSharedKey(id, mKey);
                            if (SharedKey.count(id) == 10) {
                                mKey = SharedKey.getKey(id);
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


    private void setEvents() {
        mTitleBar.setListener(new TitleBar.TitleBarListener() {
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

        mSendKey.setOnClickListener(v -> {
            if (mKey != null) {
                Toast.makeText(this, "Jorl!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getViews() {
        mTitleBar = findViewById(R.id.titleBar);
        mSendKey = findViewById(R.id.button9);
    }
}
