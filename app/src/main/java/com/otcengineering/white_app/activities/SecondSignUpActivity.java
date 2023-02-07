package com.otcengineering.white_app.activities;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otc.alice.api.model.General;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.BuildConfig;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.WelcomeNetwork;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import static com.otcengineering.white_app.network.ConfigurationNetwork.fetchBluetoothConfiguration;

public class SecondSignUpActivity extends EventActivity {
    private ProgressDialog progressDialogRegister;

    private EditText model, plate, dealer, fullName, sex, birthDate, country, region, location, address, bloodType, etDrivingLicense, etCarRegistration,
        etFinanceStart, etFinanceEnd, etInsuranceStart, etInsuranceEnd, etPostalCode;
    private Switch autoCall;
    private TextView vin, serial, onOff;
    private CheckBox carOwner;
    private Button data, legal, disclaimer, signUp;
    private String instalationNumber;
    private Integer intentsReg;
    private String mac;
    private LinearLayout layoutSerialNumber, layoutVin;

    private String userSex;

    //private OTC_Authenticate Authenticate;
    private Set<String> diccionariHex = new HashSet<>();

    private Boolean com1 = false;
    private Boolean com2 = false;
    private Boolean com3 = false;

    //private boolean countrySelected = false;
    private String emptyField;

    public SecondSignUpActivity() {
        super("SecondSignUpActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_second_sign_up_new);
        loadDictionary();

        Welcome.DeviceSpecs ds = Welcome.DeviceSpecs.newBuilder()
                .setAppVersion(BuildConfig.VERSION_NAME)
                .setMobileSO(String.format("Android %s", Build.VERSION.RELEASE))
                .setMobileIMEI(Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build();
        GenericTask gt = new GenericTask(Endpoints.DEVICE_SPECS, ds, true, (v) -> {});
        gt.execute();

        fetchBluetoothConfiguration();

        retrieveViews();
        setEvents();
        getProfile();

        progressDialogRegister = new ProgressDialog(SecondSignUpActivity.this);
        progressDialogRegister.setCancelable(false);
        progressDialogRegister.setCanceledOnTouchOutside(false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#001e67'>" + getResources().getString(R.string.sign_up) + "</font>"));
    }

    private void setEvents() {
        layoutVin.setOnClickListener(v -> {
            if (!Utils.isBluetoothEnabled()) {
                Utils.enableBle(SecondSignUpActivity.this);
                return;
            }
            MySharedPreferences msp = MySharedPreferences.createLogin(this);
            if (msp.getBoolean("NewPhone") && !msp.contains("Mactmp")) return;
            if (Utils.isLocationDisabled(this)) {
                DialogMultiple cd = new DialogMultiple(this);
                cd.setTitle(getString(R.string._error));
                cd.setDescription(getString(R.string.no_location));
                //cd.setOnDismissListener(dis -> Utils.enableLocation(SecondSignUpActivity.this));
                cd.addButton(getString(R.string.cancel), () -> {});
                cd.addButton(getString(R.string.open_settings), () -> Utils.enableLocation(SecondSignUpActivity.this));
                cd.show();
                return;
            }
            Intent intent = new Intent(SecondSignUpActivity.this, DongleActivity.class);
            intent.putExtra("num", 5);
            startActivity(intent);
        });

        plate.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (plate.getRight() - plate.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    plate.setText("");
                    return true;
                }
            }
            return false;
        });

        /*serial.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (serial.getRight() - serial.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    serial.setText("");
                    return true;
                }
            }
            return false;
        });*/
        layoutSerialNumber.setOnClickListener(v -> {
            Intent intent = new Intent(this, QRActivity.class);
            intent.putExtra("QR_PATTERN", "serial=.+");
            startActivityForResult(intent, QRActivity.QR_RESULT);
        });

        fullName.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (fullName.getRight() - fullName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    fullName.setText("");
                    return true;
                }
            }
            return false;
        });

        sex.setOnClickListener(v -> {
            Intent intent = new Intent(SecondSignUpActivity.this, ListViewSignUpActivity.class);
            intent.putExtra("num", 4);
            startActivity(intent);
        });

        country.setOnClickListener(v -> {
            Intent intent = new Intent(SecondSignUpActivity.this, ListViewSignUpActivity.class);
            intent.putExtra("num", 2);
            startActivity(intent);
            //countrySelected = true;
        });

        region.setOnClickListener(v -> {
            if (country.getText().length() == 0) {
                showError(getResources().getString(R.string.region_error), getResources().getString(R.string.error_select_one_country));
            } else {
                Intent intent = new Intent(SecondSignUpActivity.this, ListViewSignUpActivity.class);
                intent.putExtra("num", 3);
                startActivity(intent);
            }
        });

        bloodType.setOnClickListener(v -> {
            Intent intent = new Intent(SecondSignUpActivity.this, ListViewSignUpActivity.class);
            intent.putExtra("num", 5);
            startActivity(intent);
        });

        location.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (location.getRight() - location.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    location.setText("");
                    return true;
                }
            }
            return false;
        });

        etPostalCode.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPostalCode.getRight() - etPostalCode.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    etPostalCode.setText("");
                    return true;
                }
            }
            return false;
        });

        data.setOnClickListener(v -> {
            endFocus();
            data.requestFocus();
            Intent intent = new Intent(SecondSignUpActivity.this, LegalActivity.class);
            intent.putExtra("num", 5);
            startActivity(intent);
        });

        legal.setOnClickListener(v -> {
            endFocus();
            legal.requestFocus();
            Intent intent = new Intent(SecondSignUpActivity.this, LegalActivity.class);
            intent.putExtra("num", 6);
            startActivity(intent);
        });

        disclaimer.setOnClickListener(v -> {
            endFocus();
            disclaimer.requestFocus();
            Intent intent = new Intent(SecondSignUpActivity.this, LegalActivity.class);
            intent.putExtra("num", 7);
            startActivity(intent);
        });

        dealer.setOnClickListener(v -> {
            if (country.getText().length() == 0) {
                showError(getResources().getString(R.string.dealer_ship_error), getResources().getString(R.string.error_select_one_country));
            } else {
                Intent intent = new Intent(SecondSignUpActivity.this, DealerShipActivity.class);
                intent.putExtra("num", 8);
                startActivity(intent);
            }
        });

        setDatePicker(birthDate);
        setDatePicker(etDrivingLicense);
        setDatePicker(etCarRegistration);
        setDatePicker(etFinanceStart);
        setDatePicker(etFinanceEnd);
        setDatePicker(etInsuranceStart);
        setDatePicker(etInsuranceEnd);

        signUp.setOnClickListener(v -> {
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
            if (msp.getBoolean("NewPhone")) {
                String currentSerialNumber = serial.getText().toString().toUpperCase();
                String bleSerialNumber = OtcBle.getInstance().serialNumber;

                if (bleSerialNumber == null || !bleSerialNumber.equals(currentSerialNumber)) {
                    showError("Error", "Please, connect with the dongle.");
                    return;
                }
            }
            if ((msp.getBoolean("NewPhone") && !msp.getBoolean("DeleteInfo")) || validateInputs()) {
                msp.putString("Vin", vin.getText().toString());
                msp.putString("Plate", plate.getText().toString());
                msp.putString("FullName", fullName.getText().toString());
                msp.putString("Location", location.getText().toString());
                msp.putString("PostalCode", etPostalCode.getText().toString());
                msp.putString("Serial", serial.getText().toString());
                msp.putString("Birth", birthDate.getText().toString());
                String owner;
                if (carOwner.isChecked()) {
                    owner = "Yes";
                } else {
                    owner = "No";
                }
                msp.putString("Owner", owner);
                if (msp.getBoolean("NewPhone")) {
                    newPhone();
                } else {
                    authBLE(0);
                }
            }
        });
    }

    private void endFocus() {
        serial.clearFocus();
        plate.clearFocus();
        fullName.clearFocus();
        location.clearFocus();
        etPostalCode.clearFocus();
        address.clearFocus();
    }

    private void newPhone() {
        progressDialogRegister = new ProgressDialog(SecondSignUpActivity.this);
        progressDialogRegister.setMessage(getString(R.string.loading));
        progressDialogRegister.setCancelable(false);
        runOnUiThread(progressDialogRegister::show);
        TypedTask<Welcome.TermsAcceptanceResponse> getTerms = new TypedTask<>(Endpoints.GET_TERMS_ACCEPTANCE, null, false, Welcome.TermsAcceptanceResponse.class, new TypedCallback<Welcome.TermsAcceptanceResponse>() {
            @Override
            public void onSuccess(@Nonnull @NonNull Welcome.TermsAcceptanceResponse termsAcceptance) {
                String timestamp = "2017-09-10 14:00:11";
                try {
                    Date now = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    timestamp = sdf.format(now);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                General.TermAcceptance.Builder ua = General.TermAcceptance.newBuilder();
                ua.setType(termsAcceptance.getTerms(0).getType());
                ua.setVersion(termsAcceptance.getTerms(0).getVersion());
                ua.setTimestamp(timestamp);
                ua.setMobileIdentifier(Utils.getImei());

                General.TermAcceptance.Builder ua2 = General.TermAcceptance.newBuilder();
                ua2.setType(termsAcceptance.getTerms(1).getType());
                ua2.setVersion(termsAcceptance.getTerms(1).getVersion());
                ua2.setTimestamp(timestamp);
                ua2.setMobileIdentifier(Utils.getImei());

                General.TermAcceptance.Builder ua3 = General.TermAcceptance.newBuilder();
                ua3.setType(termsAcceptance.getTerms(2).getType());
                ua3.setVersion(termsAcceptance.getTerms(2).getVersion());
                ua3.setTimestamp(timestamp);
                ua3.setMobileIdentifier(Utils.getImei());

                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                General.UserProfile.Builder builder = General.UserProfile.newBuilder();
                builder.setVin(vin.getText().toString());
                String macBle = msp.getString("macBLE");
                if (macBle.isEmpty()) {
                    if (mac.isEmpty()) {
                        runOnUiThread(progressDialogRegister::dismiss);
                        showError("Error", "Please, connect with the dongle.");
                        return;
                    } else {
                        macBle = mac;
                        msp.putString("macBLE", mac);
                    }
                }
                builder.setMac(macBle);
                builder.setImei(Utils.getImei());
                builder.setAddress(address.getText().toString().isEmpty() ? " " : address.getText().toString());
                builder.setName(fullName.getText().toString());
                builder.setCountryId(msp.getInteger("CountryID"));
                builder.setRegion(msp.getInteger("RegionID"));
                builder.setCity(location.getText().toString());
                builder.setPostalCode(etPostalCode.getText().toString());

                builder.addTerms(ua);
                builder.addTerms(ua2);
                builder.addTerms(ua3);

                switch (userSex)
                {
                    case "Male":
                        builder.setSexType(General.SexType.MALE);
                        break;
                    case "Female":
                        builder.setSexType(General.SexType.FEMALE);
                        break;
                }

                builder.setBirthdayDate(dateParse(birthDate));
                builder.setDealershipId(msp.getLong("DealerID"));
                builder.setDongleSerialNumber(serial.getText().toString().toUpperCase());
                builder.setPlate(plate.getText().toString());
                Locale locale = Locale.getDefault();
                builder.setLanguage(locale.getLanguage().equals("in") ? General.Language.BAHASA : General.Language.ENGLISH);
                builder.setCarOwner(carOwner.isChecked());
                builder.setBloodType(General.BloodType.forNumber(msp.getInteger("BloodType")));

                builder.setDrivingLicenseDate(dateParse(etDrivingLicense));
                builder.setCarRegistrationDate(dateParse(etCarRegistration));
                builder.setFinanceTermDateStart(dateParse(etFinanceStart));
                builder.setFinanceTermDateEnd(dateParse(etFinanceEnd));
                builder.setInsuranceTermDateStart(dateParse(etInsuranceStart));
                builder.setInsuranceTermDateEnd(dateParse(etInsuranceEnd));

                Utils.runOnBackThread(() -> {
                    instalationNumber = DongleActivity.installationNumber;
                    if (instalationNumber == null || instalationNumber.isEmpty()) {
                        if (OtcBle.getInstance().isConnected()) {
                            instalationNumber = OtcBle.getInstance().getStringValue("autenticate01");
                        } else {
                            instalationNumber = "0123456789abcdef";
                        }
                    }
                    builder.setInstallationNumber(instalationNumber);

                    General.UserProfile profile = builder.build();

                    ProfileAndSettings.UserUpdate uu = ProfileAndSettings.UserUpdate.newBuilder()
                            .setEmail(msp.getString("email"))
                            .setPhone(msp.getString("Tlf"))
                            .setProfile(profile)
                            .setUsername(msp.getString("Nick"))
                            .build();
                    TypedTask<Shared.OTCResponse> updateProfile = new TypedTask<>(Endpoints.USER_UPDATE, uu, true, Shared.OTCResponse.class, new TypedCallback<Shared.OTCResponse>() {
                        @Override
                        public void onSuccess(@Nonnull @NonNull Shared.OTCResponse value) {
                            new Thread(() -> {
                                if (!OtcBle.getInstance().serialNumber.equals(MySharedPreferences.createLogin(SecondSignUpActivity.this).getString("SerialNumbertmp"))) {
                                    Utils.runOnMainThread(() -> progressDialogRegister.setMessage(getString(R.string.register_dongle)));
                                    registerDongleNewPhone();
                                } else {
                                    enableUserNewPhone();
                                }
                            }).start();
                        }

                        @Override
                        public void onError(@NonNull Shared.OTCStatus status, String str) {
                            runOnUiThread(progressDialogRegister::dismiss);
                            showError(CloudErrorHandler.handleError(status), str);
                        }
                    });
                    updateProfile.execute();
                });
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {
                runOnUiThread(progressDialogRegister::dismiss);
                showError(CloudErrorHandler.handleError(status), str);
            }
        });
        getTerms.execute();
    }

    private CustomDialog enableUserNewPhoneDialog;
    private void enableUserNewPhone() {
        try {
            OtcBle.getInstance().connect();
        } catch (NullPointerException e) {
            //Log.e("LoginSignUpV2", "Exception", e);
        }
        MySharedPreferences msp = MySharedPreferences.createLogin(this);
        Utils.runOnMainThread(() -> progressDialogRegister.setMessage(getString(R.string.enabling_user) + " " + msp.getString("Nick")));
        Welcome.UserEnabled en = Welcome.UserEnabled.newBuilder().setUsername(msp.getString("Nick")).build();
        TypedTask<Shared.OTCResponse> enableUser = new TypedTask<>(Endpoints.ENABLE_USER, en, true, Shared.OTCResponse.class, new TypedCallback<Shared.OTCResponse>() {
            @Override
            public void onSuccess(@Nonnull @NonNull Shared.OTCResponse value) {
                runOnUiThread(progressDialogRegister::dismiss);
                String text = getString(R.string.phone_updated_good);
                if (OtcBle.getInstance().isConnected()) {
                    text += ".";
                } else {
                    text += ", " + getString(R.string.connect_dongle);
                }
                enableUserNewPhoneDialog = new CustomDialog(SecondSignUpActivity.this);
                enableUserNewPhoneDialog.setMessage(text);
                enableUserNewPhoneDialog.setOnOkListener(() -> {
                    Intent intent = new Intent(SecondSignUpActivity.this, Home2Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
                enableUserNewPhoneDialog.show();
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {
                runOnUiThread(progressDialogRegister::dismiss);
                showError(CloudErrorHandler.handleError(status), str);
            }
        });
        enableUser.execute();
    }

    private void registerDongleNewPhone() {
        OtcBle.getInstance().setOnUpdatedValues(null);
        new Thread(() -> {
            if (!OtcBle.getInstance().isConnected()) {
                OtcBle.getInstance().status = false;
                OtcBle.getInstance().connect();
            }
            int tries = 5;
            long timeout = System.currentTimeMillis() + 20000;
            while (!OtcBle.getInstance().isConnected()) {
                if (System.currentTimeMillis() >= timeout) {
                    OtcBle.getInstance().status = false;
                    OtcBle.getInstance().connect();
                    timeout = System.currentTimeMillis() + 20000;
                    --tries;
                    if (tries == 0) {
                        runOnUiThread(() -> {
                            progressDialogRegister.dismiss();
                            new AlertDialog.Builder(SecondSignUpActivity.this)
                                    .setTitle("Error")
                                    .setMessage(getApplicationContext().getString(R.string.no_register_dongle))
                                    .setPositiveButton("Ok", (dialog, which) -> runOnUiThread(() -> new ActivateUser().execute())).show();
                        });
                        return;
                    }
                }
            }
            for (int i = 0; i < 5; ++i) {
                OtcBle.getInstance().register();
                byte[] regproc_r = OtcBle.getInstance().readLongTag("REGPROC_R", false);
                if (regproc_r != null && (((regproc_r[0] & 255) << 8)|(regproc_r[1] & 255)) > 0x100) {
                    runOnUiThread(this::enableUserNewPhone);
                    return;
                }
            }
            OtcBle.getInstance().disconnect();
            runOnUiThread(() -> progressDialogRegister.dismiss());
            runOnUiThread(() -> new AlertDialog.Builder(SecondSignUpActivity.this)
                    .setTitle("Error")
                    .setMessage(getApplicationContext().getString(R.string.no_register_dongle))
                    .setPositiveButton("Ok", (dialog, which) -> {}).show());
        }, "SecondSignUpThread2").start();

        MySharedPreferences.createLogin(getApplicationContext()).putString("macBLE", OtcBle.getInstance().getDeviceMac());
    }

    private void setDatePicker(EditText et) {
        DatePickerDialog.OnDateSetListener onDateSetListener =
                (view, year, month, dayOfMonth) -> et.setText(String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        et.setOnClickListener(v -> {
            LocalDate date = LocalDate.now();
            new DatePickerDialog(SecondSignUpActivity.this, onDateSetListener, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show();
        });
    }

    private boolean validateInputs() {
        String validacionM = validarMatricula(plate.getText().toString());
        String validacionM2 = validarNumerosMatricula(plate.getText().toString());
        String validacionFN = validarNombreNums(fullName.getText().toString());
        String validacionL = validarNombreNums(location.getText().toString());

        if (checkEmptyFields()) {
            showError(emptyField + " " + getResources().getString(R.string.error_default), getResources().getString(R.string.field) + " " + emptyField + " " + getResources().getString(R.string.is_empty));
        } else if (Pattern.matches(".*[YMD].*", birthDate.getText().toString())) {
            showError(getResources().getString(R.string.date_error), getResources().getString(R.string.malformed_data));
        } else if (validacionL.equals("error")) {
            showError(getResources().getString(R.string.location_error), getResources().getString(R.string.malformed_location));
        } else if (validacionFN.equals("error")) {
            showError(getResources().getString(R.string.name_error), getResources().getString(R.string.malformed_name));
        } else if (validacionM2.equals("error")) {
            showError(getResources().getString(R.string.plate_error), getResources().getString(R.string.malformed_plate));
        } else if (validacionM.equals("error")) {//checkSN
            showError(getResources().getString(R.string.plate_error), getResources().getString(R.string.malformed_plate));
        } else if (Pattern.matches(".*[éèàáóíù].*", plate.getText().toString())) {
            showError(getResources().getString(R.string.plate_error), getResources().getString(R.string.malformed_plate));
        } else if (vin.getText().toString().equals("") || model.getText().toString().equals("") || serial.getText().toString().equals("") || fullName.getText().toString().equals("") || sex.getText().toString().equals("") || country.getText().toString().equals("") || etPostalCode.getText().toString().isEmpty() ||  region.getText().toString().equals("") || location.getText().toString().equals("") || dealer.getText().toString().equals("") || birthDate.getText().toString().equals("")) {
            showError(getResources().getString(R.string.error_default), getResources().getString(R.string.required_field_missing));
        } else if (!com1 || !com2 || !com3) {
            showError(getResources().getString(R.string.error_default), getResources().getString(R.string.required_field_missing));
        } else if (!checkSN(serial.getText().toString())) {
            showError(getResources().getString(R.string.serial_number_error), getString(R.string.bad_sn));
        } else if (!serial.getText().toString().toUpperCase().equals(OtcBle.getInstance().serialNumber)) {
            showError(getResources().getString(R.string.serial_number_error), getString(R.string.bad_sn));
        } else {
            return true;
        }
        return false;
    }

    private String getDate(String other) {
        return DateUtils.reparseDate(other, DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE);
    }

    private void getProfile() {
        MySharedPreferences loginPrefs = MySharedPreferences.createLogin(this);

        GenericTask gt = new GenericTask(Endpoints.USER_INFO, null, true, otcResponse -> {
            if (otcResponse != null && otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                ProfileAndSettings.UserDataResponse udr = otcResponse.getData().unpack(ProfileAndSettings.UserDataResponse.class);

                loginPrefs.putString("Tlf", udr.getPhone());
                loginPrefs.putString("email", udr.getEmail());
                loginPrefs.putString("Mactmp", udr.getMac());
                loginPrefs.putString("Vintmp", udr.getVin());
                loginPrefs.putString("SerialNumbertmp", udr.getDongleSerialNumber());
                instalationNumber = udr.getInstallationNumber();
                DongleActivity.installationNumber = udr.getInstallationNumber();

                OtcBle.getInstance().setContext(MyApp.getContext());
                OtcBle.getInstance().createBleLibrary();
                OtcBle.getInstance().setDeviceMac(udr.getMac());
                OtcBle.getInstance().serialNumber = udr.getDongleSerialNumber();

                if (!loginPrefs.getBoolean("DeleteInfo")) {
                    vin.setText(udr.getVin());
                    serial.setText(udr.getDongleSerialNumber());
                    plate.setText(udr.getPlate());
                    fullName.setText(udr.getName());
                    carOwner.setChecked(udr.getCarOwner());
                    birthDate.setText(getDate(udr.getBirthdayDate()));
                    location.setText(udr.getCity());
                    address.setText(udr.getAddress());
                    bloodType.setText(Utils.bloodTypeNormalized(udr.getBloodType()));
                    etDrivingLicense.setText(getDate(udr.getDrivingLicenseDate()));
                    etCarRegistration.setText(getDate(udr.getCarRegistrationDate()));
                    etFinanceStart.setText(getDate(udr.getFinanceTermDateStart()));
                    etFinanceEnd.setText(getDate(udr.getFinanceTermDateEnd()));
                    etInsuranceStart.setText(getDate(udr.getInsuranceTermDateStart()));
                    etInsuranceEnd.setText(getDate(udr.getInsuranceTermDateEnd()));
                    etPostalCode.setText(udr.getPostalCode());

                    loginPrefs.putString("Vin", udr.getVin());

                    loginPrefs.putInteger("CountryID", udr.getCountryId());
                    loginPrefs.putInteger("RegionID", udr.getRegion());
                    loginPrefs.putLong("DealerID", udr.getDealershipId());
                    loginPrefs.putInteger("BloodType", udr.getBloodTypeValue());

                    WelcomeNetwork.getCountry(udr.getCountryId(), new Callback<String>() {
                        @Override
                        public void onSuccess(String success) {
                            country.setText(success);
                            loginPrefs.putString("Country", success);
                        }

                        @Override
                        public void onError(Shared.OTCStatus status) {}
                    });

                    WelcomeNetwork.getRegion(udr.getCountryId(), udr.getRegion(), new Callback<String>() {
                        @Override
                        public void onSuccess(String success) {
                            region.setText(success);
                            loginPrefs.putString("Region", success);
                        }

                        @Override
                        public void onError(Shared.OTCStatus status) {}
                    });

                    switch (udr.getSexType())
                    {
                        case MALE:
                            sex.setText(getString(R.string.male));
                            loginPrefs.putString("Sex", "Male");
                            break;
                        case FEMALE:
                            sex.setText(getString(R.string.female));
                            loginPrefs.putString("Sex", "Female");
                            break;
                    }
                    new RellenarCampos().execute();

                    WelcomeNetwork.getDealer(new Callback<String>() {
                        @Override
                        public void onSuccess(String success) {
                            loginPrefs.putString("Dealer", success);
                            dealer.setText(success);
                        }

                        @Override
                        public void onError(Shared.OTCStatus status) {

                        }
                    });

                    loginPrefs.putBoolean("data", true);
                    loginPrefs.putBoolean("legal", true);
                    loginPrefs.putBoolean("discla", true);
                    loginPrefs.putString("macBLE", udr.getMac());
                    mac = udr.getMac();

                    data.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                    com1 = true;
                    legal.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                    com2 = true;
                    disclaimer.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                    com3 = true;
                }
            } else if (otcResponse != null && otcResponse.getStatus() == Shared.OTCStatus.USER_PROFILE_REQUIRED) {
                loginPrefs.remove("NewPhone");
            }
        });
        gt.execute();
    }

    private String dateParse(EditText et) {
        return DateUtils.reparseDate(et.getText().toString(), "dd/MM/yyyy", "yyyy-MM-dd");
    }
    private boolean checkEmptyFields() {
        boolean isEmpty = false;
        if (vin.getText().length() == 0) {
            emptyField = getResources().getString(R.string.vin_number);
            isEmpty = true;
        } else if (serial.getText().length() == 0) {
            emptyField = getResources().getString(R.string.serial_number);
            isEmpty = true;
        } else if (fullName.getText().length() == 0) {
            emptyField = getResources().getString(R.string.full_name);
            isEmpty = true;
        } else if (sex.getText().length() == 0) {
            emptyField = getResources().getString(R.string.sex);
            isEmpty = true;
        } else if (birthDate.getText().length() == 0) {
            emptyField = getResources().getString(R.string.birth_text);
            isEmpty = true;
        } else if (location.getText().length() == 0) {
            emptyField = getResources().getString(R.string.location);
            isEmpty = true;
        } else if (country.getText().length() == 0) {
            emptyField = getResources().getString(R.string.country_text);
            isEmpty = true;
        } else if (region.getText().length() == 0) {
            emptyField = getResources().getString(R.string.region_text);
            isEmpty = true;
        } else if (dealer.getText().length() == 0) {
            emptyField = getResources().getString(R.string.dealer_ship);
            isEmpty = true;
        } else if (etPostalCode.getText().length() == 0) {
            emptyField = "Postal Code";
            isEmpty = true;
        }

        return isEmpty;
    }

    private void retrieveViews() {
        vin = findViewById(R.id.etVin);
        model = findViewById(R.id.etCarModel);
        serial = findViewById(R.id.etSerialNumber);
        plate = findViewById(R.id.etPlate);
        dealer = findViewById(R.id.etDealerShip);
        autoCall = findViewById(R.id.swiAutocall);
        onOff = findViewById(R.id.txtOnOff);
        fullName = findViewById(R.id.etFullName);
        sex = findViewById(R.id.etSex);
        birthDate = findViewById(R.id.etBirthDate);
        country = findViewById(R.id.etCountry);
        region = findViewById(R.id.etRegion);
        location = findViewById(R.id.etLocation);
        etPostalCode = findViewById(R.id.etPostalCode);
        address = findViewById(R.id.etAddress);
        data = findViewById(R.id.btnPrivacy);
        legal = findViewById(R.id.btnLegal);
        disclaimer = findViewById(R.id.btnDisclaimer);
        signUp = findViewById(R.id.btnSignUp);
        carOwner = findViewById(R.id.ckOwner);
        carOwner.setText(getResources().getString(R.string.car_owner));
        bloodType = findViewById(R.id.etBloodType);
        etDrivingLicense = findViewById(R.id.etDrivingLicense);
        etCarRegistration = findViewById(R.id.etCarRegistration);
        etFinanceStart = findViewById(R.id.etFinanceStart);
        etFinanceEnd = findViewById(R.id.etFinanceEnd);
        etInsuranceStart = findViewById(R.id.etInsuranceStart);
        etInsuranceEnd = findViewById(R.id.etInsuranceEnd);

        layoutSerialNumber = findViewById(R.id.layoutSerialNumber);
        layoutVin = findViewById(R.id.layoutVin);
    }

    public void authBLE(int intents) {
        int intentsAuth = intents + 1;
        if (intentsAuth >= 2) {
            progressDialogRegister.dismiss();
            new AlertDialog.Builder(SecondSignUpActivity.this)
                    .setTitle(getResources().getString(R.string.error_default))
                    .setMessage(getResources().getString(R.string.auth_dongle))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                        // Whatever...
                    }).show();
            return;
        }

        progressDialogRegister.setTitle(getResources().getString(R.string.authentication));
        progressDialogRegister.show();

        instalationNumber = DongleActivity.installationNumber;

        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new InsertUserProfile(
                    vin.getText().toString(),
                    plate.getText().toString(),
                    fullName.getText().toString(),
                    userSex,
                    birthDate.getText().toString(),
                    location.getText().toString(),
                    serial.getText().toString(),
                    carOwner.isChecked(),
                    MySharedPreferences.createLogin(getApplicationContext()).getInteger("DealerID")
            ).execute();
        } else {
            progressDialogRegister.dismiss();
            ConnectionUtils.showOfflineToast();
        }

    }


    public void regDongle(Integer intents) {
        intentsReg = intents + 1;

        progressDialogRegister.setTitle(getApplicationContext().getString(R.string.register_dongle));

        if (intentsReg >= 2) {
            progressDialogRegister.dismiss();
            new AlertDialog.Builder(SecondSignUpActivity.this)
                    .setTitle(getResources().getString(R.string.error_default))
                    .setMessage(getResources().getString(R.string.regis_dongle))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                        // Whatever...
                    }).show();

            return;
        }

        OtcBle.getInstance().setOnUpdatedValues(null);
        new Thread(() -> {
            for (int j = 0; j < 5; ++j) {
                if (!OtcBle.getInstance().isConnected()) {
                    OtcBle.getInstance().status = false;
                    OtcBle.getInstance().connect();
                }
                int tries = 5;
                long timeout = System.currentTimeMillis() + 20000;
                while (!OtcBle.getInstance().isConnected()) {
                    if (System.currentTimeMillis() >= timeout) {
                        OtcBle.getInstance().status = false;
                        OtcBle.getInstance().connect();
                        timeout = System.currentTimeMillis() + 20000;
                        --tries;
                        if (tries == 0) {
                            runOnUiThread(() -> {
                                progressDialogRegister.dismiss();
                                new AlertDialog.Builder(SecondSignUpActivity.this)
                                        .setTitle("Error")
                                        .setMessage(getApplicationContext().getString(R.string.no_register_dongle))
                                        .setPositiveButton("Ok", (dialog, which) -> runOnUiThread(() -> new ActivateUser().execute())).show();
                            });
                            return;
                        }
                    }
                }
                for (int i = 0; i < 5; ++i) {
                    OtcBle.getInstance().register();
                    byte[] regproc_r = OtcBle.getInstance().readLongTag("REGPROC_R", false);
                    if (regproc_r != null && (((regproc_r[0] & 255) << 8)|(regproc_r[1] & 255)) > 0x100) {
                        runOnUiThread(() -> new ActivateUser().execute());
                        return;
                    }
                }
                OtcBle.getInstance().disconnect();
            }
            runOnUiThread(() -> progressDialogRegister.dismiss());
            runOnUiThread(() -> new AlertDialog.Builder(SecondSignUpActivity.this)
                    .setTitle("Error")
                    .setMessage(getApplicationContext().getString(R.string.no_register_dongle))
                    .setPositiveButton("Ok", (dialog, which) -> {}).show());
        }, "SecondSignUpThread2").start();

        MySharedPreferences.createLogin(getApplicationContext()).putString("macBLE", OtcBle.getInstance().getDeviceMac());
    }


    protected void onResume() {
        super.onResume();
        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

        userSex = msp.getString("Sex");

        if (userSex.equals("Male")) {
            sex.setText(getString(R.string.male));
        } else if (userSex.equals("Female")) {
            sex.setText(getString(R.string.female));
        }

        country.setText(msp.getString("Country"));
        region.setText(msp.getString("Region"));
        dealer.setText(msp.getString("Dealer"));
        vin.setText(msp.getString("Vin"));

        if (msp.contains("BloodType")) {
            General.BloodType bt = General.BloodType.forNumber(msp.getInteger("BloodType"));
            if (bt != null) {
                bloodType.setText(Utils.bloodTypeNormalized(bt));
            }
        }

        autoCall.setOnClickListener(v -> {
            if (!autoCall.isChecked()) {
                onOff.setText(getResources().getString(R.string.off));
            } else {
                onOff.setText(getResources().getString(R.string.on));
            }
        });

        if (msp.getBoolean("data")) {
            data.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
            com1 = true;
        }
        if (msp.getBoolean("legal")) {
            legal.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
            com2 = true;
        }
        if (msp.getBoolean("discla")) {
            disclaimer.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
            com3 = true;
        }

        if (!vin.getText().toString().equals("")) {
            if (ConnectionUtils.isOnline(getApplicationContext())) {
                new RellenarCampos().execute();
            } else {
                ConnectionUtils.showOfflineToast();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QRActivity.QR_RESULT && resultCode == RESULT_OK) {
            String code = data.getStringExtra("QR_RESULT");
            String sn = code.replace("serial=", "");
            serial.setText(sn);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(SecondSignUpActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return true;
    }


    public void loadDictionary() {
        String[] allowedCharsHex = {"a", "b", "c", "d", "e", "f", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        Collections.addAll(diccionariHex, allowedCharsHex);
    }

    public boolean checkSN(String SN) {
        for (int i = 0; i < SN.length(); i++) {
            String valid = SN.charAt(i) + "";
            if (!diccionariHex.contains(valid.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public String validarMatricula(String str) {
        if (str == null) {
            return null;
        }
        String valor = getString(R.string.caracter_extraños);
        char[] array = str.toCharArray();
        for (char anArray : array) {
            int pos = valor.indexOf(anArray);
            if (pos > -1) {
                return "error";
            }
        }
        return new String(array);
    }

    public String validarNumerosMatricula(String str) {
        return Pattern.matches("^[0-9]+$", str) ? "error" : "ok";
    }

    public String validarNombreNums(String str) {
        if (str == null) {
            return null;
        }
        String valor = getString(R.string.caracter_extraños_excepcion_nums);
        char[] array = str.toCharArray();
        for (char anArray : array) {
            int pos = valor.indexOf(anArray);
            if (pos > -1) {
                return "error";
            }
        }

        return new String(array);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.birthDate.clearFocus();
        this.bloodType.clearFocus();
        this.dealer.clearFocus();
        this.etCarRegistration.clearFocus();
        this.etDrivingLicense.clearFocus();
        this.etFinanceEnd.clearFocus();
        this.etFinanceStart.clearFocus();
        this.etInsuranceEnd.clearFocus();
        this.etInsuranceStart.clearFocus();
        this.fullName.clearFocus();
        this.location.clearFocus();
        this.etPostalCode.clearFocus();
        this.plate.clearFocus();
        this.serial.clearFocus();
        this.region.clearFocus();
        this.sex.clearFocus();
        this.vin.clearFocus();
    }

    class RellenarCampos extends AsyncTask<Object, Object, String> {
        String vinSt;

        @Override
        protected void onPreExecute() {
            vinSt = vin.getText().toString();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Welcome.Model.Builder modelWs = Welcome.Model.newBuilder();
                modelWs.setVin(vinSt);

                Welcome.ModelResponse dsr = ApiCaller.doCall(Endpoints.MODEL, modelWs.build(), Welcome.ModelResponse.class);
                return dsr.getModel();
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String lista) {
            model.setText(lista);
        }
    }


    class InsertUserProfile extends AsyncTask<String, String, Shared.OTCResponse> {
        //private ProgressDialog progressDialog = new ProgressDialog(SecondSignUpActivity.this);

        private final String vin;
        private final String plate;
        private final String fullName;
        private final String sex;
        private final String birthDate;
        private final String location;
        private final String serialNumber;
        private final Boolean carOwner;
        private final int dealer;
        private General.UserProfile profile;

        InsertUserProfile(String vin, String plate, String fullName, String sex, String birthDate, String location, String serialNumber, Boolean carOwner, int dealer) {
            this.vin = vin;
            this.plate = plate;
            this.fullName = fullName;
            this.sex = sex;
            this.birthDate = birthDate;
            this.location = location;
            this.serialNumber = serialNumber;
            this.carOwner = carOwner;
            this.dealer = dealer;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Shared.OTCResponse doInBackground(String... params) {
            try {
                Welcome.TermsAcceptanceResponse termsAcceptance = ApiCaller.doCall(Endpoints.GET_TERMS_ACCEPTANCE, null, Welcome.TermsAcceptanceResponse.class);
                String timestamp = "2017-09-10 14:00:11";
                try {
                    Date now = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    timestamp = sdf.format(now);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                General.TermAcceptance.Builder ua = General.TermAcceptance.newBuilder();
                ua.setType(termsAcceptance.getTerms(0).getType());
                ua.setVersion(termsAcceptance.getTerms(0).getVersion());
                ua.setTimestamp(timestamp);
                ua.setMobileIdentifier(Utils.getImei());

                General.TermAcceptance.Builder ua2 = General.TermAcceptance.newBuilder();
                ua2.setType(termsAcceptance.getTerms(1).getType());
                ua2.setVersion(termsAcceptance.getTerms(1).getVersion());
                ua2.setTimestamp(timestamp);
                ua2.setMobileIdentifier(Utils.getImei());

                General.TermAcceptance.Builder ua3 = General.TermAcceptance.newBuilder();
                ua3.setType(termsAcceptance.getTerms(2).getType());
                ua3.setVersion(termsAcceptance.getTerms(2).getVersion());
                ua3.setTimestamp(timestamp);
                ua3.setMobileIdentifier(Utils.getImei());

                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                General.UserProfile.Builder builder = General.UserProfile.newBuilder();
                builder.setVin(vin);
                builder.setMac(msp.getString("macBLE"));
                builder.setImei(Utils.getImei());
                builder.setAddress(address.getText().toString().isEmpty() ? " " : address.getText().toString());
                builder.setName(fullName);
                builder.setCountryId(msp.getInteger("CountryID"));
                builder.setRegion(msp.getInteger("RegionID"));
                builder.setCity(location);
                builder.setPostalCode(etPostalCode.getText().toString());

                builder.addTerms(ua);
                builder.addTerms(ua2);
                builder.addTerms(ua3);

                switch (userSex)
                {
                    case "Male":
                        builder.setSexType(General.SexType.MALE);
                        break;
                    case "Female":
                        builder.setSexType(General.SexType.FEMALE);
                        break;
                }

                builder.setBirthdayDate(DateUtils.reparseDate(birthDate, "dd/MM/yyyy", "yyyy-MM-dd"));
                builder.setDealershipId(dealer);
                builder.setDongleSerialNumber(serialNumber.toUpperCase());
                builder.setPlate(plate);
                builder.setLanguage(MyApp.getUserLocale().getLanguage().equals("in") ? General.Language.BAHASA : General.Language.ENGLISH);
                builder.setCarOwner(carOwner);
                builder.setBloodType(General.BloodType.forNumber(msp.getInteger("BloodType")));

                builder.setDrivingLicenseDate(dateParse(etDrivingLicense));
                builder.setCarRegistrationDate(dateParse(etCarRegistration));
                builder.setFinanceTermDateStart(dateParse(etFinanceStart));
                builder.setFinanceTermDateEnd(dateParse(etFinanceEnd));
                builder.setInsuranceTermDateStart(dateParse(etInsuranceStart));
                builder.setInsuranceTermDateEnd(dateParse(etInsuranceEnd));

                instalationNumber = DongleActivity.installationNumber;
                if (instalationNumber == null || instalationNumber.isEmpty()) {
                    if (OtcBle.getInstance().isConnected()) {
                        for (int i = 0; i < 3; i++) {
                            String[] in = OtcBle.getInstance().getAuthenticateValues();
                            instalationNumber = in[0];
                        }
                        if (instalationNumber == null || instalationNumber.isEmpty()) {
                            return Shared.OTCResponse.newBuilder().setStatus(Shared.OTCStatus.UNUSED_RESERVED).build();
                        }
                    } else {
                        return Shared.OTCResponse.newBuilder().setStatus(Shared.OTCStatus.UNUSED_RESERVED).build();
                    }
                }
                builder.setInstallationNumber(instalationNumber);

                profile = builder.build();

                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.PROFILE, MySharedPreferences.createLogin(getApplicationContext()).getBytes("token"), builder.build(), Shared.OTCResponse.class);
                if (response.getStatus() == Shared.OTCStatus.PROFILE_ALREADY_EXISTS) {
                    ProfileAndSettings.UserUpdate uu = ProfileAndSettings.UserUpdate.newBuilder()
                            .setEmail(msp.getString("email"))
                            .setPhone(msp.getString("Tlf"))
                            .setProfile(profile)
                            .setUsername(msp.getString("Nick"))
                            .build();
                    response = ApiCaller.doCall(Endpoints.USER_UPDATE, true, uu, Shared.OTCResponse.class);
                }
                return response;
            } catch (Exception e) {
                //Log.e("SecondSignUpActivity", "Exception", e);
                return Shared.OTCResponse.newBuilder().setStatus(Shared.OTCStatus.UNRECOGNIZED).build();
            }
        }

        @Override
        protected void onPostExecute(Shared.OTCResponse response) {
            Shared.OTCStatus status = response.getStatus();
            if (status == Shared.OTCStatus.SUCCESS) {
                regDongle(0);
            } else if (status == Shared.OTCStatus.PROFILE_ALREADY_EXISTS) {
                intentsReg = 0;
                regDongle(0);
            } else if (status == Shared.OTCStatus.UNUSED_RESERVED) {
                progressDialogRegister.dismiss();
                new AlertDialog.Builder(SecondSignUpActivity.this)
                        .setTitle(getApplicationContext().getString(R.string.error_default))
                        .setMessage("Cannot connect to the dongle. Please, try again.")
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {

                        }).show();
            } else {
                String error = CloudErrorHandler.handleError(status);
                // The infamous error 9
                if (status == Shared.OTCStatus.REQUIRED_FIELDS_MISSING) {
                    String miss = response.getMessage();

                    progressDialogRegister.dismiss();
                    new AlertDialog.Builder(SecondSignUpActivity.this)
                            .setTitle(getApplicationContext().getString(R.string.required_field_missing))
                            .setMessage(miss)
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                                // Whatever...
                            }).show();

                } else {
                    progressDialogRegister.dismiss();
                    new AlertDialog.Builder(SecondSignUpActivity.this)
                            .setTitle(getResources().getString(R.string.error_default))
                            .setMessage(getResources().getString(R.string.dongle_server_error) + ' ' + status + '\n' + error)
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                                // Whatever...
                            }).show();
                }
            }
        }
    }

    /**
     * Activate User
     */
    class ActivateUser extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
            runOnUiThread(() -> progressDialogRegister.setTitle(getResources().getString(R.string.enabling_user) + " " + msp.getString("Nick")));
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {

                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                Welcome.UserEnabled.Builder enableUser = Welcome.UserEnabled.newBuilder();
                enableUser.setUsername(msp.getString("Nick"));
                Shared.OTCResponse termsAcceptance = ApiCaller.doCall(Endpoints.ENABLE_USER, msp.getBytes("token"), enableUser.build(), Shared.OTCResponse.class);
                return termsAcceptance.getStatusValue();
            } catch (Exception e) {
                //Log.e("SecondSignUp", "Exception", e);
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status != 1) {
                progressDialogRegister.dismiss();
                String error = CloudErrorHandler.handleError(status);
                showError(getResources().getString(R.string.server_error), String.format("%s. %s", getResources().getString(R.string.error_enabling_user), error));

                // Toast.makeText(SecondSignUpActivity.this, "Error enabling user", Toast.LENGTH_lonG).show();
            } else {
                progressDialogRegister.dismiss();
                Intent intent = new Intent(SecondSignUpActivity.this, Home2Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        OtcBle.getInstance().serialNumber = "";
        Intent intent = new Intent(SecondSignUpActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showError(String title, String message) {
        try {
            runOnUiThread(() -> new AlertDialog.Builder(SecondSignUpActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                        // Whatever...
                    }).show());
        } catch (Exception e) {
            //Log.e("SecondSignUpActivity", "Exception", e);
        }
    }

}
