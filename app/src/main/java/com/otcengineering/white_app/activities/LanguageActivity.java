package com.otcengineering.white_app.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.otc.alice.api.model.General;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.blecontrol.service.FileTransfer;
import com.otcengineering.apible.blecontrol.service.HeartBeatService;
import com.otcengineering.apible.blecontrol.service.UpdateCarService;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.service.ConnectDongleService;
import com.otcengineering.white_app.tasks.BadgesTask;
import com.otcengineering.white_app.utils.LanguageUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;

import java.util.Locale;

public class LanguageActivity extends BaseActivity {

    private static final String ENGLISH = "en";
    private static final String BAHASA_INDONESIA = "in";
    private static final String ESPANOL = "es";
    private static final String CATALA = "ca";

    private TitleBar titleBar;
    private LinearLayout vEnglish, vBahasa, vEspanol, vCatala;
    private ImageView imgEnglish, imgBahasa, imgEspanol, imgCatala;
    private TextView txtEnglish, txtBahasa, txtEspanol, txtCatala;

    private ProgressDialog pd;

    public LanguageActivity() {
        super("LanguageActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        pd = new ProgressDialog(this);

        retrieveViews();
        setTypeface();
        setEvents();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.language_titleBar);
        vEnglish = findViewById(R.id.viewEnglish);
        vBahasa = findViewById(R.id.viewBahasa);
        vEspanol = findViewById(R.id.viewEspanol);
        vCatala = findViewById(R.id.viewCatala);
        imgEnglish = findViewById(R.id.imagEnglish);
        imgBahasa = findViewById(R.id.imagBahasa);
        imgEspanol = findViewById(R.id.imagEspanol);
        imgCatala = findViewById(R.id.imagCatala);
        txtEnglish = findViewById(R.id.txtEnglish);
        txtBahasa = findViewById(R.id.txtBahasa);
        txtEspanol = findViewById(R.id.txtEspanol);
        txtCatala = findViewById(R.id.txtCatala);
    }

    private void setTypeface() {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        txtEnglish.setTypeface(face);
        txtBahasa.setTypeface(face);
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

        vEnglish.setOnClickListener(v -> {
            if (imgEnglish.getVisibility() != View.VISIBLE) {
                imgEnglish.setVisibility(View.VISIBLE);
                imgBahasa.setVisibility(View.INVISIBLE);
                imgEspanol.setVisibility(View.INVISIBLE);
                imgCatala.setVisibility(View.INVISIBLE);
                setEnglish();
            }
        });

        vBahasa.setOnClickListener(v -> {
            if (imgBahasa.getVisibility() != View.VISIBLE) {
                imgEnglish.setVisibility(View.INVISIBLE);
                imgEspanol.setVisibility(View.INVISIBLE);
                imgCatala.setVisibility(View.INVISIBLE);
                imgBahasa.setVisibility(View.VISIBLE);
                setBahasa();
            }
        });

        vEspanol.setOnClickListener(v -> {
            if (imgEspanol.getVisibility() != View.VISIBLE) {
                imgEnglish.setVisibility(View.INVISIBLE);
                imgEspanol.setVisibility(View.VISIBLE);
                imgCatala.setVisibility(View.INVISIBLE);
                imgBahasa.setVisibility(View.INVISIBLE);
                setEspanol();
            }
        });

        vCatala.setOnClickListener(v -> {
            if (imgCatala.getVisibility() != View.VISIBLE) {
                imgEnglish.setVisibility(View.INVISIBLE);
                imgEspanol.setVisibility(View.INVISIBLE);
                imgCatala.setVisibility(View.VISIBLE);
                imgBahasa.setVisibility(View.INVISIBLE);
                setCatala();
            }
        });
    }

    private void setEnglish() {
        MySharedPreferences.createLanguage(getApplicationContext()).putString("Idioma","ENGLISH");
        changeLanguage(this, ENGLISH);
    }

    private void setBahasa() {
        MySharedPreferences.createLanguage(getApplicationContext()).putString("Idioma","BAHASA_INDONESIA");
        changeLanguage(this, BAHASA_INDONESIA);
    }

    private void setEspanol() {
        MySharedPreferences.createLanguage(this).putString("Idioma", "ESPAÑOL");
        changeLanguage(this, ESPANOL);
    }

    private void setCatala() {
        MySharedPreferences.createLanguage(this).putString("Idioma", "CATALÀ");
        changeLanguage(this, CATALA);
    }

    private void getInformationAndUpdateLanguageFromUser(@NonNull final String language) {
        new Thread(() -> {
            try {
                //runOnUiThread(pd::show);
                ProfileAndSettings.UserDataResponse response =
                        ApiCaller.doCall(Endpoints.USER_INFO, MySharedPreferences.createLogin(getApplicationContext()).getBytes("token"),
                                null, ProfileAndSettings.UserDataResponse.class);
                if (response == null) {
                    return;
                }

                General.Language lang;

                switch (language) {
                    case "in": lang = General.Language.BAHASA; break;
                    case "es": lang = General.Language.SPANISH; break;
                    case "ca": lang = General.Language.FRENCH; break;
                    default: lang = General.Language.ENGLISH; break;
                }

                General.UserProfile.Builder profile = General.UserProfile.newBuilder()
                        .setAddress(response.getAddress())
                        .setBirthdayDate(response.getBirthdayDate())
                        .setCarOwner(response.getCarOwner())
                        .setCarRegistrationDate(response.getCarRegistrationDate())
                        .setCity(response.getCity())
                        .setCountryId(response.getCountryId())
                        .setDealershipId(response.getDealershipId())
                        .setDongleSerialNumber(response.getDongleSerialNumber())
                        .setDrivingLicenseDate(response.getDrivingLicenseDate())
                        .setFinanceTermDateEnd(response.getFinanceTermDateEnd())
                        .setFinanceTermDateStart(response.getFinanceTermDateStart())
                        .setImei(response.getImei())
                        .setInsuranceTermDateEnd(response.getInsuranceTermDateEnd())
                        .setInsuranceTermDateStart(response.getInsuranceTermDateStart())
                        .setInstallationNumber(response.getInstallationNumber())
                        .setPostalCode(response.getPostalCode())
                        .setLanguage(lang)
                        .setMac(response.getMac())
                        .setName(response.getName())
                        .setPlate(response.getPlate())
                        .setRegion(response.getRegion())
                        .setSexType(response.getSexType())
                        .setVin(response.getVin());
                for (General.TermAcceptance term : response.getTermsList()) {
                    profile.addTerms(term);
                }

                ProfileAndSettings.UserUpdate update = ProfileAndSettings.UserUpdate.newBuilder()
                        .setEmail(response.getEmail())
                        .setPhone(response.getPhone())
                        .setProfile(profile.build())
                        .setUsername(response.getUsername())
                        .build();
                ApiCaller.doCall(Endpoints.USER_UPDATE,
                        MySharedPreferences.createLogin(getApplicationContext()).getBytes("token"), update, Shared.OTCResponse.class);

                MySharedPreferences.createBadges(this).clear();
                Utils.runOnMainThread(() -> new BadgesTask.getVersionBadges().execute());
                PrefsManager.getInstance().saveManualVersion(0, LanguageActivity.this);

                runOnUiThread(pd::dismiss);
                runOnUiThread(() -> {
                    AlertDialog.Builder ad = new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.language_changed))
                            .setMessage(getResources().getString(R.string.app_restart))
                            .setPositiveButton(getResources().getString(R.string.restart_app), (dialog, which) -> restartActivity());

                    ad.show();
                });
                //restartActivity();
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
            }

        }, "LanguageThread").start();
    }

    private void changeLanguage(Context context, String language) {
        pd.setTitle(R.string.loading);
        pd.setMessage(getString(R.string.change_language));
        pd.show();
        Locale locale = Locale.forLanguageTag(language);
        Locale.setDefault(locale);

        LanguageUtils.setLocale(locale);
        LanguageUtils.saveLanguage();
        MySharedPreferences.createLanguage(context).putString("USER_LANGUAGE", language);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        MyApp.setUserLocale(locale);
        //restartActivity();
        getInformationAndUpdateLanguageFromUser(language);
    }

    private void restartActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        int pendingIntentID = 123456;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, pendingIntentID, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);

        stopService(new Intent(this, FileTransfer.class));
        stopService(new Intent(this, HeartBeatService.class));
        stopService(new Intent(this, UpdateCarService.class));
        stopService(new Intent(this, ConnectDongleService.class));

        finishAffinity();
        System.exit(0);
    }

/*
    private void changeLanguage(String language) {
        String country = Locale.getDefault().getCountry();
        Locale locale = new Locale(language, country);
        LanguageUtils.setLocale(locale);
        LanguageUtils.saveLanguage();

        restartApp();
    }

    private void restartApp() {
        Intent restartAppIntent = new Intent(getBaseContext(), MainActivity.class);
        restartAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //To clear the stack
        startActivity(restartAppIntent);
    }
*/
    @Override
    protected void onResume() {
        super.onResume();
        setLanguageSelected();
    }

    private void setLanguageSelected() {
        String language = Locale.getDefault().getLanguage();
        if (BAHASA_INDONESIA.equalsIgnoreCase(language)) {
            imgEnglish.setVisibility(View.INVISIBLE);
            imgBahasa.setVisibility(View.VISIBLE);
            imgCatala.setVisibility(View.INVISIBLE);
            imgEspanol.setVisibility(View.INVISIBLE);
        } else if (ENGLISH.equalsIgnoreCase(language)) {
            imgEnglish.setVisibility(View.VISIBLE);
            imgBahasa.setVisibility(View.INVISIBLE);
            imgCatala.setVisibility(View.INVISIBLE);
            imgEspanol.setVisibility(View.INVISIBLE);
        } else if (CATALA.equalsIgnoreCase(language)) {
            imgEnglish.setVisibility(View.INVISIBLE);
            imgBahasa.setVisibility(View.INVISIBLE);
            imgCatala.setVisibility(View.VISIBLE);
            imgEspanol.setVisibility(View.INVISIBLE);
        } else if (ESPANOL.equalsIgnoreCase(language)) {
            imgEnglish.setVisibility(View.INVISIBLE);
            imgBahasa.setVisibility(View.INVISIBLE);
            imgCatala.setVisibility(View.INVISIBLE);
            imgEspanol.setVisibility(View.VISIBLE);
        }
    }
}
