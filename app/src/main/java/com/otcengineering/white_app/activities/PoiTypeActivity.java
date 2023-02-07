package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.PoiTypeAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyTrip;
import com.otcengineering.white_app.network.utils.ApiCaller;

import java.util.List;

public class PoiTypeActivity extends BaseActivity {
    private TitleBar titleBar;
    private RecyclerView recyclerView;

    private PoiTypeAdapter adapter;

    private GetPoiTypesTask getPoiTypesTask;

    public PoiTypeActivity() {
        super("PoiTypeActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_type);
        retrieveViews();
        setEvents();
        configureAdapter();
        getPoiTypes();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.poi_type_titleBar);
        recyclerView = findViewById(R.id.poi_type_recyclerView);
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
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PoiTypeAdapter(this, position -> {
            General.PoiType poiType = adapter.getItem(position);
            Intent data = new Intent();
            data.putExtra(Constants.Extras.POI_TYPE, poiType);
            setResult(RESULT_OK, data);
            finish();
        });
        recyclerView.setAdapter(adapter);
    }

    private void getPoiTypes() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            getPoiTypesTask = new GetPoiTypesTask();
            getPoiTypesTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetPoiTypesTask extends AsyncTask<Void, Void, List<General.PoiType>> {
        private ProgressDialog progressDialog = new ProgressDialog(PoiTypeActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected List<General.PoiType> doInBackground(Void... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
                MyTrip.PoiTypesResponse response = ApiCaller.doCall(Endpoints.GET_POI_TYPES, msp.getBytes("token"), null, MyTrip.PoiTypesResponse.class);
                return response.getTypesList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<General.PoiType> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                showPoiTypeList(result);
            }
        }
    }

    private void showPoiTypeList(List<General.PoiType> poiTypeList) {
        List<General.PoiType> po = Stream.of(poiTypeList).sortBy(General.PoiType::getNumber).toList();
        adapter.clearItems();
        adapter.addItems(po);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getPoiTypesTask != null) {
            getPoiTypesTask.cancel(true);
        }
    }
}
