package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyDrive;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.ChartItemAdapter;
import com.otcengineering.white_app.components.ChartBarView;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.SwipeLinearLayout;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.EcoItem;
import com.otcengineering.white_app.serialization.pojo.MileageItem;
import com.otcengineering.white_app.serialization.pojo.SafetyItem;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalAdjusters;
import org.threeten.bp.temporal.WeekFields;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class ChartActivity extends EventActivity {

    private static final int DOTTED_LINE_HEIGHT = 2;

    public static final int DAILY = Constants.TimeType.DAILY;
    public static final int WEEKLY = Constants.TimeType.WEEKLY;
    public static final int MONTHLY = Constants.TimeType.MONTHLY;

    private TitleBar titleBar;
    private CustomTabLayout customTabLayout;
    private ImageView btnPrev, btnNext;
    private TextView txtTotalValue, txtDate;
    private RecyclerView recycler;
    private FrameLayout btnScrollUp;
    private LinearLayout layoutDottedLines;
    private SwipeLinearLayout layoutChart;

    private ChartItemAdapter adapter;

    private List<ChartBarView> chartBarViews;

    private int chartType = Constants.ChartMode.MILEAGE;
    private int tabSelected = DAILY;
    private LocalDate dateStart;

    private int numRepeats = 0;
    private ProgressDialog progressDialog;

    private double maxDistance;
    private double maxConsumption;
    private double maxDrivingTechnique;

    private Community.UserCommunity user;

    public ChartActivity() {
        super("ChartActivity");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(ChartActivity.this);

        setContentView(R.layout.activity_chart);
        retrieveExtras();
        retrieveViews();
        setEvents();
        configureTitle();
        configureAdapter();
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            chartType = getIntent().getExtras().getInt(Constants.Extras.CHART_MODE, Constants.ChartMode.MILEAGE);
            tabSelected = getIntent().getExtras().getInt(Constants.Extras.TIME_TYPE, Constants.TimeType.DAILY);
            user = (Community.UserCommunity) getIntent().getExtras().getSerializable(Constants.Extras.USER);
        }
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.chart_titleBar);
        customTabLayout = findViewById(R.id.chart_customTabLayout);
        btnPrev = findViewById(R.id.chart_btnPrev);
        btnNext = findViewById(R.id.chart_btnNext);
        txtTotalValue = findViewById(R.id.chart_txtTotalValue);
        txtDate = findViewById(R.id.chart_txtDate);
        recycler = findViewById(R.id.chart_recycler);
        btnScrollUp = findViewById(R.id.chart_btnScrollUp);
        layoutDottedLines = findViewById(R.id.chart_layoutDottedLines);
        layoutChart = findViewById(R.id.chart_layoutChart);
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
                Intent intent = new Intent(getApplicationContext(), TipsActivity.class);
                intent.putExtra("title", titleBar.getTitle());
                intent.putExtra("type", chartType);
                startActivity(intent);
            }
        });

        customTabLayout.configure(tabSelected, this::manageTabChanged, DAILY, WEEKLY, MONTHLY);

        layoutChart.setOnSwipeListener(new SwipeLinearLayout.OnSwipeListener() {
            @Override
            public void onSwipeLeft() {
                btnNext.performClick();
            }

            @Override
            public void onSwipeRight() {
                btnPrev.performClick();
            }
        });

        btnPrev.setOnClickListener(v -> {
            moveDateStartToPrevious();
            getChartInfo(1);
        });

        btnNext.setOnClickListener(v -> {
            moveDateStartToNext();
            getChartInfo(2);
        });

        btnScrollUp.setOnClickListener(view -> recycler.smoothScrollToPosition(0));
    }

    private void configureTitle() {

        titleBar.showImgRight2();

        switch (chartType) {
            case Constants.ChartMode.MILEAGE:
                titleBar.setTitle(R.string.mileage);
                break;
            case Constants.ChartMode.ECO:
                titleBar.setTitle(R.string.eco_driving);
                break;
            case Constants.ChartMode.SAFETY:
                titleBar.setTitle(R.string.safety_driving);
                break;
        }
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new ChartItemAdapter(this, chartType, new ChartItemAdapter.ChartItemListener() {
            @Override
            public void onShare(int position) {
                Intent intent = new Intent(getApplicationContext(), SendPostActivity.class);

                Object chartItem = adapter.getItem(position);
                if (chartItem instanceof MileageItem) {
                    intent.putExtra(Constants.Extras.MILEAGE_ITEM, (MileageItem) chartItem);
                } else if (chartItem instanceof EcoItem) {
                    intent.putExtra(Constants.Extras.ECO_ITEM, (EcoItem) chartItem);
                } else if (chartItem instanceof SafetyItem) {
                    intent.putExtra(Constants.Extras.SAFETY_ITEM, (SafetyItem) chartItem);
                }

                startActivity(intent);
            }

            @Override
            public void onMileageSelected(int position) {
                markBarAsSelected(position);
            }
        });
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                btnScrollUp.setVisibility(dy > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void markBarAsSelected(int position) {
        for (int i = 0; i < chartBarViews.size(); i++) {
            chartBarViews.get(i).setHighlighted(i == position);
        }
    }

    private void manageTabChanged(int tabSelected) {
        this.tabSelected = tabSelected;
        initializeDateStart();
        getChartInfo(0);
    }

    private void initializeDateStart() {
        LocalDate date = LocalDate.now();
        if (tabSelected == DAILY) {
            date = date.with(WeekFields.of(Locale.US).dayOfWeek(), 1);
        } else if (tabSelected == WEEKLY) {
            date = date.with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY)).minus(1, ChronoUnit.WEEKS);
        } else if (tabSelected == MONTHLY) {
            date = date.withDayOfMonth(1);
            date = date.withMonth(date.getMonthValue() > 6 ? 7 : 1);
        }
        dateStart = date;
    }

    private void moveCalendarToTheFirstSundayOfTheWeek(Calendar calendar) {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { //search the previous sunday
            calendar.add(Calendar.DATE, -1);
        }
    }

    private void moveDateStartToNext() {
        if (tabSelected == DAILY) {
            dateStart = dateStart.plus(1, ChronoUnit.WEEKS);
        } else if (tabSelected == WEEKLY) {
            dateStart = dateStart.plus(1, ChronoUnit.WEEKS);
            /*calendar.add(Calendar.DATE, 7);
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                calendar.add(Calendar.DATE, -1);
            }*/
        } else if (tabSelected == MONTHLY) {
            dateStart = dateStart.plus(6, ChronoUnit.MONTHS);
        }
    }

    private void moveDateStartToPrevious() {
        if (tabSelected == DAILY) {
            dateStart = dateStart.minus(1, ChronoUnit.WEEKS);
        } else if (tabSelected == WEEKLY) {
            dateStart = dateStart.minus(1, ChronoUnit.WEEKS);
        } else if (tabSelected == MONTHLY) {
            dateStart = dateStart.minus(6, ChronoUnit.MONTHS);
        }
    }

    private void moveCalendarToFirstSundayOfTheMonth(Calendar calendar) {
        calendar.set(Calendar.DATE, 1);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { //search the 1st sunday of the month
            calendar.add(Calendar.DATE, 1);
        }
    }

    private boolean isMyUser() {
        long myUserId = PrefsManager.getInstance().getMyUserId(this);
        return user == null || user.getUserId() == myUserId;
    }

    private void getChartInfo(int direction) {
        switch (chartType) {
            case Constants.ChartMode.MILEAGE:
                if (isMyUser()) {
                    getMileageInfo(Endpoints.USER_MILEAGE, direction);
                } else {
                    getMileageInfo(Endpoints.GET_USER_MILEAGE, direction);
                }
                break;
            case Constants.ChartMode.ECO:
                if (isMyUser()) {
                    getEcoInfo(Endpoints.USER_ECO, direction);
                } else {
                    getEcoInfo(Endpoints.GET_USER_ECO, direction);
                }
                break;
            case Constants.ChartMode.SAFETY:
                if (isMyUser()) {
                    getSafetyInfo(Endpoints.USER_SAFETY, direction);
                } else {
                    getSafetyInfo(Endpoints.GET_USER_SAFETY, direction);
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getMileageInfo(final String endpoint, final int dir) {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new AsyncTask<Void, Void, MyDrive.UserMileageResponse>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    if (numRepeats == 0) {
                        progressDialog.setMessage(getString(R.string.loading));
                        progressDialog.show();
                    }
                }

                @Override
                protected MyDrive.UserMileageResponse doInBackground(Void... voids) {
                    try {
                        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                        MyDrive.UserMileageResponse response;

                        if (isMyUser()) {
                            MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();
                            General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
                            builder.setTypeTime(typeTime);
                            String dateStartString = DateUtils.dateToString(dateStart, DateUtils.FMT_SRV_DATE);
                            builder.setDateStart(dateStartString);

                            response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), MyDrive.UserMileageResponse.class);
                            PrefsManager.getInstance().saveMileage(response, typeTime, getApplicationContext());

                            return response;
                        } else {
                            Community.UserDrive.Builder builder = Community.UserDrive.newBuilder();
                            General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
                            builder.setTypeTime(typeTime);
                            String dateStartString = DateUtils.dateToString(dateStart, DateUtils.FMT_SRV_DATE);
                            builder.setDateStart(dateStartString);
                            builder.setUserId(user.getUserId());

                            response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), MyDrive.UserMileageResponse.class);
                            return response;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(MyDrive.UserMileageResponse response) {
                    super.onPostExecute(response);
                    actionsAfterGetMileage(response, dir);
                }
            }.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            getMileageInfoFromDb(dir);
        }
    }

    private void getMileageInfoFromDb(int direction) {
        MyDrive.UserMileageResponse mileage = PrefsManager.getInstance().getMileage(Constants.TimeType.fromIntToTimeType(tabSelected), this);
        actionsAfterGetMileage(mileage, direction);
    }

    private List<MileageItem> createMileageItems(MyDrive.UserMileageResponse response) {
        List<MyDrive.UserMileageResponse.Mileage> mileageList = response.getMileageList();
        List<MileageItem> mileageItems = new ArrayList<>();
        for (MyDrive.UserMileageResponse.Mileage mileage : mileageList) {
            MileageItem mileageItem = new MileageItem(
                    mileage.getDate(),
                    mileage.getDistance(),
                    mileage.getDuration(),
                    mileage.getGlobalRanking(),
                    mileage.getLocalRanking(),
                    mileage.getFragmentsList());
            mileageItems.add(mileageItem);
        }
        return mileageItems;
    }

    private void actionsAfterGetMileage(MyDrive.UserMileageResponse response, int dir) {
        if (response == null) return;

        List<MileageItem> mileageItems = createMileageItems(response);
        General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
        if ((mileageItems.size() == 0) && (numRepeats > 4)) {
            progressDialog.dismiss();
            updateAndShowTotalAndMaxDistance(mileageItems);
            showDatesInterval();
            showMileageChart(mileageItems);
            showMileageList(mileageItems);
        } else if ((mileageItems.size() == 0) && (dir == 1)) {
            numRepeats++;
            moveDateStartToNext();
            getChartInfo(2);
        } else if ((mileageItems.size() == 0) && (dir == 2)) {
            numRepeats++;
            moveDateStartToPrevious();
            getChartInfo(1);
        } else if ((mileageItems.size() == 0) && (typeTime == General.TimeType.MONTHLY)) {
            numRepeats++;
            moveDateStartToNext();
            getChartInfo(2);
        } else if ((mileageItems.size() == 0) && (dir == 0)) {
            numRepeats++;
            moveDateStartToPrevious();
            getChartInfo(1);
        } else {
            numRepeats = 0;
            progressDialog.dismiss();
            updateAndShowTotalAndMaxDistance(mileageItems);
            showDatesInterval();
            showMileageChart(mileageItems);
            showMileageList(mileageItems);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getEcoInfo(final String endpoint, final int dir) {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new AsyncTask<Void, Void, MyDrive.UserEcoResponse>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (numRepeats == 0) {
                        progressDialog.setMessage(getString(R.string.loading));
                        progressDialog.show();
                    }
                }

                @Override
                protected MyDrive.UserEcoResponse doInBackground(Void... voids) {
                    try {
                        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                        MyDrive.UserEcoResponse response;

                        if (isMyUser()) {
                            MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();
                            General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
                            builder.setTypeTime(typeTime);
                            String dateStartString = DateUtils.dateToString(dateStart, DateUtils.FMT_SRV_DATE);
                            builder.setDateStart(dateStartString);

                            response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), MyDrive.UserEcoResponse.class);
                            PrefsManager.getInstance().saveEco(response, typeTime, getApplicationContext());

                            return response;
                        } else {
                            Community.UserDrive.Builder builder = Community.UserDrive.newBuilder();
                            General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
                            builder.setTypeTime(typeTime);
                            String dateStartString = DateUtils.dateToString(dateStart, DateUtils.FMT_SRV_DATE);
                            builder.setDateStart(dateStartString);
                            builder.setUserId(user.getUserId());

                            response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), MyDrive.UserEcoResponse.class);
                            return response;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(MyDrive.UserEcoResponse response) {
                    super.onPostExecute(response);
                    actionsAfterGetEco(response, dir);
                }
            }.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            getEcoInfoFromDb(dir);
        }
    }

    private void getEcoInfoFromDb(int direction) {
        MyDrive.UserEcoResponse eco = PrefsManager.getInstance().getEco(Constants.TimeType.fromIntToTimeType(tabSelected), this);
        actionsAfterGetEco(eco, direction);
    }

    private List<EcoItem> createEcoItems(MyDrive.UserEcoResponse response) {
        List<MyDrive.UserEcoResponse.Eco> ecoList = response.getEcoList();
        List<EcoItem> ecoItems = new ArrayList<>();
        for (MyDrive.UserEcoResponse.Eco eco : ecoList) {
            EcoItem ecoItem = new EcoItem(
                    eco.getDate(),
                    eco.getTotalConsumption(),
                    eco.getAverageConsumption(),
                    eco.getDuration(),
                    eco.getGlobalRanking(),
                    eco.getLocalRanking(),
                    eco.getFragmentsList());
            ecoItems.add(ecoItem);
        }
        return ecoItems;
    }

    private void actionsAfterGetEco(MyDrive.UserEcoResponse response, int dir) {
        if (response == null) return;

        List<EcoItem> ecoItems = createEcoItems(response);
        General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
        if ((ecoItems.size() == 0) && (numRepeats > 4)) {
            progressDialog.dismiss();
            updateAndShowTotalAndMaxConsumption(ecoItems);
            showDatesInterval();
            showEcoChart(ecoItems);
            showEcoList(ecoItems);
        } else if ((ecoItems.size() == 0) && (dir == 1)) {
            numRepeats++;
            moveDateStartToNext();
            getChartInfo(2);
        } else if ((ecoItems.size() == 0) && (dir == 2)) {
            numRepeats++;
            moveDateStartToPrevious();
            getChartInfo(1);
        } else if ((ecoItems.size() == 0) && (typeTime == General.TimeType.MONTHLY)) {
            numRepeats++;
            moveDateStartToNext();
            getChartInfo(2);
        } else if ((ecoItems.size() == 0) && (dir == 0)) {
            numRepeats++;
            moveDateStartToPrevious();
            getChartInfo(1);
        } else {
            numRepeats = 0;
            progressDialog.dismiss();
            updateAndShowTotalAndMaxConsumption(ecoItems);
            showDatesInterval();
            showEcoChart(ecoItems);
            showEcoList(ecoItems);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getSafetyInfo(final String endpoint, final int dir) {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new AsyncTask<Void, Void, MyDrive.UserSafetyResponse>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (numRepeats == 0) {
                        progressDialog.setMessage(getString(R.string.loading));
                        progressDialog.show();
                    }
                }

                @Override
                protected MyDrive.UserSafetyResponse doInBackground(Void... voids) {
                    try {
                        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                        MyDrive.UserSafetyResponse response;

                        if (isMyUser()) {
                            MyDrive.Summary.Builder builder = MyDrive.Summary.newBuilder();
                            General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
                            builder.setTypeTime(typeTime);
                            String dateStartString = DateUtils.dateToString(dateStart, DateUtils.FMT_SRV_DATE);
                            builder.setDateStart(dateStartString);

                            response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), MyDrive.UserSafetyResponse.class);
                            PrefsManager.getInstance().saveSafety(response, typeTime, getApplicationContext());

                            return response;
                        } else {
                            Community.UserDrive.Builder builder = Community.UserDrive.newBuilder();
                            General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
                            builder.setTypeTime(typeTime);
                            String dateStartString = DateUtils.dateToString(dateStart, DateUtils.FMT_SRV_DATE);
                            builder.setDateStart(dateStartString);
                            builder.setUserId(user.getUserId());

                            response = ApiCaller.doCall(endpoint, msp.getBytes("token"), builder.build(), MyDrive.UserSafetyResponse.class);
                            return response;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(MyDrive.UserSafetyResponse safetyItems) {
                    super.onPostExecute(safetyItems);
                    actionsAfterGetSafety(safetyItems, dir);
                }
            }.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            getSafetyInfoFromDb(dir);
        }
    }

    private void getSafetyInfoFromDb(int direction) {
        MyDrive.UserSafetyResponse safety = PrefsManager.getInstance().getSafety(Constants.TimeType.fromIntToTimeType(tabSelected), this);
        actionsAfterGetSafety(safety, direction);
    }

    private List<SafetyItem> createSafetyItems(MyDrive.UserSafetyResponse response) {
        List<MyDrive.UserSafetyResponse.Safety> safetyList = response.getSafetyList();
        List<SafetyItem> safetyItems = new ArrayList<>();
        for (MyDrive.UserSafetyResponse.Safety safety : safetyList) {
            SafetyItem safetyItem = new SafetyItem(
                    safety.getDate(),
                    safety.getDrivingTechnique() / 10,
                    safety.getDuration());
            safetyItems.add(safetyItem);
        }
        return safetyItems;
    }

    private void actionsAfterGetSafety(MyDrive.UserSafetyResponse response, int dir) {
        if (response == null) return;

        List<SafetyItem> safetyItems = createSafetyItems(response);
        General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);
        if ((safetyItems.size() == 0) && (numRepeats > 4)) {
            progressDialog.dismiss();
            updateAndShowTotalAndMaxDrivingTechinque(safetyItems);
            showDatesInterval();
            showSafetyChart(safetyItems);
            showSafetyList(safetyItems);
            progressDialog.dismiss();
        } else if ((safetyItems.size() == 0) && (dir == 1)) {
            numRepeats++;
            moveDateStartToNext();
            getChartInfo(2);
        } else if ((safetyItems.size() == 0) && (dir == 2)) {
            numRepeats++;
            moveDateStartToPrevious();
            getChartInfo(1);
        } else if ((safetyItems.size() == 0) && (typeTime == General.TimeType.MONTHLY)) {
            numRepeats++;
            moveDateStartToNext();
            getChartInfo(2);
        } else if ((safetyItems.size() == 0) && (dir == 0)) {
            numRepeats++;
            moveDateStartToPrevious();
            getChartInfo(1);
        } else {
            numRepeats = 0;
            progressDialog.dismiss();
            updateAndShowTotalAndMaxDrivingTechinque(safetyItems);
            showDatesInterval();
            showSafetyChart(safetyItems);
            showSafetyList(safetyItems);
        }
    }

    private void updateAndShowTotalAndMaxDistance(List<MileageItem> mileageItems) {
        maxDistance = 0;
        double totalDistance = 0;
        for (MileageItem mileageItem : mileageItems) {
            if (mileageItem.getDistance() > maxDistance) {
                maxDistance = mileageItem.getDistance();
            }
            totalDistance += mileageItem.getDistance();
        }
        txtTotalValue.setText(String.format(Locale.US, "%.1f km", totalDistance));
    }

    private void updateAndShowTotalAndMaxConsumption(List<EcoItem> ecoItems) {
        maxConsumption = 0;
        double totalConsumption = 0;
        for (EcoItem ecoItem : ecoItems) {
            if (ecoItem.getTotalConsumption() / 100.0f > maxConsumption) {
                maxConsumption = ecoItem.getTotalConsumption() / 100.0f;
            }
            totalConsumption += ecoItem.getTotalConsumption() / 100.0f;
        }
        txtTotalValue.setText(String.format(Locale.US, "%.1f l.", totalConsumption));
    }

    private void updateAndShowTotalAndMaxDrivingTechinque(List<SafetyItem> safetyItems) {
        maxDrivingTechnique = 0;
        double totalDrivingTechnique = 0;
        int totalMinutes = 0;
        int minutes;
        double dt;
        double result;
        for (SafetyItem safetyItem : safetyItems) {
            if (safetyItem.getDrivingTechinique() > maxDrivingTechnique) {
                maxDrivingTechnique = safetyItem.getDrivingTechinique();
            }
            minutes = (Integer.parseInt(safetyItem.getDuration().split(":")[0]) * 60) + Integer.parseInt(safetyItem.getDuration().split(":")[1]);
            if (minutes == 0 && safetyItem.getDrivingTechinique() > 0) minutes = 1;
            dt = safetyItem.getDrivingTechinique() * minutes;
            totalMinutes += minutes;
            totalDrivingTechnique += dt;
        }
        if (totalDrivingTechnique > 0 && totalMinutes > 0)
            result = totalDrivingTechnique / totalMinutes;
        else result = 0;
        txtTotalValue.setText(String.format(Locale.US, "%.1f", result));
    }

    private LocalDate calculateDateEnd() {
        LocalDate end = dateStart;
        if (tabSelected == DAILY) {
            end = dateStart.plus(6, ChronoUnit.DAYS);
        } else if (tabSelected == WEEKLY) {
            end = dateStart.plus(27, ChronoUnit.DAYS);
        } else if (tabSelected == MONTHLY) {
            end = dateStart.plus(6, ChronoUnit.MONTHS).minus(1, ChronoUnit.DAYS);
        }
        return end;
    }

    private void showDatesInterval() {
        LocalDate dateEnd = calculateDateEnd();
        txtDate.setText(String.format(getString(R.string.total) + ": %s - %s",
                DateUtils.dateToString(dateStart, "dd MMM yyyy"),
                DateUtils.dateToString(dateEnd, "dd MMM yyyy")));
    }

    private void showMileageChart(List<MileageItem> mileageItems) {
        drawDottedLines();
        drawMileageChartBars(mileageItems);
    }

    private void showEcoChart(List<EcoItem> ecoItems) {
        drawDottedLines();
        drawEcoChartBars(ecoItems);
    }

    private void showSafetyChart(List<SafetyItem> safetyItems) {
        drawDottedLines();
        drawSafetyChartBars(safetyItems);
    }

    private void drawMileageChartBars(List<MileageItem> mileageItems) {
        chartBarViews = new ArrayList<>();
        layoutChart.removeAllViews();
        int chartBarViewWidth = mileageItems.isEmpty() ? 0 : layoutChart.getMeasuredWidth() / mileageItems.size();
        for (int i = 0; i < mileageItems.size(); i++) {
            MileageItem mileageItem = mileageItems.get(i);
            drawChartBar(chartBarViewWidth, i, mileageItem.getDate(), mileageItem.getFragments(),
                    mileageItem.getDistance(), maxDistance);
        }
    }

    private void drawEcoChartBars(List<EcoItem> ecoItems) {
        chartBarViews = new ArrayList<>();
        layoutChart.removeAllViews();
        int chartBarViewWidth = ecoItems.isEmpty() ? 0 : layoutChart.getMeasuredWidth() / ecoItems.size();
        for (int i = 0; i < ecoItems.size(); i++) {
            EcoItem ecoItem = ecoItems.get(i);
            drawChartBar(chartBarViewWidth, i, ecoItem.getDate(), ecoItem.getFragments(),
                    ecoItem.getTotalConsumption() / 100, maxConsumption);
        }
    }

    private void drawSafetyChartBars(List<SafetyItem> safetyItems) {
        chartBarViews = new ArrayList<>();
        layoutChart.removeAllViews();
        int chartBarViewWidth = safetyItems.isEmpty() ? 0 : layoutChart.getMeasuredWidth() / safetyItems.size();
        for (int i = 0; i < safetyItems.size(); i++) {
            SafetyItem safetyItem = safetyItems.get(i);
            drawChartBar(chartBarViewWidth, i, safetyItem.getDate(), null,
                    safetyItem.getDrivingTechinique(), maxDrivingTechnique);
        }
    }

    private void drawChartBar(int chartBarViewWidth, final int position, LocalDate dateStart, List<Double> fragments, double value, double maxValue) {
        FrameLayout frameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(chartBarViewWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(params);
        ChartBarView chartBarView = new ChartBarView(
                ChartActivity.this,
                tabSelected,
                dateStart,
                fragments,
                value,
                maxValue);
        chartBarView.setOnClickListener(v -> {
            markBarAsSelected(position);
            recycler.smoothScrollToPosition(position);
            adapter.setRowSelected(position);
        });
        chartBarViews.add(chartBarView);
        frameLayout.addView(chartBarView);
        layoutChart.addView(frameLayout);
    }

    private void drawDottedLines() {
        int layoutDottedLinesHeight = layoutDottedLines.getMeasuredHeight();
        layoutDottedLines.removeAllViews();
        int numLines = 7;

        int dottedLineHeight = (int) (DOTTED_LINE_HEIGHT * getResources().getDisplayMetrics().density);
        int margin = (layoutDottedLinesHeight - numLines * dottedLineHeight) / (numLines - 1);
        for (int i = 0; i < numLines; i++) {
            View view = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dottedLineHeight);
            if (i != 0) {
                params.setMargins(0, margin, 0, 0);
            }
            view.setLayoutParams(params);
            view.setBackground(getDrawable(R.drawable.line_dotted));
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            layoutDottedLines.addView(view);
        }
    }

    private void showMileageList(List<MileageItem> mileageItems) {
        configureAdapter();
//        adapter.clearItems();
        adapter.addMileageItems(mileageItems);
        adapter.setTimeType(tabSelected);
        adapter.notifyDataSetChanged();
    }

    private void showEcoList(List<EcoItem> ecoItems) {
        configureAdapter();
//
        adapter.clearItems();
        adapter.addEcoItems(ecoItems);
        adapter.setTimeType(tabSelected);
        adapter.notifyDataSetChanged();
    }

    private void showSafetyList(List<SafetyItem> safetyItems) {
        configureAdapter();
//
        adapter.clearItems();
        adapter.addSafetyItems(safetyItems);
        adapter.setTimeType(tabSelected);
        adapter.notifyDataSetChanged();
    }


}
