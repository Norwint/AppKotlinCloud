package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.ethanco.circleprogresslibrary.CircleProgress;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.ChartActivity;
import com.otcengineering.white_app.activities.InviteActivity;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.util.Locale;

/**
 * Created by cenci7
 */

public class InfoFragment extends BaseFragment {

    public interface MenuListener {
        void onMenu();
    }

    private ImageView imgUser;
    private Button btnInvite;
    private ImageView btnThreeDots;
    private TextView txtName, txtModel;
    private LinearLayout mileage, eco, safety;
    private TextView globalTop, localTop;
    private ImageView LocalIconTop, GlobalIconTop;
    private TextView AverageMileage, TotalMileage, LocalMileage, GlobalMileage;
    private ImageView GlobalIconMileage, LocalIconMileage;
    private TextView AverageEco, TotalEco, LocalEco, GlobalEco;
    private ImageView LocalIconEco, GlobalIconEco;
    private TextView SafetyDriving;
    private CircleProgress CircleProgressMileage, CircleProgressEco, CircleProgressSafety;

    private Community.UserCommunity user;

    private MenuListener listener;

    public void configure(Community.UserCommunity user, MenuListener listener) {
        this.user = user;
        this.listener = listener;
        if (isVisible() && isAdded()) {
            showProfileNameAndImage();
            getProfileInfo();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        retrieveViews(v);
        setEvents();
        showProfileNameAndImage();
        getProfileInfo();
        return v;
    }

    private void retrieveViews(View v) {
        imgUser = v.findViewById(R.id.info_imgUser);
        btnInvite = v.findViewById(R.id.info_btnInvite);
        btnInvite.setVisibility(View.GONE);
        btnThreeDots = v.findViewById(R.id.info_btnThreeDots);
        txtName = v.findViewById(R.id.info_txtName);
        txtModel = v.findViewById(R.id.info_txtModel);
        mileage = v.findViewById(R.id.mileageOpen);
        eco = v.findViewById(R.id.ecoOpen);
        safety = v.findViewById(R.id.safetyOpen);
        globalTop = v.findViewById(R.id.textGlobalTop);
        localTop = v.findViewById(R.id.textLocalTop);
        LocalIconTop = v.findViewById(R.id.LocalIconTop);
        GlobalIconTop = v.findViewById(R.id.GlobalIconTop);
        AverageMileage = v.findViewById(R.id.AverageMileage);
        TotalMileage = v.findViewById(R.id.TotalMileage);
        LocalMileage = v.findViewById(R.id.LocalMileage);
        GlobalMileage = v.findViewById(R.id.GlobalMileage);
        GlobalIconMileage = v.findViewById(R.id.GlobalIconMileage);
        LocalIconMileage = v.findViewById(R.id.LocalIconMileage);
        AverageEco = v.findViewById(R.id.AverageEco);
        TotalEco = v.findViewById(R.id.TotalEco);
        LocalEco = v.findViewById(R.id.LocalEco);
        GlobalEco = v.findViewById(R.id.GlobalEco);
        LocalIconEco = v.findViewById(R.id.LocalIconEco);
        GlobalIconEco = v.findViewById(R.id.GlobalIconEco);
        SafetyDriving = v.findViewById(R.id.SafetyDriving);
        CircleProgressMileage = v.findViewById(R.id.progressMileage);
        CircleProgressEco = v.findViewById(R.id.progressEco);
        CircleProgressSafety = v.findViewById(R.id.progressSafety);
    }

    private void setEvents() {
        btnInvite.setOnClickListener(view -> openInvite());

        btnThreeDots.setOnClickListener(view -> {
            if (listener != null) {
                listener.onMenu();
            }
        });

        mileage.setOnClickListener(v -> openChartActivity(Constants.ChartMode.MILEAGE));

        eco.setOnClickListener(v -> openChartActivity(Constants.ChartMode.ECO));

        safety.setOnClickListener(v -> openChartActivity(Constants.ChartMode.SAFETY));
    }

    private void openChartActivity(int chartMode) {
        Intent intent = new Intent(getActivity(), ChartActivity.class);
        intent.putExtra(Constants.Extras.CHART_MODE, chartMode);
        intent.putExtra(Constants.Extras.USER, user);
        startActivity(intent);
    }

    private void openInvite() {
        Intent intent = new Intent(getActivity(), InviteActivity.class);
        intent.putExtra(Constants.Extras.USER, user);
        startActivity(intent);
        btnInvite.setPressed(true);
    }

    private void getProfileInfo() {
        if (ConnectionUtils.isOnline(getContext())) {
            new GetProfileInfoTask().execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetProfileInfoTask extends AsyncTask<Void, Void, Community.UserSummary> {
        @Override
        protected Community.UserSummary doInBackground(Void... params) {
            try {
                General.UserId.Builder builder = General.UserId.newBuilder();
                builder.setUserId(user.getUserId());

                return ApiCaller.doCall(Endpoints.USER_PROFILE, PrefsManager.getInstance().getToken(getContext()), builder.build(), Community.UserSummary.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Community.UserSummary response) {
            setProfileData(response);
        }
    }

    private void showProfileNameAndImage() {
        if (user != null) {
            txtName.setText(user.getName());
            getImage(user.getImage());
        }
    }

    private boolean isMyUser() {
        long myUserId = PrefsManager.getInstance().getMyUserId(getContext());
        return user == null || user.getUserId() == myUserId;
    }

    private void setProfileData(Community.UserSummary response) {
        if (response == null) return;

        boolean showBtnInvite = !user.getFriend() && !isMyUser() && MySharedPreferences.createSocial(getContext()).getLong("user_" + user.getUserId()) < System.currentTimeMillis();
        btnInvite.setVisibility(showBtnInvite ? View.VISIBLE : View.GONE);

        txtName.setText(response.getName());
        txtModel.setText(response.getModel());

        int mileage = (int) (100 * response.getMileageTotal() / Constants.CAR_MILEAGE_BEST);
        CircleProgressMileage.setProgress(mileage);

        int eco = (int) (10000 * response.getEcoAverageConsumption() / Constants.CAR_CONSUMPTION_BEST);
        CircleProgressEco.setProgress(eco);

        int safety = (int)response.getSafetyDrivingTechnique();
        CircleProgressSafety.setProgress(safety);

        try {
            setIcon(response.getBestGlobalRanking(), GlobalIconTop);
            setIcon(response.getBestLocalRanking(), LocalIconTop);
            setIcon(response.getMileageGlobalRanking(), GlobalIconMileage);
            setIcon(response.getMileageLocalRanking(), LocalIconMileage);
            setIcon(response.getEcoGlobalRanking(), GlobalIconEco);
            setIcon(response.getEcoLocalRanking(), LocalIconEco);
        } catch (Exception e) {
            e.printStackTrace();
        }

        globalTop.setText(String.format(Locale.US, "%d", response.getBestGlobalRanking()));
        localTop.setText(String.format(Locale.US, "%d", response.getBestLocalRanking()));

        AverageMileage.setText(String.format(Locale.US, "%.1f km", response.getMileageAverage()));
        TotalMileage.setText(String.format(Locale.US, "%.1f km", response.getMileageTotal()));
        LocalMileage.setText(String.format(Locale.US, "%d", response.getMileageLocalRanking()));
        GlobalMileage.setText(String.format(Locale.US, "%d", response.getMileageGlobalRanking()));

        AverageEco.setText(String.format(Locale.US, "%.1f km/l", 100 * response.getEcoAverageConsumption()));
        TotalEco.setText(String.format(Locale.US, "%.1f l", response.getEcoTotalConsumption() / 100.f));
        LocalEco.setText(String.format(Locale.US, "Top: %d", response.getEcoLocalRanking()));
        GlobalEco.setText(String.format(Locale.US, "Top: %d", response.getEcoGlobalRanking()));

        double technique = response.getSafetyDrivingTechnique() / 10;
        SafetyDriving.setText(String.format(Locale.US, "%.1f", technique <= 10 ? technique : 10));
    }

    private void setIcon(int position, ImageView toSet) {
        int iconRes;
        switch (position) {
            case 1: iconRes = R.drawable.my_drive_icons_17; break;
            case 2: iconRes = R.drawable.my_drive_icons_10; break;
            default: iconRes = R.drawable.my_drive_icons_11; break;
        }
        Glide.with(this).load(iconRes).into(toSet);
    }

    private void getImage(long imageId) {
        if (imageId == 0) {
            showImagePlaceholder();
            return;
        }
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
        Activity act = getActivity();
        if (imagePath != null && act != null) {
            Glide.with(act)
                    .load(ImageUtils.getImageFromCache(getContext(), imagePath))
                    .into(imgUser);
        } else {
            showImagePlaceholder();
        }
    }

    private void showImagePlaceholder() {
        Activity act = getActivity();
        if (act != null) {
            Glide.with(act)
                    .load(R.drawable.user_placeholder_correct)
                    .into(imgUser);
        }
    }
}
