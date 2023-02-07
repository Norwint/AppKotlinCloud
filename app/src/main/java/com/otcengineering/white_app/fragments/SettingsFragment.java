package com.otcengineering.white_app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.ConfigurationNewEra;
import com.otc.alice.api.model.Connector;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.Home2Activity;
import com.otcengineering.white_app.activities.LanguageActivity;
import com.otcengineering.white_app.activities.LicenseActivity;
import com.otcengineering.white_app.activities.NotificationsPushActivity;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;

import java.io.IOException;


public class SettingsFragment extends EventFragment {

    private LinearLayout viewSmart, viewNotifi, viewLang, layout_license, layout_developer;
    private ConstraintLayout viewUpdates;
    private View notificationsFrame;
    private Switch rankings, rewards, swRecentTrip, swAutoUpdate;
    private TextView parti1, parti2, txtSmart, txtNotifications, txtLanguage, txtRankings, txtBarrita, txtReward, txtBarrita2;

    public SettingsFragment() {
        super("SettingsActivity");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        retrieveViews(v);
        setTypefaces();
        setEvents();
        setVersionNumber(v);
        return v;
    }

    private void retrieveViews(View v) {
        viewSmart = v.findViewById(R.id.viewSmart);
        viewNotifi = v.findViewById(R.id.viewNotifications);
        viewLang = v.findViewById(R.id.viewLanguage);
        rankings = v.findViewById(R.id.swRankings);
        rewards = v.findViewById(R.id.swRewards);
        parti1 = v.findViewById(R.id.txtParticipate1);
        parti2 = v.findViewById(R.id.txtParticipate2);
        txtSmart = v.findViewById(R.id.txtSmart);
        txtNotifications = v.findViewById(R.id.txtNotifications);
        txtLanguage = v.findViewById(R.id.txtLanguage);
        txtRankings = v.findViewById(R.id.txtrankings);
        txtBarrita = v.findViewById(R.id.txtBarrita);
        txtReward = v.findViewById(R.id.txtRewards);
        txtBarrita2 = v.findViewById(R.id.txtBarrita2);
        viewUpdates = v.findViewById(R.id.viewUpdates);
        swAutoUpdate = v.findViewById(R.id.swAutoUpdate);
        swRecentTrip = v.findViewById(R.id.swRecentTrip);
        notificationsFrame = v.findViewById(R.id.notificationsFrame);
        layout_license = v.findViewById(R.id.layout_license);
        layout_developer = v.findViewById(R.id.layout_developer);

        swRecentTrip.setEnabled(!MySharedPreferences.createLogin(getContext()).getBoolean("Expired"));
    }

    private void isNewVersion() {
        GenericTask gt2 = new GenericTask(Endpoints.VERSION, null, true, (rsp) -> {
            if (rsp != null) {
                ConfigurationNewEra.VersionResponse resp = null;
                try {
                    resp = ConfigurationNewEra.VersionResponse.parseFrom(rsp.getData().getValue());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                if (resp != null) {
                    String[] parts = BuildConfig.VERSION_NAME.split("\\.");
                    int major = Integer.parseInt(parts[0]);
                    int minor = Integer.parseInt(parts[1]);
                    int build = Integer.parseInt(parts[2]);

                    boolean isLess = major < resp.getAndroidMajor() ||
                            ((major == resp.getAndroidMajor()) && (minor < resp.getAndroidMinor())) ||
                            ((major == resp.getAndroidMajor()) && (minor == resp.getAndroidMinor()) && (build < resp.getAndroidBuild()));
                    if (isLess) {
                        notificationsFrame.setVisibility(View.VISIBLE);
                        DialogYesNo cd = new DialogYesNo(getContext(), getString(R.string.new_app_update), () -> {}, () -> {
                            Context ctx = getContext();
                            if (ctx != null) {
                                String url = String.format("market://details?id=%s", ctx.getPackageName());
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        });
                        cd.setYesButtonText(getString(R.string.later));
                        cd.setNoButtonText(getString(R.string.update));
                        cd.show();
                        MySharedPreferences.createLogin(getContext()).putBoolean("LastVersion", false);
                    } else {
                        notificationsFrame.setVisibility(View.GONE);
                        CustomDialog cd = new CustomDialog(getContext(), getString(R.string.you_app_is_updated), false);
                        cd.show();
                        MySharedPreferences.createLogin(getContext()).putBoolean("LastVersion", true);
                    }
                }
            }
        });
        gt2.execute();
    }

    private void setTypefaces() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");

        parti1.setTypeface(face);
        parti2.setTypeface(face);
        txtSmart.setTypeface(face);
        txtNotifications.setTypeface(face);
        txtLanguage.setTypeface(face);
        txtRankings.setTypeface(face);
        txtBarrita.setTypeface(face);
        txtReward.setTypeface(face);
        txtBarrita2.setTypeface(face);
    }

    private void setEvents() {
        viewSmart.setOnClickListener(v -> {
            try {
                Activity activity = getActivity();
                if (activity != null) {
                    String packageName = activity.getPackageName();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", packageName, null));
                    startActivityForResult(intent, 0);
                }
            } catch (Exception ignored) {

            }
        });

        viewNotifi.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationsPushActivity.class);
            startActivity(intent);
        });

        viewLang.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LanguageActivity.class);
            startActivity(intent);
        });

        rankings.setOnClickListener(v -> {
            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_RANKINGS, rankings.isChecked(), getContext());
            manageRankingsState();
            updateRankingsAndBadges();
        });

        rewards.setOnClickListener(v -> {
            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_REWARDS, rewards.isChecked(), getContext());
            manageRewardsState();
            updateRankingsAndBadges();
        });

        swRecentTrip.setOnClickListener(v -> {
            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_RECENT_TRIP, swRecentTrip.isChecked(), getContext());
            updateRankingsAndBadges();
        });

        swAutoUpdate.setOnClickListener(v -> {
            PrefsManager.getInstance().saveSettingValue(Constants.Prefs.SETTINGS_AUTOUPDATE, swAutoUpdate.isChecked(), getContext());
            updateRankingsAndBadges();
        });

        viewUpdates.setOnClickListener(v -> isNewVersion());

        notificationsFrame.setVisibility(MySharedPreferences.createLogin(getContext()).getBoolean("LastVersion") ? View.GONE : View.VISIBLE);

        layout_license.setOnClickListener(v -> startActivity(new Intent(getContext(), LicenseActivity.class)));
    }

    private void updateRankingsAndBadges() {
        new Thread(() -> {
            ProfileAndSettings.SocialNetworkStatus sns = ProfileAndSettings.SocialNetworkStatus.newBuilder()
                    .setRankingEnabled(rankings.isChecked())
                    .setBadgeEnabled(rewards.isChecked())
                    .setSaveRecentTripEnabled(swRecentTrip.isChecked())
                    .setAutoUpdateDongleEnabled(swAutoUpdate.isChecked())
                    .build();
            try {
                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.SOCIAL_NETWORK_UPDATE, true, sns, Shared.OTCResponse.class);
                Log.d("Rankings", response.getStatus().name());
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
            }
            Utils.setDongleEnableRouteStorage(swRecentTrip.isChecked());
        }, "SettingsThread").start();
    }

    private void manageRankingsState() {
        if (!rankings.isChecked()) {
            parti1.setText(R.string.no_participate);
        } else {
            parti1.setText(R.string.participate);
        }
    }

    private void manageRewardsState() {
        if (!rewards.isChecked()) {
            parti2.setText(R.string.no_participate);
        } else {
            parti2.setText(R.string.participate);
        }
    }
    private ProgressDialog pd;

    private void setVersionNumber(View v) {
        TextView txtVersion = v.findViewById(R.id.settings_txtVersion);
        txtVersion.setText(String.format("%s V%s", getString(R.string.app_name), BuildConfig.VERSION_NAME));

        if (Utils.developer) {
            TextView tv = new TextView(getContext());
            tv.setText(String.format("Server version: %s", MySharedPreferences.createLogin(getContext()).getString("ServerVersion")));
            layout_developer.addView(tv);

            try {
                TextView tv1 = new TextView(getContext());
                int fw = OtcBle.getInstance().carStatus.getRawData().get("S12_FW").getIntValue();
                tv1.setText(String.format("Dongle version: %d.%d", fw & 0xFF, (fw >> 8)));
                layout_developer.addView(tv1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Button removeDongleBtn = new Button(getContext());
            removeDongleBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_invite_background));
            removeDongleBtn.setOnClickListener(x -> removeDongle());
            removeDongleBtn.setText("Unregister dongle");
            layout_developer.addView(removeDongleBtn);

            Button registerDongleBtn = new Button(getContext());
            registerDongleBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_invite_background));
            registerDongleBtn.setOnClickListener(x -> new Thread(() -> OtcBle.getInstance().register()).start());
            registerDongleBtn.setText("Register dongle");
            layout_developer.addView(registerDongleBtn);

            Button disableDeveloperBtn = new Button(getContext());
            disableDeveloperBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_invite_background));
            disableDeveloperBtn.setOnClickListener(x -> Utils.developer = false);
            disableDeveloperBtn.setText("Disable developer");
            layout_developer.addView(disableDeveloperBtn);
        }
    }

    private void removeDongle() {
        new Thread(() -> {
            Utils.runOnMainThread(() -> {
                pd = new ProgressDialog(getContext());
                pd.setTitle("Unregistering Dongle...");
                pd.setMessage("Please wait...");
                pd.setCancelable(false);
                pd.show();
            });
            tries:
            for (int i = 0; i < 5; ++i) {
                if (!OtcBle.getInstance().isConnected()) {
                    OtcBle.getInstance().connect();
                }
                long timeout = System.currentTimeMillis() + 15000;
                while (!OtcBle.getInstance().isConnected()) {
                    com.otcengineering.apible.Utils.wait(this, 100);
                    if (System.currentTimeMillis() >= timeout) {
                        continue tries;
                    }
                }
                byte[] b_regproc_r = OtcBle.getInstance().readLongTag("REGPROC_R", true);
                if (b_regproc_r == null) {
                    OtcBle.getInstance().disconnect();
                    continue;
                }
                int regproc_r = ((b_regproc_r[0] & 255) << 8) | (b_regproc_r[1] & 255);
                if (regproc_r >= 0x100) {
                    OtcBle.getInstance().writeLongTag("REGPROC_W", new byte[]{(byte)0xCD, 0}, false);
                }
                Connector.DongleUnregister du = Connector.DongleUnregister.newBuilder()
                        .setSerialNumber(MySharedPreferences.createLogin(getContext()).getString("SN"))
                        .setUserName(MySharedPreferences.createLogin(getContext()).getString("Nick")).build();
                try {
                    Shared.OTCResponse resp  = ApiCaller.doCall(Endpoints.UNREGISTER_DONGLE, true, du, Shared.OTCResponse.class);
                    if (resp != null && resp.getStatus() == Shared.OTCStatus.SUCCESS) {
                        Utils.runOnMainThread(() -> pd.dismiss());
                        try {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Context ctx = getContext();
                        if (ctx == null) {
                            ctx = MyApp.getContext();
                        }
                        Context finalCtx = ctx;
                        Utils.runOnMainThread(() -> Utils.logout(finalCtx));
                        return;
                    } else {
                        Utils.runOnMainThread(() -> pd.dismiss());
                        Utils.runOnMainThread(() -> new AlertDialog.Builder(getContext())
                                .setTitle("Unregistering dongle error.")
                                .setMessage("Please, contact with the Call Center")
                                .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss()).show());
                        return;
                    }
                } catch (ApiCaller.OTCException e) {
                    e.printStackTrace();
                }
            }
            Utils.runOnMainThread(() -> pd.dismiss());
            Utils.runOnMainThread(() -> new AlertDialog.Builder(MyApp.getContext())
                    .setTitle("Unregistering dongle error.")
                    .setMessage("Please, contact with the Call Center")
                    .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss()).show());
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        txtNotifications.setText(R.string.notifications_push);
        txtLanguage.setText(R.string.language);
        txtRankings.setText(R.string.rankings);
        txtReward.setText(R.string.rewards);

        Home2Activity act = ((Home2Activity)this.getActivity());
        if (act != null) {
            act.configureTitleBar(R.string.title_settings);
        }

        boolean rankingsChecked = PrefsManager.getInstance().getSettingValue(Constants.Prefs.SETTINGS_RANKINGS, getContext());
        rankings.setChecked(rankingsChecked);
        manageRankingsState();

        boolean rewardChecked = PrefsManager.getInstance().getSettingValue(Constants.Prefs.SETTINGS_REWARDS, getContext());
        rewards.setChecked(rewardChecked);
        manageRewardsState();

        boolean saveRecentTrip = PrefsManager.getInstance().getSettingValue(Constants.Prefs.SETTINGS_RECENT_TRIP, getContext());
        swRecentTrip.setChecked(saveRecentTrip);

        boolean autoupdateChecked = PrefsManager.getInstance().getSettingValue(Constants.Prefs.SETTINGS_AUTOUPDATE, getContext());
        swAutoUpdate.setChecked(autoupdateChecked);
    }
}
