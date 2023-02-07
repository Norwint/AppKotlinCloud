package com.otcengineering.white_app.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.ethanco.circleprogresslibrary.CircleProgress;
import com.otc.alice.api.model.MyDrive;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BadgesActivity;
import com.otcengineering.white_app.activities.ChartActivity;
import com.otcengineering.white_app.activities.RankingActivity;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

import org.threeten.bp.LocalDate;

import java.util.Locale;


public class MyDriveFragment extends EventFragment {
    public static final int DAILY = Constants.TimeType.DAILY;
    public static final int WEEKLY = Constants.TimeType.WEEKLY;
    public static final int MONTHLY = Constants.TimeType.MONTHLY;

    private ScrollView scrollView;
    private FrameLayout btnScrollUp;
    private CustomTabLayout customTabLayout;
    private LinearLayout mileage, eco, safety, globalRanking, localRanking, RankingLocalEco,
            RankingGlobalEco, RankingGlobalMileage, RankingLocalMileage, badges;
    private TextView globalTop, localTop;
    private ImageView LocalIconTop, GlobalIconTop;
    private TextView TitleDate, AverageMileage, TotalMileage;
    private ImageView GlobalIconMileage, LocalIconMileage;
    private TextView AverageEco, TotalEco;
    private ImageView LocalIconEco, GlobalIconEco;
    private TextView SafetyDriving;
    private CircleProgress CircleProgressMileage, CircleProgressEco, CircleProgressSafety;
    private TextView totalBadges;

    public MyDriveFragment() {
        super("MyDriveActivity");
    }

    private int tabSelected = WEEKLY;

    private AsyncTask getDataSummaryTask;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_drive, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    private void retrieveViews(View v) {
        scrollView = v.findViewById(R.id.my_drive_scrollView);
        btnScrollUp = v.findViewById(R.id.my_drive_btnScrollUp);

        customTabLayout = v.findViewById(R.id.my_drive_customTabLayout);
        mileage = v.findViewById(R.id.mileageOpen);
        eco = v.findViewById(R.id.ecoOpen);
        safety = v.findViewById(R.id.safetyOpen);
        globalRanking = v.findViewById(R.id.globalRanking);
        localRanking = v.findViewById(R.id.localRanking);
        RankingLocalEco = v.findViewById(R.id.RankingLocalEco);
        RankingGlobalEco = v.findViewById(R.id.RankingGlobalEco);
        RankingGlobalMileage = v.findViewById(R.id.RankingGlobalMileage);
        RankingLocalMileage = v.findViewById(R.id.RankingLocalMileage);
        globalTop = v.findViewById(R.id.textGlobalTop);
        localTop = v.findViewById(R.id.textLocalTop);
        LocalIconTop = v.findViewById(R.id.LocalIconTop);
        GlobalIconTop = v.findViewById(R.id.GlobalIconTop);
        TitleDate = v.findViewById(R.id.TitleDate);
        AverageMileage = v.findViewById(R.id.AverageMileage);
        TotalMileage = v.findViewById(R.id.TotalMileage);
        GlobalIconMileage = v.findViewById(R.id.GlobalIconMileage);
        LocalIconMileage = v.findViewById(R.id.LocalIconMileage);
        AverageEco = v.findViewById(R.id.AverageEco);
        TotalEco = v.findViewById(R.id.TotalEco);
        LocalIconEco = v.findViewById(R.id.LocalIconEco);
        GlobalIconEco = v.findViewById(R.id.GlobalIconEco);
        SafetyDriving = v.findViewById(R.id.SafetyDriving);
        CircleProgressMileage = v.findViewById(R.id.progressMileage);
        CircleProgressEco = v.findViewById(R.id.progressEco);
        CircleProgressSafety = v.findViewById(R.id.progressSafety);

        badges = v.findViewById(R.id.badges);
        totalBadges = v.findViewById(R.id.tv_totalbadges);
    }

    private void setEvents() {
        customTabLayout.configure(WEEKLY, tabSelected -> {
            if (getDataSummaryTask != null) {
                getDataSummaryTask.cancel(true);
            }
            manageTabChanged(tabSelected);
        }, DAILY, WEEKLY, MONTHLY);

        mileage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChartActivity.class);
            intent.putExtra(Constants.Extras.CHART_MODE, Constants.ChartMode.MILEAGE);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        eco.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChartActivity.class);
            intent.putExtra(Constants.Extras.CHART_MODE, Constants.ChartMode.ECO);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        safety.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChartActivity.class);
            intent.putExtra(Constants.Extras.CHART_MODE, Constants.ChartMode.SAFETY);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        globalRanking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RankingActivity.class);
            intent.putExtra(Constants.Extras.RANKING_MODE, Constants.RankingMode.BEST);
            intent.putExtra(Constants.Extras.RANKING_TYPE, Constants.RankingType.GLOBAL);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        localRanking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RankingActivity.class);
            intent.putExtra(Constants.Extras.RANKING_MODE, Constants.RankingMode.BEST);
            intent.putExtra(Constants.Extras.RANKING_TYPE, Constants.RankingType.LOCAL);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        RankingLocalEco.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RankingActivity.class);
            intent.putExtra(Constants.Extras.RANKING_MODE, Constants.RankingMode.ECO);
            intent.putExtra(Constants.Extras.RANKING_TYPE, Constants.RankingType.LOCAL);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        RankingGlobalEco.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RankingActivity.class);
            intent.putExtra(Constants.Extras.RANKING_MODE, Constants.RankingMode.ECO);
            intent.putExtra(Constants.Extras.RANKING_TYPE, Constants.RankingType.GLOBAL);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        RankingGlobalMileage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RankingActivity.class);
            intent.putExtra(Constants.Extras.RANKING_MODE, Constants.RankingMode.MILEAGE);
            intent.putExtra(Constants.Extras.RANKING_TYPE, Constants.RankingType.GLOBAL);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        RankingLocalMileage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RankingActivity.class);
            intent.putExtra(Constants.Extras.RANKING_MODE, Constants.RankingMode.MILEAGE);
            intent.putExtra(Constants.Extras.RANKING_TYPE, Constants.RankingType.LOCAL);
            intent.putExtra(Constants.Extras.TIME_TYPE, tabSelected);
            startActivity(intent);
        });

        btnScrollUp.setOnClickListener(view -> scrollView.smoothScrollTo(0, 0));

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            if (scrollY > 0) {
                btnScrollUp.setVisibility(View.VISIBLE);
            } else {
                btnScrollUp.setVisibility(View.GONE);
            }
        });

        badges.setOnClickListener(v -> startActivity(new Intent(getContext(), BadgesActivity.class)));

        MySharedPreferences msp = MySharedPreferences.createBadges(getContext());
        totalBadges.setText(String.format(Locale.US,"%d", msp.getInteger("TotalBadges")));
    }

    private void manageTabChanged(int tabSelected) {
        this.tabSelected = tabSelected;
        getMyDriveInfoFromDb();
        MySharedPreferences msp = MySharedPreferences.createBadges(getContext());
        totalBadges.setText(String.format(Locale.US,"%d", msp.getInteger("TotalBadges")));
        if (ConnectionUtils.isOnline(getContext())) {
            getDataSummaryTask = new GetDataSummaryTask().execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void getMyDriveInfoFromDb() {
        MyDrive.SummaryResponse myDriveInfo = PrefsManager.getInstance().getMyDriveInfo(getContext());
        setDataSummary(myDriveInfo);
    }

    @SuppressLint("StaticFieldLeak")
    class GetDataSummaryTask extends AsyncTask<Void, Void, MyDrive.SummaryResponse> {
        @Override
        protected MyDrive.SummaryResponse doInBackground(Void... params) {
            try {
                Context context = getContext();
                if (context != null) {
                    MySharedPreferences msp = MySharedPreferences.createLogin(getContext());

                    MyDrive.Summary.Builder sum = MyDrive.Summary.newBuilder();
                    sum.setTypeTimeValue(tabSelected);

                    return ApiCaller.doCall(Endpoints.SUMMARY, msp.getBytes("token"), sum.build(), MyDrive.SummaryResponse.class);
                }
                else
                {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MyDrive.SummaryResponse response) {
            PrefsManager.getInstance().saveMyDriveInfo(response, getContext());
            setDataSummary(response);
        }
    }

    private void setDataSummary(MyDrive.SummaryResponse response) {
        if (response == null) return;

        int mileage = (int) ((response.getMileageTotal() / Constants.CAR_MILEAGE_BEST) * 100);
        CircleProgressMileage.setProgress(mileage);

        int eco = (int) ((response.getEcoAverageConsumption() / Constants.CAR_CONSUMPTION_BEST) * 100);
        CircleProgressEco.setProgress(eco);

        //response.getSafetyDrivingTechnique()
        int safety = (int) (response.getSafetyDrivingTechnique());
        CircleProgressSafety.setProgress(safety);

        try {
            int iconPosition3orHigher = R.drawable.my_drive_icons_11;
            int iconPosition2 = R.drawable.my_drive_icons_10;
            int iconPosition1 = R.drawable.my_drive_icons_17;

            final Activity act = this.getActivity();
            if (act != null) {
                switch (response.getBestLocalRanking()) {
                    case 1: Glide.with(act).load(iconPosition1).into(LocalIconTop); break;
                    case 2: Glide.with(act).load(iconPosition2).into(LocalIconTop); break;
                    default: Glide.with(act).load(iconPosition3orHigher).into(LocalIconTop); break;
                }

                switch (response.getMileageLocalRanking()) {
                    case 1: Glide.with(act).load(iconPosition1).into(LocalIconMileage); break;
                    case 2: Glide.with(act).load(iconPosition2).into(LocalIconMileage); break;
                    default: Glide.with(act).load(iconPosition3orHigher).into(LocalIconMileage); break;
                }

                switch (response.getEcoLocalRanking()) {
                    case 1: Glide.with(act).load(iconPosition1).into(LocalIconEco); break;
                    case 2: Glide.with(act).load(iconPosition2).into(LocalIconEco); break;
                    default: Glide.with(act).load(iconPosition3orHigher).into(LocalIconEco); break;
                }
                /*

                if (response.getEcoGlobalRanking() > 2 || response.getEcoGlobalRanking() == 0) {
                    Glide.with(act).load(iconPosition3orHigher).into(GlobalIconEco);
                } else {
                    Context context = GlobalIconEco.getContext();
                    int id = context.getResources().getIdentifier("global" + response.getEcoGlobalRanking(), "drawable", context.getPackageName());
                    Glide.with(act).load(id).into(GlobalIconEco);
                }

                if (response.getBestGlobalRanking() > 2 || response.getBestGlobalRanking() == 0) {
                    Glide.with(act).load(iconPosition3orHigher).into(GlobalIconTop);
                } else {
                    Context context = GlobalIconTop.getContext();
                    int id = context.getResources().getIdentifier("global" + response.getBestGlobalRanking(), "drawable", context.getPackageName());
                    Glide.with(act).load(id).into(GlobalIconTop);
                }

                if (response.getMileageGlobalRanking() > 2 || response.getMileageGlobalRanking() == 0) {
                    Glide.with(act).load(iconPosition3orHigher).into(GlobalIconMileage);
                } else {
                    Context context = GlobalIconMileage.getContext();
                    int id = context.getResources().getIdentifier("global" + response.getMileageGlobalRanking(), "drawable", context.getPackageName());
                    Glide.with(act).load(id).into(GlobalIconMileage);
                }
                */

            }
        } catch (Exception e) {
            //Log.e("MyDrive", "Exception", e);
        }

        try {
            String strCurrentDate = response.getDateStart();
            LocalDate newDate = DateUtils.stringToDate(strCurrentDate, DateUtils.FMT_SRV_DATE);
            String date1 = DateUtils.getDayAndMonthFormatted(newDate);

            if (!response.getDateEnd().equals(response.getDateStart())) {
                String strCurrentDate2 = response.getDateEnd();
                LocalDate newDate2 = DateUtils.stringToDate(strCurrentDate2, DateUtils.FMT_SRV_DATE);
                String date2 = DateUtils.getDayAndMonthFormatted(newDate2);

                TitleDate.setText(String.format("%s - %s", date1, date2));
            } else {
                TitleDate.setText(date1);
            }
        } catch (Exception e) {
            //Log.e("test", e.toString());
        }

        globalTop.setText(String.format(Locale.US, "%d", response.getBestGlobalRanking()));
        localTop.setText(String.format(Locale.US, "%d", response.getBestLocalRanking()));

        AverageMileage.setText(String.format(Locale.US, "%.1f km", response.getMileageAverage()));
        TotalMileage.setText(String.format(Locale.US, "%.1f km", response.getMileageTotal()));

        try {
            mountLayoutGlobalLocalonline(response.getMileageLocalRanking(), RankingLocalMileage, getResources().getString(R.string.local));
            mountLayoutGlobalLocalonline(response.getMileageGlobalRanking(), RankingGlobalMileage, getResources().getString(R.string.global));

            mountLayoutGlobalLocalonline(response.getEcoLocalRanking(), RankingLocalEco, getResources().getString(R.string.local));
            mountLayoutGlobalLocalonline(response.getEcoGlobalRanking(), RankingGlobalEco, getResources().getString(R.string.global));
        }
        catch (IllegalStateException ise) {
            //Log.e("MyDriveFragment", "IllegalStateException", ise);
        }

        AverageEco.setText(String.format(Locale.US, "%.1f km/l", response.getEcoAverageConsumption()));
        TotalEco.setText(String.format(Locale.US, "%.1f l", response.getEcoTotalConsumption()));

        double safetyDriving = response.getSafetyDrivingTechnique() / 10.0;

        SafetyDriving.setText(String.format(Locale.US, "%.1f", safetyDriving > 10 ? 10 : safetyDriving));
    }


    private void mountLayoutGlobalLocalonline(int valor, LinearLayout linearLayout, String textRanking) {
        for (int i = 1; i < linearLayout.getChildCount(); i++) {
            linearLayout.removeViewAt(i);
        }

        if (valor > -1) {
            LinearLayout contenedor = new LinearLayout(getContext());
            contenedor.setLayoutParams(new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            contenedor.setOrientation(LinearLayout.VERTICAL);
            contenedor.setPadding(3, 15, 0, 1);
            contenedor.setGravity(Gravity.CENTER);

            TextView textView1 = new TextView(getContext());
            textView1.setLayoutParams(new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            textView1.setText(textRanking);
            textView1.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textView1.setPadding(15, 0, 0, 0);
            textView1.setTextColor(Color.parseColor("#AFAEC6"));

            TextView textView2 = new TextView(getContext());
            textView2.setLayoutParams(new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            textView2.setText(String.format(Locale.US, "Top %d", valor));
            textView2.setTextColor(Color.parseColor("#032E7B"));
            textView2.setTextSize(17);
            textView2.setPadding(15, 0, 0, 0);
            textView2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            contenedor.addView(textView1);
            contenedor.addView(textView2);

            linearLayout.addView(contenedor);
        } else {
            LinearLayout contenedor = new LinearLayout(getContext());
            contenedor.setLayoutParams(new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            contenedor.setOrientation(LinearLayout.HORIZONTAL);
            contenedor.setGravity(Gravity.CENTER);
            contenedor.setPadding(0, 30, 0, 0);

            TextView textView1 = new TextView(getContext());
            textView1.setLayoutParams(new LinearLayout.LayoutParams(120, TableLayout.LayoutParams.MATCH_PARENT));
            textView1.setText(textRanking);
            textView1.setTextSize(12);
            textView1.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textView1.setGravity(Gravity.CENTER);
            textView1.setTextColor(Color.parseColor("#AFAEC6"));

            contenedor.addView(textView1);

            TextView textView2 = new TextView(getContext());
            textView2.setLayoutParams(new LinearLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

            textView2.setText(String.valueOf(valor));
            textView2.setTextColor(Color.parseColor("#FF0000"));
            textView2.setTextSize(20);
            textView2.setGravity(Gravity.CENTER);
            textView2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            contenedor.addView(textView2);
            linearLayout.addView(contenedor);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getDataSummaryTask != null) {
            getDataSummaryTask.cancel(true);
        }
    }
}