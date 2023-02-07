package com.otcengineering.white_app.payment.activity;

import androidx.annotation.Nullable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nonnull;

public class ShippingSelectionActivity extends BaseActivity {
    private EditText mShippingAddress, mShippingCity, mShippingPC, mShippingName;
    private Spinner mShippingRegion, mShippingCountry;
    private int mRegionID, mCountryID;
    private TitleBar mTitleBar;
    private ProgressDialog mProgressDialog;

    private SparseArray<String> mIndianRegions, mIndoRegions, mCCAA;

    public ShippingSelectionActivity() {
        super("ShippingSelectionActivity");
        mIndianRegions = new SparseArray<>();
        mIndoRegions = new SparseArray<>();
        mCCAA = new SparseArray<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_selection);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        getViews();
        setEvents();
        fillViews();
        getInfo();
    }

    private void setEvents() {
        mShippingCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setRegion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getRegions() {
        for (int i = 0; i < 3; ++i) {
            Welcome.Regions regs = Welcome.Regions.newBuilder().setCountryId(i + 1).build();
            int finalI = i + 1;
            new TypedTask<>(Endpoints.REGIONS, regs, false, Welcome.RegionsResponse.class, new TypedCallback<Welcome.RegionsResponse>() {
                @Override
                public void onSuccess(@Nonnull Welcome.RegionsResponse value) {
                    switch (finalI) {
                        case 1:
                            for (Welcome.RegionsResponse.Region reg : value.getRegionsList()) {
                                mIndianRegions.append(reg.getId(), reg.getName());
                            }
                            break;
                        case 2:
                            for (Welcome.RegionsResponse.Region reg : value.getRegionsList()) {
                                mIndoRegions.append(reg.getId(), reg.getName());
                            }
                            break;
                        case 3:
                            for (Welcome.RegionsResponse.Region reg : value.getRegionsList()) {
                                mCCAA.append(reg.getId(), reg.getName());
                            }
                            break;
                    }
                    if (finalI == mCountryID) {
                        setRegion();

                        SparseArray<String> regions;

                        switch (mCountryID) {
                            case 1: regions = mIndianRegions; break;
                            case 2: regions = mIndoRegions; break;
                            case 3: regions = mCCAA; break;
                            default: regions = null; break;
                        }
                        mShippingRegion.setSelection(regions.indexOfKey(mRegionID), true);
                        mProgressDialog.dismiss();
                    }
                }

                @Override
                public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {

                }
            }).execute();
        }
    }

    private void getInfo() {
        TypedTask<ProfileAndSettings.UserDataResponse> getUserProfile = new TypedTask<>(Endpoints.USER_INFO, null, true, ProfileAndSettings.UserDataResponse.class,
                new TypedCallback<ProfileAndSettings.UserDataResponse>() {
                    @Override
                    public void onSuccess(@Nonnull ProfileAndSettings.UserDataResponse value) {
                        mRegionID = value.getRegion();
                        mCountryID = value.getCountryId();

                        mShippingAddress.setText(value.getAddress());
                        mShippingCity.setText(value.getCity());
                        mShippingPC.setText(value.getPostalCode());
                        mShippingName.setText(value.getName());

                        mShippingCountry.setAdapter(Utils.createSpinnerAdapter(ShippingSelectionActivity.this, new String[]{"India", "Indonesia", "Spain"}));
                        mShippingCountry.setSelection(value.getCountryId() - 1);

                        getRegions();
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {

                    }
                });
        getUserProfile.execute();
    }

    private void fillViews() {
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
    }

    private void setRegion() {
        String cnt = (String) mShippingCountry.getSelectedItem();
        switch (cnt) {
            case "India": {
                mShippingRegion.setAdapter(Utils.createSpinnerAdapter(this, getRegionsOfMap(mIndianRegions)));
            }
            break;
            case "Indonesia": {
                mShippingRegion.setAdapter(Utils.createSpinnerAdapter(this, getRegionsOfMap(mIndoRegions)));
            }
            break;
            case "Spain": {
                mShippingRegion.setAdapter(Utils.createSpinnerAdapter(this, getRegionsOfMap(mCCAA)));
            }
            break;
        }
    }

    private String[] getRegionsOfMap(SparseArray<String> map) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < map.size(); ++i) {
            int key = map.keyAt(i);
            list.add(map.get(key));
        }
        return list.toArray(new String[]{});
    }

    private void getViews() {
        mShippingAddress = findViewById(R.id.shippingAddress);
        mShippingCity = findViewById(R.id.shippingCity);
        mShippingPC = findViewById(R.id.shippingPC);
        mShippingName = findViewById(R.id.shippingName);
        mShippingRegion = findViewById(R.id.shippingRegion);
        mShippingCountry = findViewById(R.id.shippingCountry);
        mTitleBar = findViewById(R.id.titleBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    public void sendShippingOptions(View view) {
        HashMap<String, String> map = new HashMap<>();

        map.put("name", mShippingName.getText().toString());
        map.put("address", mShippingAddress.getText().toString());
        map.put("postal_code", mShippingPC.getText().toString());
        map.put("city", mShippingCity.getText().toString());
        map.put("region", (String) mShippingRegion.getSelectedItem());
        map.put("country", (String) mShippingCountry.getSelectedItem());

        String json = Utils.getGson().toJson(map);

        Intent intent = new Intent(this, PaymentSelectorActivity.class);
        intent.putExtra("shipping_options", json);

        startActivityForResult(intent, Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY);
    }
}
