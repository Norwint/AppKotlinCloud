package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.EditBasicProfileActivity;
import com.otcengineering.white_app.activities.EditPrimordialActivity;
import com.otcengineering.white_app.adapter.ProfileAdapter;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.CustomKeySet;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyProfileFragment extends EventFragment {
    private TextView nick;
    private TextView correo;
    private TextView tlf;
    private NestedScrollView scrollViewMyProfile;
    private FrameLayout btnScrollUp;
    private CircleImageView img;
    private RetrieveFeedTask getDataTask;
    private ProfileAdapter adaptador = new ProfileAdapter();
    private ProgressDialog pd;
    private long m_imageID;

    public MyProfileFragment() {
        super("MyProfileActivity");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_profile, container, false);

        getViews(v);

        Button edit1 = v.findViewById(R.id.btnEdit1);
        Button edit2 = v.findViewById(R.id.btnImgChange);

        edit1.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), EditBasicProfileActivity.class);
            startActivity(intent);
        });

        edit2.setOnClickListener(v12 -> {
            Intent intent = new Intent(getActivity(), EditPrimordialActivity.class);
            startActivity(intent);
        });

        btnScrollUp.setOnClickListener(view -> scrollViewMyProfile.smoothScrollTo(0, 0));

        scrollViewMyProfile.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollViewMyProfile.getScrollY();
            btnScrollUp.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE);
        });

        nick.setText(MySharedPreferences.createLogin(getContext()).getString("Nick"));

        MySharedPreferences msp = MySharedPreferences.createLogin(getContext());
        if (msp.contains("UserImageId")) {
            Long imgId = msp.getLong("UserImageId");
            Glide.with(this).load(imgId).into(img);
        } else {
            Glide.with(this).load(R.drawable.user_placeholder_correct).into(img);
        }

        pd = new ProgressDialog(getContext());
        pd.setCancelable(false);
        pd.setMessage(getString(R.string.loading));

        OtcBle.getInstance().setContext(getContext());
        return v;
    }

    private void getViews(View v) {
        nick = v.findViewById(R.id.txtName);
        correo = v.findViewById(R.id.txtCorreo);
        tlf = v.findViewById(R.id.txtTlf);
        scrollViewMyProfile = v.findViewById(R.id.scrollViewMyProfile);
        btnScrollUp = v.findViewById(R.id.profile_btnScrollUp);
        img = v.findViewById(R.id.imageView3);
        RecyclerView lstRead = v.findViewById(R.id.lvCarac);
        lstRead.setNestedScrollingEnabled(false);
        lstRead.setLayoutManager(new LinearLayoutManager(getContext()));
        lstRead.setAdapter(adaptador);
    }

    private void createListAdapter() {
        final Context ctx = getContext();
        if (ctx == null || Utils.isActivityFinish(ctx) || getActivity() == null) {
            return;
        }
        MySharedPreferences msp = MySharedPreferences.createLogin(ctx);

        setAdapterList(msp.getString("vinNumberProfile"), msp.getString("Car"), msp.getString("plateProfile"),
                msp.getString("serialNumberProfile"), msp.getString("dealerShipProfile"), msp.getString("fullNameProfile"),
                msp.getBoolean("carOwner"), General.BloodType.forNumber(msp.getInteger("BloodType")), msp.getString("Sex"),
                msp.getString("birthdateProfile"), msp.getString("countryProfile"), msp.getString("regionProfile"), msp.getString("postalCodeProfile"),
                msp.getString("locationProfile"), msp.getString("addressProfile"), msp.getString("drivingLicenseProfile"), msp.getString("CarRegistration"),
                msp.getString("FinanceTerm"), msp.getString("InsuranceTerm"));
    }

    private void setAdapterList(String vin, String carModel, String plate, String sn, String dealer, String name, boolean carOwner, General.BloodType bloodType, String sex,
                                String birthday, String country, String region, String postalCode, String location, String address, String drivingLicense, String carRegistration, String financeTerm, String insuranceTerm) {
        Utils.runOnMainThread(() -> {
            adaptador.clear();
            adaptador.addKeySet(getResources().getString(R.string.vin_number) + ":", vin);
            adaptador.addKeySet(getResources().getString(R.string.car_model) + ":", carModel);
            adaptador.addKeySet(getResources().getString(R.string.plate) + ":", plate);
            adaptador.addKeySet(getResources().getString(R.string.serial_number) + ":", sn);
            adaptador.addKeySet(getResources().getString(R.string.dealer_ship) + ":", dealer);
            adaptador.addKeySet(getResources().getString(R.string.full_name) + ":", name);
            adaptador.addKeySet(getString(R.string.car_owner) + ":", carOwner ? getResources().getString(R.string.yes) : getResources().getString(R.string.no));
            adaptador.addKeySet(getResources().getString(R.string.blood_type) + ":", Utils.bloodTypeNormalized(bloodType));
            String userSex = "";
            if (sex.equals("Male")) {
                userSex = getString(R.string.male);
            } else if (sex.equals("Female")) {
                userSex = getString(R.string.female);
            }
            adaptador.addKeySet(getResources().getString(R.string.sex) + ":", userSex);
            adaptador.addKeySet(getResources().getString(R.string.birthday) + ":", birthday);
            adaptador.addKeySet(getResources().getString(R.string.country) + ":", country);
            adaptador.addKeySet(getResources().getString(R.string.region1) + ":", region);
            adaptador.addKeySet(getResources().getString(R.string.location1) + ":", location);
            adaptador.addKeySet("Postal Code:", postalCode);
            adaptador.addKeySet(getResources().getString(R.string.address) + ":", address);
            adaptador.addKeySet(getResources().getString(R.string.driving_license) + ":", drivingLicense);
            adaptador.addKeySet(getResources().getString(R.string.car_registration) + ":", carRegistration);
            adaptador.addKeySet(getResources().getString(R.string.finance_term_date) + ":", financeTerm);
            adaptador.addKeySet(getResources().getString(R.string.insurance_term_date) + ":", insuranceTerm);
            adaptador.notifyDataSetChanged();
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getDataTask != null) {
            getDataTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nick.setText(MySharedPreferences.createLogin(getContext()).getString("Nick"));
        if (ConnectionUtils.isOnline(getContext())) {
            getDataTask = new RetrieveFeedTask();
            getDataTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }

        MySharedPreferences msp = MySharedPreferences.createLogin(getContext());
        nick.setText(msp.getString("Nick"));
        correo.setText(msp.getString("email").toLowerCase());
        tlf.setText(String.format("T. %s", msp.getString("phone")));
        if (msp.contains("UserImageId")) {
            Long usrImgId = msp.getLong("UserImageId");
            Glide.with(this).load(usrImgId).into(img);
            /*Utils.runOnBackground(() -> {
                byte[] bs = ImageUtils.getImageFromCache(getContext(), msp.getString("UserImageId"));
                Utils.runOnMainThread(() -> {
                    Glide.with(this).load(bs).placeholder(img.getDrawable()).into(img);
                });
            });*/
        } else {
            Glide.with(this).load(getContext().getDrawable(R.drawable.user_placeholder_correct)).placeholder(img.getDrawable()).into(img);
        }
        setAdapterList("", "", "", "", "", "", false, General.BloodType.UNDEFINED_,
                "", "", "", "", "", "", "", "", "", "", "");
        if (!msp.contains("Sex")) {
            pd.show();
        }
        Utils.runOnBackground(this::createListAdapter);
    }

    @SuppressLint("StaticFieldLeak")
    private class RetrieveFeedTask extends AsyncTask<Object, Object, ArrayList<CustomKeySet>> {
        ProfileAndSettings.UserDataResponse response;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected ArrayList<CustomKeySet> doInBackground(Object... params) {
            try {
                response = ApiCaller.doCall(Endpoints.USER_INFO, PrefsManager.getInstance().getToken(getContext()), null, ProfileAndSettings.UserDataResponse.class);

                if (response.getVin() == null) {
                    Welcome.UserEnabled.Builder enableUser = Welcome.UserEnabled.newBuilder();
                    enableUser.setUsername(MySharedPreferences.createLogin(getContext()).getString("Nick"));
                    response = ApiCaller.doCall(Endpoints.USER_INFO, PrefsManager.getInstance().getToken(getContext()), null, ProfileAndSettings.UserDataResponse.class);
                }

                Gson gson = new GsonBuilder().create();
                String myProfileInfoJson = gson.toJson(response, ProfileAndSettings.UserDataResponse.class);
                MySharedPreferences.createLogin(getContext()).putString(Constants.Prefs.DB_MY_PROFILE, myProfileInfoJson);
                MySharedPreferences msp = MySharedPreferences.createLogin(getContext());

                ArrayList<CustomKeySet> arrayProfile = new ArrayList<>();

                String countryName = null, regionName = null;

                String dealer = MySharedPreferences.createLogin(getContext()).getString("DealerShipName");
                try {
                    Community.DealerResponse resp = ApiCaller.doCall(Endpoints.GET_DEALER, PrefsManager.getInstance().getToken(getContext()), null, Community.DealerResponse.class);
                    if (resp != null) {
                        dealer = resp.getName();
                        MySharedPreferences.createLogin(getContext()).putString("DealerShipName", dealer);
                    }
                } catch (Exception ignored) {

                }

                try {
                    Welcome.CountriesResponse countryResponse = ApiCaller.doCall(Endpoints.GET_COUNTRIES, null, Welcome.CountriesResponse.class);
                    List<Welcome.CountriesResponse.Country> test = countryResponse.getCountriesList();
                    for (Welcome.CountriesResponse.Country countryR : test) {
                        if (countryR.getId() == response.getCountryId()) {
                            countryName = countryR.getName();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (countryName == null) {
                    if (getContext() == null) {
                        countryName = "Not found";
                    } else {
                        countryName = getResources().getString(R.string.not_found);
                    }
                }

                try {
                    int CountryId = response.getCountryId();
                    Welcome.Regions.Builder ua = Welcome.Regions.newBuilder();
                    ua.setCountryId(CountryId);

                    Welcome.RegionsResponse regionResponse = ApiCaller.doCall(Endpoints.REGIONS, ua.build(), Welcome.RegionsResponse.class);

                    List<Welcome.RegionsResponse.Region> test = regionResponse.getRegionsList();

                    for (Welcome.RegionsResponse.Region regionR : test) {
                        if (regionR.getId() == response.getRegion()) {
                            regionName = regionR.getName();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (regionName == null) {
                    if (getContext() != null) {
                        regionName = getResources().getString(R.string.not_found);
                    } else {
                        regionName = "Not found";
                    }
                }

                Activity act = getActivity();
                if (act != null && !Utils.isActivityFinish(act)) {
                    act.runOnUiThread(() -> getImage(response.getImageId()));
                    msp.putLong("DealerID", response.getDealershipId());
                    msp.putInteger("CountryID", response.getCountryId());
                    msp.putInteger("RegionID", response.getRegion());
                    msp.putString("macBLE", response.getMac());
                    msp.putString("instnumberBLE", response.getInstallationNumber());
                    msp.putString("imeiBLE", response.getImei());

                    try {
                        Welcome.Model.Builder modelWs = Welcome.Model.newBuilder();
                        modelWs.setVin(response.getVin());

                        Welcome.ModelResponse dsr = ApiCaller.doCall(Endpoints.MODEL, modelWs.build(), Welcome.ModelResponse.class);

                        msp.putString("Car", dsr.getModel());
                    } catch (Exception ignored) {

                    }
                }

                msp.putString("vinNumberProfile", response.getVin());
                msp.putString("serialNumberProfile", response.getDongleSerialNumber());
                msp.putString("dealerShipProfile", dealer);
                msp.putString("fullNameProfile", response.getName());
                msp.putBoolean("carOwner", response.getCarOwner());
                msp.putString("Sex", response.getSexType() == General.SexType.MALE ? "Male" : "Female");
                msp.putInteger("BloodType", response.getBloodTypeValue());
                msp.putString("birthdateProfile", DateUtils.reparseDate(response.getBirthdayDate(), DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE));
                msp.putString("countryProfile", countryName);
                msp.putString("regionProfile", regionName);
                msp.putString("locationProfile", response.getCity());
                msp.putString("postalCodeProfile", response.getPostalCode());
                msp.putString("addressProfile", response.getAddress());
                msp.putString("drivingLicenseProfile", DateUtils.reparseDate(response.getDrivingLicenseDate(), DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE));
                msp.putString("plateProfile", response.getPlate());
                msp.putString("CarRegistration", DateUtils.reparseDate(response.getCarRegistrationDate(), DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE));
                msp.putString("FinanceTerm", String.format("%s\n%s", DateUtils.reparseDate(response.getFinanceTermDateEnd(), DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE),
                        DateUtils.reparseDate(response.getFinanceTermDateStart(),  DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE)));
                msp.putString("InsuranceTerm", String.format("%s\n%s", DateUtils.reparseDate(response.getInsuranceTermDateEnd(),  DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE),
                        DateUtils.reparseDate(response.getInsuranceTermDateStart(),  DateUtils.FMT_SRV_DATE, DateUtils.FMT_DATE
                        )));
                msp.putString("email", response.getEmail());
                msp.putString("phone", response.getPhone());

                PaymentUtils.Currency.selectedCurrency = PaymentUtils.Currency.getByCode(response.getCurrency());
                switch (response.getCountryId()) {
                    case 1: PaymentUtils.Currency.selectedCountry = "IN";
                    case 2: PaymentUtils.Currency.selectedCountry = "ID";
                    case 3: PaymentUtils.Currency.selectedCountry = "ES";
                }

                return arrayProfile;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<CustomKeySet> lista) {
            //actionsAfterGetProfileInfo(lista);
            if (response == null) {
                return;
            }
            Context ctx = getContext();
            MySharedPreferences msp = MySharedPreferences.createLogin(ctx);
            Utils.runOnBackThread(() -> setAdapterList(msp.getString("vinNumberProfile"), msp.getString("Car"), msp.getString("plateProfile"),
                    msp.getString("serialNumberProfile"), msp.getString("dealerShipProfile"), msp.getString("fullNameProfile"),
                    msp.getBoolean("carOwner"), General.BloodType.forNumber(msp.getInteger("BloodType")), msp.getString("Sex"),
                    msp.getString("birthdateProfile"), msp.getString("countryProfile"), msp.getString("regionProfile"), msp.getString("postalCodeProfile"),
                    msp.getString("locationProfile"), msp.getString("addressProfile"), msp.getString("drivingLicenseProfile"), msp.getString("CarRegistration"),
                    msp.getString("FinanceTerm"), msp.getString("InsuranceTerm")));

            if (m_imageID == 0 && ctx != null) {
                Glide.with(ctx).load(R.drawable.user_placeholder_correct).placeholder(img.getDrawable()).into(img);
            }

            correo.setText(response.getEmail());
            tlf.setText(String.format("T. %s", response.getPhone()));
        }
    }

    private void getImage(long imageId) {
        if (imageId == 0) {
            return;
        }

        m_imageID = imageId;

        String imageFilePathInCache = ImageUtils.getImageFilePathInCache(getActivity(), imageId);
        if (imageFilePathInCache != null) {
            showImage(imageFilePathInCache);
        } else {
            downloadImage(imageId);
        }
    }

    private void downloadImage(long imageId) {
        @SuppressLint("StaticFieldLeak")
        GetImageTask getImageTask = new GetImageTask(imageId) {
            @Override
            protected void onPostExecute(String imagePath) {
                super.onPostExecute(imagePath);
                showImage(imagePath);
            }
        };
        if (ConnectionUtils.isOnline(getContext())) {
            getImageTask.execute(getActivity());
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void showImage(String imagePath) {
        if (imagePath != null && getActivity() != null) {
            Glide.with(getActivity())
                    .load(ImageUtils.getImageFromCache(getContext(), imagePath))
                    .placeholder(img.getDrawable())
                    .into(img);
        } else {
            showImagePlaceholder();
        }
    }

    private void showImagePlaceholder() {
        final Context ctx = getContext();
        if (ctx == null) {
            return;
        }
        Glide.with(ctx)
                .load(R.drawable.user_placeholder)
                .into(img);
    }
}
