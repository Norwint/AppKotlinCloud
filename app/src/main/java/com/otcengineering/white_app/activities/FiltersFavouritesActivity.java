package com.otcengineering.white_app.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;


import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.utils.ApiCaller;

import java.util.Locale;

public class FiltersFavouritesActivity extends EventActivity {
    private Switch filter1, filter2, filter3, filter4, filter5, filter6;

    public FiltersFavouritesActivity() {
        super("FiltersFavouritesActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters_favourites);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.my_drive_icons_1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#001e67'>Filter</font>"));

        filter1 = findViewById(R.id.filter1);
        filter2 = findViewById(R.id.filter2);
        filter3 = findViewById(R.id.filter3);
        filter4 = findViewById(R.id.filter4);
        filter5 = findViewById(R.id.filter5);
        filter6 = findViewById(R.id.filter6);

        Button saveButton = findViewById(R.id.saveFilters);

        MySharedPreferences filters = MySharedPreferences.createFilter(getApplicationContext());

        if (!filters.contains("filters")) {
            filters.putInteger("filters", 0b111111);
        } else {
            int filter = filters.getInteger("filters");

            filter1.setChecked((filter &  1) == 1 );
            filter2.setChecked((filter &  2) == 2 );
            filter3.setChecked((filter &  4) == 4 );
            filter4.setChecked((filter &  8) == 8 );
            filter5.setChecked((filter & 16) == 16);
            filter6.setChecked((filter & 32) == 32);
        }

        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new getNumberFilters().execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }


        saveButton.setOnClickListener(v -> {
            int filt = 0;
            filt |= filter1.isChecked() ? 1  : 0;
            filt |= filter2.isChecked() ? 2  : 0;
            filt |= filter3.isChecked() ? 4  : 0;
            filt |= filter4.isChecked() ? 8  : 0;
            filt |= filter5.isChecked() ? 16 : 0;
            filt |= filter6.isChecked() ? 32 : 0;
            filters.putInteger("filters", filt);
            finish();
        });
    }

    private class getNumberFilters extends AsyncTask<String, Void, MyTrip.RoutesFilter> {

        @Override
        protected MyTrip.RoutesFilter doInBackground(String... strings) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                return ApiCaller.doCall(Endpoints.GET_ROUTES_FILTER, msp.getBytes("token"), null, MyTrip.RoutesFilter.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MyTrip.RoutesFilter response) {
            if (response != null) {
                super.onPostExecute(response);

                filter1.setText(String.format(Locale.US, "%d", response.getMyPlanned()));
                filter2.setText(String.format(Locale.US, "%d", response.getMyDone()));

                filter3.setText(String.format(Locale.US, "%d", response.getFriendsPlanned()));
                filter4.setText(String.format(Locale.US, "%d", response.getFriendsDone()));

                filter5.setText(String.format(Locale.US, "%d", response.getCommunityPlanned()));
                filter6.setText(String.format(Locale.US, "%d", response.getCommunityDone()));
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;

    }
}
