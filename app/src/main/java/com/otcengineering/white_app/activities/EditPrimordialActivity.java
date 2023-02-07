package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import org.threeten.bp.LocalDate;

import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;


public class EditPrimordialActivity extends EventActivity {

    private ScrollView scrollViewMyProfile;
    private FrameLayout btnScrollUp;
    private CheckBox carOwner;
    private EditText fullName, vin, model, serial, dealer, sex, birthDate, country, region, location, postalCode,
            plate, address, driving, carRegistration, financeStart, financeEnd, insuranceStart, insuranceEnd, bloodType;

    private Button signUp;
    private String telefono, elemail, emptyField;
    private ProfileAndSettings.UserDataResponse udr;

    public EditPrimordialActivity() {
        super("EditPrimordialActivity");
    }
    private String userSex;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_edit_primordial_new);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.comunications_15);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#001e67'>" + getResources().getString(R.string.title_my_profile) + "</font>"));

        getViews();

        setNotEditable(vin);

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

        country.setOnClickListener(v -> {
            Intent intent = new Intent(EditPrimordialActivity.this, ListViewSignUpActivity.class);
            intent.putExtra("num", 2);
            startActivity(intent);
        });

        region.setOnClickListener(v -> {
            Intent intent = new Intent(EditPrimordialActivity.this, ListViewSignUpActivity.class);
            intent.putExtra("num", 3);
            startActivity(intent);
        });

        sex.setOnClickListener(v -> {
            Intent intent = new Intent(EditPrimordialActivity.this, ListViewSignUpActivity.class);
            intent.putExtra("num", 4);
            startActivity(intent);
        });

        bloodType.setOnClickListener(v -> {
            Intent intent = new Intent(EditPrimordialActivity.this, ListViewSignUpActivity.class);
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

        postalCode.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (postalCode.getRight() - postalCode.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    postalCode.setText("");
                    return true;
                }
            }
            return false;
        });

        btnScrollUp.setOnClickListener(view -> scrollViewMyProfile.smoothScrollTo(0, 0));

        scrollViewMyProfile.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollViewMyProfile.getScrollY();
            btnScrollUp.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE);
        });

        MySharedPreferences.createLogin(getApplicationContext()).putString("Vin", "");

        address.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (address.getRight() - address.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    address.setText("");
                    return true;
                }
            }
            return false;
        });

        driving.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (driving.getRight() - driving.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    driving.setText("");
                    return true;
                }
            }
            return false;
        });

        dealer.setOnClickListener(v -> {
            Intent intent = new Intent(EditPrimordialActivity.this, DealerShipActivity.class);
            startActivity(intent);
        });


        signUp.setOnClickListener(v-> {
            if (validations()) {
                General.UserProfile.Builder userProfile = General.UserProfile.newBuilder();

                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                userProfile.setVin(udr.getVin());
                userProfile.setMac(udr.getMac());
                userProfile.setImei(Utils.getImei());
                userProfile.setAddress(address.getText().toString());
                userProfile.setInstallationNumber(udr.getInstallationNumber());

                userProfile.setName(fullName.getText().toString());
                userProfile.setCountryId(msp.getInteger("CountryID"));
                userProfile.setRegion(msp.getInteger("RegionID"));
                userProfile.setCity(location.getText().toString());
                userProfile.setPostalCode(postalCode.getText().toString());

                switch (userSex) {
                    case "Male":
                        userProfile.setSexType(General.SexType.MALE);
                        break;
                    case "Female":
                        userProfile.setSexType(General.SexType.FEMALE);
                        break;
                }
                Stream.of(udr.getTermsList()).forEach(userProfile::addTerms);

                userProfile.setBirthdayDate(getDateAsSrv(birthDate));
                userProfile.setDrivingLicenseDate(getDateAsSrv(driving));
                userProfile.setDealershipId(msp.getInteger("DealerID"));
                userProfile.setPlate(plate.getText().toString());
                userProfile.setLanguage(udr.getLanguage());
                userProfile.setCarOwner(carOwner.isChecked());
                userProfile.setCarRegistrationDate(getDateAsSrv(carRegistration));
                userProfile.setFinanceTermDateStart(getDateAsSrv(financeStart));
                userProfile.setFinanceTermDateEnd(getDateAsSrv(financeEnd));
                userProfile.setInsuranceTermDateStart(getDateAsSrv(insuranceStart));
                userProfile.setInsuranceTermDateEnd(getDateAsSrv(insuranceEnd));
                userProfile.setBloodType(General.BloodType.forNumber(msp.getInteger("BloodType")));

                ProfileAndSettings.UserUpdate.Builder userUpdate = ProfileAndSettings.UserUpdate.newBuilder();

                userUpdate.setUsername(udr.getUsername());
                userUpdate.setPhone(telefono);
                userUpdate.setEmail(elemail);
                userUpdate.setProfile(userProfile);

                GenericTask updateProfile = new GenericTask(Endpoints.USER_UPDATE, userUpdate.build(), true, otcResponse -> {
                    if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                        finish();
                    } else {
                        showError("Update error", CloudErrorHandler.handleError(otcResponse.getStatusValue()));
                    }
                });
                updateProfile.execute();
            }
        });

        fillFields();
        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        dealer.setText(msp.getString("DealerShipName"));
    }

    private String getDateAsSrv(EditText et) {
        return DateUtils.reparseDate(et.getText().toString(), DateUtils.FMT_DATE, DateUtils.FMT_SRV_DATE);
    }

    private void setNotEditable(EditText et) {
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setCursorVisible(false);
    }

    private boolean validations() {
        String validacionM = validarMatricula(plate.getText().toString());
        String validacionM2 = validarNumerosMatricula(plate.getText().toString());
        String validacionFN = validarNombreNums(fullName.getText().toString());
        String validacionL = validarNombreNums(location.getText().toString());

        if (checkEmptyFields()) {
            showError(emptyField + " " + getResources().getString(R.string.error_default), getResources().getString(R.string.field) + " " + emptyField + " " + getResources().getString(R.string.is_empty));
            return false;
        }

        if (Pattern.matches(".*[YMD].*", birthDate.getText().toString())) {
            Toast.makeText(EditPrimordialActivity.this, getResources().getString(R.string.malformed_data), Toast.LENGTH_LONG).show();
        } else {
            if (validacionL.equals("error")) {
                Toast.makeText(EditPrimordialActivity.this, getResources().getString(R.string.malformed_location), Toast.LENGTH_LONG).show();
            } else {
                if (validacionFN.equals("error")) {
                    Toast.makeText(EditPrimordialActivity.this, getResources().getString(R.string.malformed_name), Toast.LENGTH_LONG).show();
                } else {
                    if (validacionM2.equals("error")) {
                        Toast.makeText(EditPrimordialActivity.this, getResources().getString(R.string.malformed_plate), Toast.LENGTH_LONG).show();
                    } else {
                        if (validacionM.equals("error")) {
                            Toast.makeText(EditPrimordialActivity.this, getResources().getString(R.string.malformed_plate), Toast.LENGTH_LONG).show();

                        } else {
                            if (vin.getText().toString().equals("") || model.getText().toString().equals("")
                                    || serial.getText().toString().equals("") || fullName.getText().toString().equals("")
                                    || sex.getText().toString().equals("") || country.getText().toString().equals("")
                                    || region.getText().toString().equals("") || location.getText().toString().equals("")
                                    || birthDate.getText().toString().equals("")) {
                                Toast.makeText(EditPrimordialActivity.this, getResources().getString(R.string.require_input), Toast.LENGTH_LONG).show();

                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void setDatePicker(EditText et, String startingDate) {
        LocalDate date = DateUtils.stringToDate(startingDate, DateUtils.FMT_SRV_DATE);
        setDatePicker(et, date);
        et.setText(DateUtils.reparseDate(startingDate, DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE));
    }

    private void setDatePicker(EditText et, LocalDate startingDate) {
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setCursorVisible(false);
        DatePickerDialog.OnDateSetListener onDateSetListener =
                (view, year, month, dayOfMonth) -> et.setText(String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        et.setOnClickListener(v ->
                new DatePickerDialog(EditPrimordialActivity.this, onDateSetListener,
                        startingDate.getYear(), startingDate.getMonthValue() - 1, startingDate.getDayOfMonth()).show());
    }

    private void getViews() {
        scrollViewMyProfile = findViewById(R.id.profile_edit_primordial_scrollView);
        btnScrollUp = findViewById(R.id.profile_edit_primordial_btnScrollUp);
        vin = findViewById(R.id.etVin);
        model = findViewById(R.id.etCarModel);
        serial = findViewById(R.id.etSerialNumber);
        dealer = findViewById(R.id.etDealerShip);
        fullName = findViewById(R.id.etFullName);
        sex = findViewById(R.id.etSex);
        birthDate = findViewById(R.id.etBirthDate);
        country = findViewById(R.id.etCountry);
        region = findViewById(R.id.etRegion);
        location = findViewById(R.id.etLocation);
        signUp = findViewById(R.id.btnSignUp);
        carOwner = findViewById(R.id.ckOwner);
        plate = findViewById(R.id.etPlate);
        carOwner.setText(getResources().getString(R.string.car_owner));
        address = findViewById(R.id.etAddress);
        driving = findViewById(R.id.etDriving);
        carRegistration = findViewById(R.id.etCarRegistration);
        financeStart = findViewById(R.id.etFinanceStart);
        financeEnd = findViewById(R.id.etFinanceEnd);
        insuranceStart = findViewById(R.id.etInsuranceStart);
        insuranceEnd = findViewById(R.id.etInsuranceEnd);
        bloodType = findViewById(R.id.etBloodType);
        postalCode = findViewById(R.id.etPostalCode);
    }

    private void fillFields() {
        GenericTask gt = new GenericTask(Endpoints.USER_INFO, null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                udr = otcResponse.getData().unpack(ProfileAndSettings.UserDataResponse.class);
                if (udr != null) {
                    vin.setText(udr.getVin());
                    fullName.setText(udr.getName());
                    serial.setText(udr.getDongleSerialNumber());
                    plate.setText(udr.getPlate());
                    carOwner.setChecked(udr.getCarOwner());
                    userSex = Utils.capitalizeFirst(udr.getSexType().name());

                    if (userSex.equals("Male")) {
                        sex.setText(getString(R.string.male));
                    } else if (userSex.equals("Female")) {
                        sex.setText(getString(R.string.female));
                    }

                    bloodType.setText(Utils.bloodTypeNormalized(udr.getBloodType()));
                    birthDate.setText(udr.getBirthdayDate());

                    MySharedPreferences.createLogin(this).putInteger("BloodType", udr.getBloodTypeValue());

                    setDatePicker(birthDate, udr.getBirthdayDate());
                    setDatePicker(driving, udr.getDrivingLicenseDate());
                    setDatePicker(carRegistration, udr.getCarRegistrationDate());
                    setDatePicker(financeStart, udr.getFinanceTermDateStart());
                    setDatePicker(financeEnd, udr.getFinanceTermDateEnd());
                    setDatePicker(insuranceStart, udr.getInsuranceTermDateStart());
                    setDatePicker(insuranceEnd, udr.getInsuranceTermDateEnd());

                    setCountry(udr.getCountryId());
                    setRegion(udr.getRegion(), udr.getCountryId());
                    setDealership();
                    setModel(udr.getVin());
                    postalCode.setText(udr.getPostalCode());
                    location.setText(udr.getCity());
                    address.setText(udr.getAddress());
                    telefono = udr.getPhone();
                    elemail = udr.getEmail();
                }
            }
        });
        gt.execute();
    }

    private void setModel(final String vin) {
        new Thread(() -> {
            Welcome.Model.Builder modelWs = Welcome.Model.newBuilder();
            modelWs.setVin(vin);
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());


            try {
                Welcome.ModelResponse dsr = ApiCaller.doCall(Endpoints.MODEL, msp.getBytes("token"), modelWs.build(), Welcome.ModelResponse.class);
                if (dsr != null) {
                    runOnUiThread(() -> model.setText(dsr.getModel()));
                }
            } catch (ApiCaller.OTCException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setCountry(final int id) {
        TypedTask<Welcome.CountriesResponse> getCountries = new TypedTask<>(Endpoints.GET_COUNTRIES, null, false, Welcome.CountriesResponse.class,
        new TypedCallback<Welcome.CountriesResponse>() {
            @Override
            public void onSuccess(@Nonnull @NonNull Welcome.CountriesResponse value) {
                for (Welcome.CountriesResponse.Country cty : value.getCountriesList()) {
                    if (cty.getId() == id) {
                        runOnUiThread(() -> country.setText(cty.getName()));
                    }
                }
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {

            }
        });
        getCountries.execute();
    }

    private void setRegion(final int id, final int countryID) {
        Welcome.Regions regs = Welcome.Regions.newBuilder().setCountryId(countryID).build();
        TypedTask<Welcome.RegionsResponse> getRegions = new TypedTask<>(Endpoints.REGIONS, regs, false, Welcome.RegionsResponse.class,
        new TypedCallback<Welcome.RegionsResponse>() {
            @Override
            public void onSuccess(@Nonnull @NonNull Welcome.RegionsResponse value) {
                for (Welcome.RegionsResponse.Region reg : value.getRegionsList()) {
                    if (reg.getId() == id) {
                        runOnUiThread(() -> region.setText(reg.getName()));
                    }
                }
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {

            }
        });
        getRegions.execute();
    }

    private void setDealership() {
        TypedTask<Community.DealerResponse> getDealer = new TypedTask<>(Endpoints.GET_DEALER, null, true, Community.DealerResponse.class,
                new TypedCallback<Community.DealerResponse>() {
                    @Override
                    public void onSuccess(@Nonnull @NonNull Community.DealerResponse value) {
                        runOnUiThread(() -> dealer.setText(value.getName()));
                    }

                    @Override
                    public void onError(@NonNull Shared.OTCStatus status, String str) {

                    }
                });
        getDealer.execute();
    }

    private boolean checkEmptyFields() {
        boolean isEmpty = false;
        if (location.getText().length() == 0) {
            emptyField = getResources().getString(R.string.location);
            isEmpty = true;
        } else if (fullName.getText().length() == 0) {
            emptyField = getResources().getString(R.string.full_name);
            isEmpty = true;
        }
        return isEmpty;
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

        if (msp.contains("BloodType")) {
            bloodType.setText(Utils.bloodTypeNormalized(General.BloodType.forNumber(msp.getInteger("BloodType"))));
        }

        country.setText(msp.getString("Country"));
        region.setText(msp.getString("Region"));
        dealer.setText(msp.getString("Dealer"));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
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

    private void showError(String title, String message) {
        new AlertDialog.Builder(EditPrimordialActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.close), (dialog, which) -> {
                    // Whatever...
                }).show();

    }
}
