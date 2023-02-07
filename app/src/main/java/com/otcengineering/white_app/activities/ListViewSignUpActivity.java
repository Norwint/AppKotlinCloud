package com.otcengineering.white_app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.otc.alice.api.model.Shared;
import com.otc.alice.api.model.Welcome;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.util.Arrays;
import java.util.List;


public class ListViewSignUpActivity extends EventActivity {

    private ListView lv;

    //COUNTRY
    public List<Welcome.CountriesResponse.Country> countryList;
    public static int countrySelectedID;

    //Region
    public List<Welcome.RegionsResponse.Region> regionList;
    public static int regionSelectedID;

    public ListViewSignUpActivity() {
        super("ListViewSignUpActivity");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_sign_up);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        Bundle dif = getIntent().getExtras();

        if (dif.getInt("num") == 2) {
            getSupportActionBar().setTitle(getString(R.string.country));

            GenericTask gt = new GenericTask(Endpoints.GET_COUNTRIES, null, false, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    Welcome.CountriesResponse rr = otcResponse.getData().unpack(Welcome.CountriesResponse.class);
                    countryList = rr.getCountriesList();
                    List<String> str = Stream.of(rr.getCountriesList()).map(Welcome.CountriesResponse.Country::getName).collect(Collectors.toList());
                    updateData(str);
                }
            });
            gt.execute();
        } else if (dif.getInt("num") == 3) {
            getSupportActionBar().setTitle(getString(R.string.region_text));

            Welcome.Regions ua = Welcome.Regions.newBuilder()
                    .setCountryId(MySharedPreferences.createLogin(getApplicationContext()).getInteger("CountryID"))
                    .build();
            GenericTask gt = new GenericTask(Endpoints.REGIONS, ua, false, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    Welcome.RegionsResponse rr = otcResponse.getData().unpack(Welcome.RegionsResponse.class);
                    regionList = rr.getRegionsList();
                    List<String> str = Stream.of(rr.getRegionsList())
                            .map(Welcome.RegionsResponse.Region::getName)
                            .sortBy(s -> s)
                            .collect(Collectors.toList());
                    updateData(str);
                }
            });
            gt.execute();
        } else if (dif.getInt("num") == 5) {
            getSupportActionBar().setTitle(getString(R.string.blood_type));
            String[] array = getResources().getStringArray(R.array.blood_array);
            updateData(Arrays.asList(array));
        } else if (dif.getInt("num") == 4) {
            getSupportActionBar().setTitle(getString(R.string.sex));
            String[] array = getResources().getStringArray(R.array.sex_array);
            updateData(Arrays.asList(array));
        }
    }

    public void updateData(List<String> names) {
        Bundle dif = getIntent().getExtras();
        lv = findViewById(R.id.listViewSignUp);
        metodoArray(names, dif.getInt("num"));
    }

    private void metodoArray(List<String> array, final int num) {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, array);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            String data = (String) parent.getItemAtPosition(position);
            MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
            if (num == 0) {
                msp.putString("Grade", data);
            } else if (num == 1) {
                msp.putString("Car", data + "/");
            } else if (num == 2) {
                countrySelectedID = countryList.get(position).getId();
                msp.putInteger("CountryID", countryList.get(position).getId());
                msp.putString("Country", countryList.get(position).getName());
            } else if (num == 3) {
                Welcome.RegionsResponse.Region region = Stream.of(regionList)
                        .filter(r -> r.getName().equals(data)).single();
                regionSelectedID = region.getId();
                msp.putInteger("RegionID", region.getId());
                msp.putString("Region", region.getName());
            } else if (num == 4) {
                if (position == 0) {
                    msp.putString("Sex", "Male");
                } else {
                    msp.putString("Sex", "Female");
                }
            } else if (num == 5) {
                msp.putInteger("BloodType", position);
            }

            finish();
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;

    }
}