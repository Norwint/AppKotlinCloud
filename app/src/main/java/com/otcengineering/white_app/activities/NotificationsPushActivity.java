package com.otcengineering.white_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import com.otc.alice.api.model.ProfileAndSettings;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.MySharedPreferences;

public class NotificationsPushActivity extends BaseActivity {

    private TitleBar titleBar;
    private ScrollView scrollView;
    private FrameLayout btnScrollUp;
    private Switch swInvitations, swUserPost, swDatsunPost,
            swDatsunMessage, swDealerPost, swDealerMessage, swGeofencing;
    private Button btnSave;

    public NotificationsPushActivity() {
        super("NotificationsPushActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_push);
        retrieveViews();
        setEvents();
        swGeofencing.setChecked(false);
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.notifications_push_titleBar);
        scrollView = findViewById(R.id.notifications_push_scrollView);
        btnScrollUp = findViewById(R.id.notifications_push_btnScrollUp);
        swInvitations = findViewById(R.id.swInvitations);
        swUserPost = findViewById(R.id.swUserPost);
        swDatsunPost = findViewById(R.id.swDatsunPost);
        swDatsunMessage = findViewById(R.id.swDatsunMessage);
        swDealerPost = findViewById(R.id.swDealerPost);
        swDealerMessage = findViewById(R.id.swDealerMessage);
        swGeofencing = findViewById(R.id.swGeofencing);
        btnSave = findViewById(R.id.notifications_push_btnSave);
    }

    private void setEvents() {
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

        //btnSave.setOnClickListener(view -> saveSettings());
        btnSave.setVisibility(View.GONE);
        btnScrollUp.setOnClickListener(view -> scrollView.smoothScrollTo(0, 0));

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            btnScrollUp.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void saveSettings() {
        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

        int value = (swInvitations.isChecked() ? 1 : 0) | (swUserPost.isChecked() ? 0x2 : 0) | (swDatsunPost.isChecked() ? 0x4 : 0)
                | (swDatsunMessage.isChecked() ? 0x8 : 0) | (swDealerPost.isChecked() ? 0x10 : 0) | (swDatsunMessage.isChecked() ? 0x20 : 0) | (swGeofencing.isChecked() ? 0x40 : 0);
        msp.putInteger("PushNotificationsSettings", value);

        ProfileAndSettings.NotificationsStatus status = ProfileAndSettings.NotificationsStatus.newBuilder()
                .setNewFriendRequest(swInvitations.isChecked())
                .setNewFriendPost(swUserPost.isChecked())
                .setNewConnectTechPost(swDatsunPost.isChecked())
                .setNewConnectTechMessage(swDatsunMessage.isChecked())
                .setNewDealerPost(swDealerPost.isChecked())
                .setNewDealerMessage(swDealerMessage.isChecked())
                .setGeofencing(swGeofencing.isChecked())
                .build();
        GenericTask gt = new GenericTask(Endpoints.NOTIFICATIONS_UPDATE, status, true, (cn) -> {});
        gt.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        if (!msp.contains("PushNotificationsSettings")) {
            msp.putInteger("PushNotificationsSettings", 0x80-1);
        }
        int value = msp.getInteger("PushNotificationsSettings");

        swInvitations.setChecked(isBitSet(value, 0x1));
        swUserPost.setChecked(isBitSet(value, 0x2));
        swDatsunPost.setChecked(isBitSet(value, 0x4));
        swDatsunMessage.setChecked(isBitSet(value, 0x8));
        swDealerPost.setChecked(isBitSet(value, 0x10));
        swDealerMessage.setChecked(isBitSet(value, 0x20));
        swGeofencing.setChecked(isBitSet(value, 0x40));
    }

    private boolean isBitSet(int value, int bit) {
        return (value & bit) == bit;
    }

    @Override
    public void onBackPressed() {
        saveSettings();
        super.onBackPressed();
    }
}
