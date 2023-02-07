package com.otcengineering.white_app.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;

public class PrivacyProfileActivity extends BaseActivity {

    private TitleBar titleBar;
    private LinearLayout vAll, vFriends, vNone;
    private ImageView imgAll, imgFriends, imgNone;
    private TextView txtAll, txtFriends, txtNone;

    public PrivacyProfileActivity() {
        super("PrivacyProfileActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_profile);
        retrieveViews();
        setTypeface();
        setEvents();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.privacy_profile_titleBar);
        vAll = findViewById(R.id.viewAll);
        vFriends = findViewById(R.id.viewFriends);
        vNone = findViewById(R.id.viewNone);
        imgAll = findViewById(R.id.imagAll);
        imgFriends = findViewById(R.id.imagFriends);
        imgNone = findViewById(R.id.imagNone);
        txtAll = findViewById(R.id.txtAll);
        txtFriends = findViewById(R.id.txtFriends);
        txtNone = findViewById(R.id.txtNone);
    }

    private void setTypeface() {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");

        txtAll.setTypeface(face);
        txtFriends.setTypeface(face);
        txtNone.setTypeface(face);
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

        vAll.setOnClickListener(v -> {
            imgAll.setVisibility(View.VISIBLE);
            imgFriends.setVisibility(View.INVISIBLE);
            imgNone.setVisibility(View.INVISIBLE);
            saveSettings();
        });

        vFriends.setOnClickListener(v -> {
            imgAll.setVisibility(View.INVISIBLE);
            imgFriends.setVisibility(View.VISIBLE);
            imgNone.setVisibility(View.INVISIBLE);
            saveSettings();
        });

        vNone.setOnClickListener(v -> {
            imgAll.setVisibility(View.INVISIBLE);
            imgFriends.setVisibility(View.INVISIBLE);
            imgNone.setVisibility(View.VISIBLE);
            saveSettings();
        });
    }

    private void saveSettings() {
        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        int priv;
        if (imgAll.getVisibility() == View.VISIBLE) {
            priv = 0;
        } else if (imgFriends.getVisibility() == View.VISIBLE) {
            priv = 2;
        } else {
            priv = 1;
        }
        msp.putInteger("privacy", priv);
        int finalPriv = priv;
        new Thread(() -> {
            ProfileAndSettings.ProfilePrivacyTypeUpdate pptu = ProfileAndSettings.ProfilePrivacyTypeUpdate.newBuilder().setProfileTypeValue(finalPriv).build();
            try {
                ApiCaller.doCall(Endpoints.TYPE_UPDATE, MySharedPreferences.createLogin(getApplicationContext()).getBytes("token"), pptu, Shared.OTCResponse.class);
            } catch (ApiCaller.OTCException e) {
                //Log.e(TAG, e.getMessage(), e);
            }

        }, "PrivacyThread").start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        imgAll.setVisibility(View.INVISIBLE);
        imgFriends.setVisibility(View.INVISIBLE);
        imgNone.setVisibility(View.INVISIBLE);

        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        if (msp.contains("privacy")) {
            int privacyType = msp.getInteger("privacy");
            switch (privacyType) {
                case 0:
                    imgAll.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    imgFriends.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    imgNone.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            imgAll.setVisibility(View.VISIBLE);
            saveSettings();
        }
    }
}
