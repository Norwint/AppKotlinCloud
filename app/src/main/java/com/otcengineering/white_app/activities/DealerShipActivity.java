package com.otcengineering.white_app.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.util.List;
import java.util.Objects;

public class DealerShipActivity extends EventActivity {

    ListView lv;
    EditText search;
    String[] array;
    ArrayAdapter<String> arrayAdapter;
    public static String id;
    public static String DealerSelectedID;
    public String[][] DealerListFinal;
    private Location m_location;

    public DealerShipActivity() {
        super("DealerShipActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dealer_ship);

        LocationManager m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getDealerships();
        } else {
            Objects.requireNonNull(m_locationManager).requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    m_location = location;
                    getDealerships();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            }, Looper.myLooper());
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#001e67'>" + getResources().getString(R.string.dealer_ship) + "</font>"));
        lv = findViewById(R.id.listViewDealer);
        search = findViewById(R.id.etSearch);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (arrayAdapter == null) {
                return;
            }
            MySharedPreferences.createLogin(getApplicationContext()).putString("dealership", arrayAdapter.getItem(position));
            finish();
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchedText = editable.toString();
                if (searchedText.isEmpty()) {
                    search.setPadding(240, 0, 100, 0);
                    getDealerships();
                } else {
                    search.setPadding(25, 0, 0, 0);
                    if (searchedText.length() >= 3) {
                        getDealershipsFiltered(searchedText);
                    }
                }
            }
        });
    }

    private void getDealerships() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new Thread(() -> {

                Welcome.Dealerships.Builder dealership = Welcome.Dealerships.newBuilder();

                double latitude, longitude;
                if(m_location != null) {
                    latitude = m_location.getLatitude();
                    longitude = m_location.getLongitude();
                } else {
                    latitude = -6.1746;
                    longitude = 106.8278;
                }
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                dealership.setLatitude(latitude);
                dealership.setLongitude(longitude);
                dealership.setCountryId(msp.getInteger("CountryID"));

                try {
                    Welcome.DealershipsResponse dsr = ApiCaller.doCall(Endpoints.DEALERSHIPS, dealership.build(), Welcome.DealershipsResponse.class);
                    String[][] strings = actionsAfterGetDealership(dsr);
                    DealerListFinal = strings;
                    runOnUiThread(() -> upDateData(strings));
                } catch (ApiCaller.OTCException e) {
                    e.printStackTrace();
                }
            }, "DealershipThread").start();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void getDealershipsFiltered(String name) {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new Thread(() -> {
                Welcome.DealershipName.Builder builder = Welcome.DealershipName.newBuilder();
                builder.setName(name);
                try {
                    Welcome.DealershipsResponse response = ApiCaller.doCall(Endpoints.DEALERSHIPS_BY_NAME, builder.build(), Welcome.DealershipsResponse.class);
                    String[][] strings = actionsAfterGetDealership(response);
                    DealerListFinal = strings;
                    runOnUiThread(() -> upDateData(strings));
                } catch (ApiCaller.OTCException e) {
                    e.printStackTrace();
                }
            }, "DealershipThread2").start();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @NonNull
    private String[][] actionsAfterGetDealership(Welcome.DealershipsResponse dsr) {
        if (dsr == null) return new String[0][0];

        int longitudDealer = dsr.getDealershipsCount() * 2;
        array = new String[longitudDealer];
        List<Welcome.DealershipsResponse.Dealership> lista = dsr.getDealershipsList();
        String[][] dealer = new String[lista.size()][2];
        for (int i = 0; i < lista.size(); ++i) {
            dealer[i][0] = String.valueOf(lista.get(i).getId());
            dealer[i][1] = lista.get(i).getName();
        }

        return dealer;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void upDateData(String[][] data) {
        String[] array = new String[data.length];

        for (int i = 0; i < data.length; ++i) {
            array[i] = data[i][1];
        }

        runOnUiThread(() -> {
            arrayAdapter = new ArrayAdapter<>
                    (this, android.R.layout.simple_list_item_1, array);
            lv.setAdapter(arrayAdapter);
            lv.setOnItemClickListener((parent, view, position, id) -> {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                DealerSelectedID = DealerListFinal[position][0];
                msp.putString("Dealer", DealerListFinal[position][1]);
                msp.putString("DealerID", DealerListFinal[position][0]);
                finish();
            });
        });
    }

}
