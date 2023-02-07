package com.otcengineering.white_app.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.ethanco.circleprogresslibrary.CircleProgress;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.DashboardAndStatus;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyDrive;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.apible.CarStatus;
import com.otcengineering.apible.OtcBle;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.ChartActivity;
import com.otcengineering.white_app.activities.ConditionActivity;
import com.otcengineering.white_app.activities.Home2Activity;
import com.otcengineering.white_app.activities.NewRouteActivity;
import com.otcengineering.white_app.keyless.activity.VehicleSchedulerActivity;
import com.otcengineering.white_app.fragments.cached.NewDashboardCache;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.ProfileNetwork;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

public class NewDashboardFragment extends EventFragment {
    private ImageView carPicture;
    private TextView carAge, carMileage, carServiceTiming, txtNotification,
            txtPosts, txtLikes, txtDrivingTime, txtLiters, txtLocalRanking,
            txtSafety, txtEco, txtMaturity, txtTimes, txtPrice, carUpdated, vehicleConditionText;
    private CircleProgress carFuel, progressSafety, progressEco;
    private ImageView carCondition, imgNotification, imgVehicleCondition;
    private ConstraintLayout carConditionLayout, layoutMileage, layoutEco, layoutSafety, layoutNotification, maintenanceLayout, activeRoute;
    private Timer m_timer, m_timer2;
    private int action = -1;
    private long notificationId = -1;

    // Cached variables
    private NewDashboardCache m_cache;
    private boolean m_vehCondition = false;

    public NewDashboardFragment() {
        super("DashboardActivity");

        m_cache = new NewDashboardCache();
        MySharedPreferences msp = MySharedPreferences.createDashboard(getContext());

        MySharedPreferences msp2 = MySharedPreferences.createLogin(getContext());

        String json = msp2.getString("StatusCache");
        if (!json.isEmpty()) {
            try {
                General.VehicleStatus vs = new Gson().fromJson(json, General.VehicleStatus.class);
                if (vs != null) {
                    m_cache.update = DateUtils.reparseDateTime(vs.getDate(), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm");
                    m_cache.fuel = vs.getFuelLevel();
                    m_cache.mileage = vs.getOdometer();
                }
            } catch (JsonSyntaxException jse) {
                //Log.e("CarStatusFragment", "JsonSyntaxException", jse);
            }
        }

        m_cache.safety = msp2.getDouble("ProfileSafetyWeekly");
        m_cache.eco = msp2.getDouble("ProfileEcoWeekly");
        if (msp.contains("CarPictureId")) {
            m_cache.pictureID = msp.getLong("CarPictureId");
        }
        Utils.runOnBackThread(() -> {
            if (msp.contains("DashboardCache")) {
                byte[] arr = msp.getBytes("DashboardCache");
                DashboardAndStatus.DashboardResponse resp;
                try {
                    resp = DashboardAndStatus.DashboardResponse.parseFrom(arr);

                    m_cache.loanMaturity = resp.getLoanMaturity();
                    m_cache.paidLoans = resp.getPayedLoans();
                    m_cache.totalLoans = resp.getTotalLoans();

                    m_cache.update = DateUtils.utcStringToLocalString(resp.getDateUpdate(), "yyyy-MM-dd HH:mm:ss.S", "dd/MM/yyyy - HH:mm");

                    m_cache.years = resp.getAgeYears();
                    m_cache.months = resp.getAgeMonths();
                    m_cache.inServiceTiming = resp.getInServiceTiming();

                    m_cache.mileage = resp.getTotalMileage();
                    m_cache.fuel = resp.getFuelLevel();

                    m_cache.monthlyMinutes = resp.getMonthlyDrivingMinutes();
                    m_cache.totalMinutes = resp.getTotalDrivingMinutes();
                    m_cache.monthlyFuel = resp.getMonthlyFuelConsume() / 100.0f;
                    m_cache.totalFuel = resp.getTotalFuelConsume() / 100.0f;
                    m_cache.bestLocalWeekly = msp.getInteger("ProfileBestLocalWeekly");
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
            if (msp.contains("PostStatsCache")) {
                byte[] bs = msp.getBytes("PostStatsCache");
                try {
                    Community.PostStats ps = Community.PostStats.parseFrom(bs);
                    m_cache.monthlyPosts = ps.getMonthlyPosts();
                    m_cache.totalPosts = ps.getTotalPosts();
                    m_cache.monthlyLikes = ps.getMonthlyLikes();
                    m_cache.totalLikes = ps.getTotalLikes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_dashboard, container, false);

        if (v != null) {
            retrieveViews(v);
            setEvents();

            setCachedValues();
            carPicture.setImageDrawable(null);
            setDefaultCarPicture();

            getDashboardData();

            GenericTask getImage = new GenericTask(Endpoints.DASHBOARD_CAR_PHOTO, null, true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    DashboardAndStatus.CarPhoto cp = otcResponse.getData().unpack(DashboardAndStatus.CarPhoto.class);
                    MySharedPreferences.createDashboard(getContext()).putLong("CarPictureId", cp.getFileId());
                    Long fileId = cp.getFileId();
                    setCarPicture(fileId);
                } else {
                    MySharedPreferences.createDashboard(getContext()).remove("CarPictureId");
                    setDefaultCarPicture();
                }
            });
            getImage.execute();
        }

        return v;
    }

    private void getDashboardData() {
        GenericTask getDashboard = new GenericTask(Endpoints.DASHBOARD, null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                DashboardAndStatus.DashboardResponse resp = otcResponse.getData().unpack(DashboardAndStatus.DashboardResponse.class);

                setFinance(resp.getLoanMaturity(), String.format(Locale.US, "%d / %d %s",  resp.getPayedLoans(), resp.getTotalLoans(), getString(R.string.times)),
                        String.format(Locale.US, "%,d", resp.getTradeInPrice()));

                String json = MySharedPreferences.createLogin(getContext()).getString("StatusCache");
                General.VehicleStatus vs = null;
                if (!json.isEmpty()) {
                    try {
                        vs = new Gson().fromJson(json, General.VehicleStatus.class);
                    } catch (JsonSyntaxException jse) {
                    }
                }

                if (vs != null && DateUtils.compareDates(resp.getDateUpdate().substring(0, resp.getDateUpdate().indexOf(".")), vs.getDate(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss") == DateUtils.DATE_BEFORE) {
                    setUpdate(DateUtils.utcStringToLocalString(vs.getDate(), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm"));
                    if (!OtcBle.getInstance().isConnected()) {
                        setMileage(String.format(Locale.US, "%d km", vs.getOdometer()));
                        setFuel(vs.getFuelLevel());
                    }

                    DashboardAndStatus.DashboardResponse.Builder builder = DashboardAndStatus.DashboardResponse.newBuilder(resp);
                    builder.setTotalMileage(vs.getOdometer());
                    builder.setDateUpdate(vs.getDate());
                    builder.setFuelLevel(vs.getFuelLevel());
                    resp = builder.build();
                } else {
                    if (!OtcBle.getInstance().isConnected()) {
                        setUpdate(DateUtils.utcStringToLocalString(resp.getDateUpdate().substring(0, resp.getDateUpdate().indexOf(".")), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm"));
                        setMileage(String.format(Locale.US, "%d km", resp.getTotalMileage()));
                        setFuel(resp.getFuelLevel());
                    }
                }

                setTextCarInfo(makeCarAge(resp.getAgeYears(), resp.getAgeMonths()),
                        resp.getInServiceTiming());
                String mDriving = minutesToHourMin(resp.getMonthlyDrivingMinutes());
                String tDriving = minutesToHourMin(resp.getTotalDrivingMinutes());

                MySharedPreferences msp = MySharedPreferences.createLogin(getContext());

                setCarDrive(String.format("%s / %s", mDriving, tDriving),
                        String.format(Locale.US, "%01.02f L / %01.02f L", resp.getMonthlyFuelConsume() / 100.0f, resp.getTotalFuelConsume() / 100.0f),
                        String.format(Locale.US, "%d", msp.getInteger("ProfileBestLocalWeekly")));
                MySharedPreferences.createDashboard(getContext()).putBytes("DashboardCache", resp.toByteArray());
            }
        });
        getDashboard.execute();

        GenericTask getStats = new GenericTask(Endpoints.POST_STATS, null, true, otcResponse -> {
            if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                Community.PostStats ps = otcResponse.getData().unpack(Community.PostStats.class);

                MySharedPreferences.createDashboard(getContext()).putBytes("PostStatsCache", ps.toByteArray());

                setTextDrive(String.format(Locale.US, "%d/%d", ps.getMonthlyPosts(), ps.getTotalPosts()), String.format(Locale.US, "%d/%d", ps.getMonthlyLikes(), ps.getTotalLikes()));
            }
        });
        getStats.execute();

        MyDrive.Summary summary = MyDrive.Summary.newBuilder().setTypeTime(General.TimeType.WEEKLY).build();
        TypedTask<MyDrive.SummaryResponse> getMyDrive = new TypedTask<>(Endpoints.SUMMARY, summary, true, MyDrive.SummaryResponse.class, new TypedCallback<MyDrive.SummaryResponse>() {
            @Override
            public void onSuccess(@Nonnull @NonNull MyDrive.SummaryResponse value) {
                MySharedPreferences msp = MySharedPreferences.createDashboard(getContext());
                msp.putDouble("ProfileSafetyWeekly", value.getSafetyDrivingTechnique());
                msp.putDouble("ProfileEcoWeekly", Utils.clamp(value.getEcoAverageConsumption() / Constants.CAR_CONSUMPTION_BEST, 0D, 1D));
                msp.putInteger("ProfileBestLocalWeekly", value.getBestLocalRanking());
                setDriving(msp.getDouble("ProfileSafetyWeekly"), msp.getDouble("ProfileEcoWeekly"));
            }

            @Override
            public void onError(@NonNull Shared.OTCStatus status, String str) {

            }
        });
        getMyDrive.execute();

        if (!OtcBle.getInstance().isConnected()) {
            TypedTask<DashboardAndStatus.VehicleCondition> getVehicleCondition = new TypedTask<>(Endpoints.VEHICLE_CONDITION, null, true, DashboardAndStatus.VehicleCondition.class,
                    new TypedCallback<DashboardAndStatus.VehicleCondition>() {
                        @Override
                        public void onSuccess(@Nonnull @NonNull DashboardAndStatus.VehicleCondition value) {
                            setCarCondition(Utils.hasProblems(value));
                        }

                        @Override
                        public void onError(@NonNull Shared.OTCStatus status, String str) {

                        }
                    });
            getVehicleCondition.execute();
        }
        Utils.runOnMainThread(this::loadLastNoti);
    }

    private void loadLastNoti() {
        if (MySharedPreferences.createLogin(getContext()).getLong("NotificationCount") >= 1) {
            General.Page page = General.Page.newBuilder().setPage(1).build();
            TypedTask<ProfileAndSettings.UserNotifications> getNotifications = new TypedTask<>(Endpoints.GET_USER_NOTIFICATIONS, page, true, ProfileAndSettings.UserNotifications.class,
                    new TypedCallback<ProfileAndSettings.UserNotifications>() {
                        @Override
                        public void onSuccess(@Nonnull @NonNull ProfileAndSettings.UserNotifications value) {
                            Utils.runOnBackThread(() -> {
                                for (int i = 0; i < value.getNotificationListCount(); ++i) {
                                    if (value.getNotificationList(i).getTypeValue() >= 6 && value.getNotificationList(i).getTypeValue() <= 9) {
                                        if (!value.getNotificationList(i).getReaded()) {
                                            int finalI = i;
                                            Utils.runOnMainThread(() -> {
                                                action = value.getNotificationList(finalI).getTypeValue() - 6;
                                                notificationId = value.getNotificationList(finalI).getId();
                                                imgNotification.setVisibility(View.VISIBLE);
                                                txtNotification.setText(getString(R.string.you_have_new_message));
                                            });
                                            return;
                                        }
                                    }
                                }
                                Utils.runOnMainThread(() -> {
                                    txtNotification.setText("");
                                    imgNotification.setVisibility(View.GONE);
                                });
                            });
                        }

                        @Override
                        public void onError(@NonNull Shared.OTCStatus status, String str) {

                        }
                    });
            getNotifications.execute();
        } else {
            txtNotification.setText("");
            imgNotification.setVisibility(View.GONE);
        }
    }

    private void setCachedValues() {
        try {
            setDriving(m_cache.safety, m_cache.eco);

            setFinance(m_cache.loanMaturity, String.format(Locale.US, "%d / %d %s", m_cache.paidLoans, m_cache.totalLoans, getString(R.string.times)), "");
            setUpdate(m_cache.update);
            setTextCarInfo(makeCarAge(m_cache.years, m_cache.months), m_cache.inServiceTiming);

            if (!OtcBle.getInstance().isConnected()) {
                setMileage(String.format(Locale.US, "%d km", m_cache.mileage));
                setFuel(m_cache.fuel);
            }

            String mDriving = minutesToHourMin(m_cache.monthlyMinutes);
            String tDriving = minutesToHourMin(m_cache.totalMinutes);

            setCarDrive(String.format("%s / %s", mDriving, tDriving),
                    String.format(Locale.US, "%01.02f L / %01.02f L", m_cache.monthlyFuel, m_cache.totalFuel),
                    String.format(Locale.US, "%d", m_cache.bestLocalWeekly));

            if (m_cache.pictureID > 0) {
                setCarPicture(m_cache.pictureID);
            }

            setTextDrive(String.format(Locale.US, "%d/%d", m_cache.monthlyPosts, m_cache.totalPosts), String.format(Locale.US, "%d/%d", m_cache.monthlyLikes, m_cache.totalLikes));

            m_cache = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String makeCarAge(int years, int months) {
        String sYear = null, sMonth = null;
        if (years > 0) {
            if (years == 1) {
                sYear = getString(R.string.one_year);
            } else {
                sYear = getString(R.string.n_years, years);
            }
        }

        if (months > 0) {
            if (months == 1) {
                sMonth = getString(R.string.one_month);
            } else {
                sMonth = getString(R.string.n_months, months);
            }
        }

        if (sYear != null && sMonth != null) {
            return String.format("%s / %s", sYear, sMonth);
        } else if (sYear == null && sMonth != null) {
            return sMonth;
        } else if (sYear != null) {
            return sYear;
        } else {
            return getString(R.string.zero_m_zero_y);
        }
    }

    private String minutesToHourMin(int minutes) {
        int hours = minutes / 60;
        int min = minutes % 60;

        String negative = minutes < 0 ? "-" : "";

        return String.format(Locale.US, "%s%02d:%02d", negative, Math.abs(hours), Math.abs(min));
    }

    @Override
    public void onResume() {
        super.onResume();
        m_timer = new Timer("DashboardTimer");
        m_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Utils.runOnMainThread(() -> {
                    if (OtcBle.getInstance().isConnected() && OtcBle.getInstance().carStatus.contains("Fuel")) {
                        setFuel(OtcBle.getInstance().carStatus.getByteVar("Fuel"));
                        setMileage(String.format(Locale.US, "%d km", OtcBle.getInstance().carStatus.getIntVar("Odometer")));
                        setUpdate(DateUtils.getLocalString("dd/MM/yyyy - HH:mm"));
                    }
                });

                CarStatus cs = OtcBle.getInstance().carStatus;
                if (OtcBle.getInstance().isConnected() && cs.getBitVar("KL15") != null) {
                    try {
                        boolean vc = cs.getBitVar("EngineNotif") || cs.getBitVar("EpsNotif") || cs.getBitVar("AscNotif") || cs.getBitVar("BrakeSystemNotif") ||
                                cs.getBitVar("AbsNotif") || cs.getBitVar("OssImmoNotif") || cs.getBitVar("KosNotif") || cs.getBitVar("SrsNotif") ||
                                cs.getBitVar("AtNotif") || cs.getBitVar("OilNotif") || cs.getBitVar("ChargeNotif") ||  cs.getBitVar("BrakeFluidNotif") ||
                                cs.getBitVar("OssElecNotif") || cs.getBitVar("OssSteeNotif");
                        Utils.runOnMainThread(() -> setCarCondition(vc));
                    } catch (NullPointerException npe) {
                        // No em toquis els pebrots, Java
                        npe.printStackTrace();
                    }
                }
            }
        }, 0, 250);

        m_timer2 = new Timer("DashboardTimer2");
        m_timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getDashboardData();
            }
        }, 10000, 10000);

        activeRoute.setVisibility(PrefsManager.getInstance().getRouteInProgress(getContext()) != null ? View.VISIBLE : View.GONE);

        if (m_vehCondition) {
            m_vehCondition = false;
            startActivity(new Intent(MyApp.getCurrentActivity(), ConditionActivity.class));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_timer.cancel();
        m_timer2.cancel();
    }

    private void setEvents() {
        this.carConditionLayout.setOnClickListener(v -> {
            if (Utils.developer) startActivity(new Intent(getContext(), VehicleSchedulerActivity.class));
            else startActivity(new Intent(getActivity(), ConditionActivity.class));
        });
        this.carPicture.setOnClickListener(v -> {
            Home2Activity ha = (Home2Activity) getActivity();
            if (ha != null) {
                ha.goDocuments();
            }
        });

        layoutEco.setOnClickListener(v -> openChart(Constants.ChartMode.ECO));
        layoutSafety.setOnClickListener(v -> openChart(Constants.ChartMode.SAFETY));
        layoutMileage.setOnClickListener(v -> openChart(Constants.ChartMode.MILEAGE));
        layoutNotification.setOnClickListener(v -> {
            if (action > -1) {
                ProfileNetwork.setNotificationRead(notificationId);

                int l_action = action;
                notificationId = -1;
                action = -1;

                Home2Activity home = (Home2Activity)getActivity();
                if (home != null) {
                    home.setCommunityTab(2 + (2 + l_action) / 2);
                    home.setCommunitySubtab(l_action % 2);
                    home.clickCommunications();
                }
            }
        });

        activeRoute.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewRouteActivity.class);
            intent.putExtra("RouteActive", true);
            startActivity(intent);
        });
    }

    private void openChart(int mode) {
        Intent intent = new Intent(getContext(), ChartActivity.class);
        intent.putExtra(Constants.Extras.CHART_MODE, mode);
        intent.putExtra(Constants.Extras.TIME_TYPE, Constants.TimeType.WEEKLY);
        startActivity(intent);
    }

    @UiThread
    private void setDefaultCarPicture() {
        MySharedPreferences msp = MySharedPreferences.createDashboard(getContext());
        Long imgId = msp.getLong("CarPictureId");
        if (imgId > 0) {
            setCarPicture(imgId);
        } else {
            Glide.with(this).load(R.drawable.car_otc).apply(new RequestOptions().transform(new RoundedCorners(20))).into(carPicture);
        }
    }

    @UiThread
    private void setCarPicture(Long fileId) {
        Glide.with(this)
                .load(fileId)
                .placeholder(carPicture.getDrawable())
                .apply(new RequestOptions().transform(new RoundedCorners(20)))
                .into(carPicture);
    }

    @UiThread
    private void setTextCarInfo(String carAge, boolean serviceTiming) {
        // this.carAge.setText(carAge);
        this.carServiceTiming.setText(serviceTiming ? getString(R.string.maintenance_timing) : getString(R.string.no_maintenance_timing));
        this.carServiceTiming.setTextColor(ContextCompat.getColor(getContext(), serviceTiming ? R.color.quantum_black_100 : R.color.colorPrimary));
        this.maintenanceLayout.setBackground(ContextCompat.getDrawable(getContext(), serviceTiming ? R.drawable.maintenance_red : R.drawable.my_edittext_bg));
    }

    @UiThread
    private void setMileage(String mileage) {
        // this.carMileage.setText(mileage);
        if (OtcBle.getInstance().isConnected()) {
            this.carMileage.setText(String.format(Locale.US, "%d km", OtcBle.getInstance().carStatus.getIntVar("Odometer")));
            this.carAge.setText(String.format(Locale.US, "%01.01f%%", OtcBle.getInstance().carStatus.getRawData().get("SOC").getIntValue() / 10.0f));
        } else {
            this.carMileage.setText("--- km");
            this.carAge.setText("--- %");
        }
    }

    @UiThread
    private void setTextDrive(String posts, String likes) {
        this.txtPosts.setText(posts);
        this.txtLikes.setText(likes);
    }

    @UiThread
    private void setCarDrive(String drivingTime, String fuel, String rank) {
        this.txtDrivingTime.setText(drivingTime);
        this.txtLiters.setText(fuel);
        this.txtLocalRanking.setText(rank);
    }

    @UiThread
    private void setDriving(double safety, double eco) {
        this.progressSafety.setProgress((int)(safety));
        this.progressEco.setProgress((int)(eco * 100));
                this.txtSafety.setText(String.format(Locale.US, "%s\n%1.1f", getString(R.string.score), safety / 10));
        this.txtEco.setText(String.format(Locale.US, "%s\n%1.1f", getString(R.string.score), eco * 10));
    }

    @UiThread
    private void setUpdate(String update) {
        if (update == null) {
            update = "";
        }
        carUpdated.setText(String.format("%s: %s", getString(R.string.update), update));
    }

    @UiThread
    private void setFuel(int fuel) {
        this.carFuel.setProgress(fuel);
    }

    @UiThread
    private void setFinance(String maturity, String times, String price) {
        this.txtMaturity.setText(getString(R.string.maturity_n, maturity));
        this.txtTimes.setText(times);
        this.txtPrice.setText(price);
    }

    private void setCarCondition(boolean nok) {
        Glide.with(this).load(nok ? R.drawable.dashboard_icons2 : R.drawable.dashboard_icons5).into(this.carCondition);
        carConditionLayout.setBackgroundResource(nok ? R.drawable.condition_error : R.drawable.my_edittext_bg);
        vehicleConditionText.setTextColor(ContextCompat.getColor(getContext(), nok ? R.color.error : R.color.colorPrimary));
        carCondition.setImageTintList(nok ? ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.error)) : null);
        imgVehicleCondition.setImageTintList(nok ? ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.error)) : null);
    }


    private void retrieveViews(final View v) {
        carPicture = v.findViewById(R.id.carPicture);
        carAge = v.findViewById(R.id.carAge);
        carMileage = v.findViewById(R.id.carMileage);
        carServiceTiming = v.findViewById(R.id.carServiceTiming);
        txtPosts = v.findViewById(R.id.txtPosts);
        txtLikes = v.findViewById(R.id.txtLikes);
        txtDrivingTime = v.findViewById(R.id.txtDrivingTime);
        txtLiters = v.findViewById(R.id.txtLiters);
        txtLocalRanking = v.findViewById(R.id.txtLocalRanking);
        txtSafety = v.findViewById(R.id.txtSafety);
        txtEco = v.findViewById(R.id.txtEco);
        txtMaturity = v.findViewById(R.id.txtMaturity);
        txtTimes = v.findViewById(R.id.txtTimes);
        txtPrice = v.findViewById(R.id.txtPrice);
        carFuel = v.findViewById(R.id.carFuel);
        progressSafety = v.findViewById(R.id.progressSafety);
        progressEco = v.findViewById(R.id.progressEco);
        carCondition = v.findViewById(R.id.carCondition);
        carConditionLayout = v.findViewById(R.id.carConditionLayout);
        carUpdated = v.findViewById(R.id.carUpdated);
        layoutMileage = v.findViewById(R.id.layoutMileage);
        layoutEco = v.findViewById(R.id.layoutEco);
        layoutSafety = v.findViewById(R.id.layoutSafety);
        layoutNotification = v.findViewById(R.id.layoutNotification);
        txtNotification = v.findViewById(R.id.txtNotification);
        imgNotification = v.findViewById(R.id.imgNotification);
        maintenanceLayout = v.findViewById(R.id.maintenanceLayout);
        activeRoute = v.findViewById(R.id.activeRoute);
        vehicleConditionText = v.findViewById(R.id.textView41);
        imgVehicleCondition = v.findViewById(R.id.imageView16);
    }

    public void setVehicleCondition(boolean cnd) {
        this.m_vehCondition = cnd;
    }
}
