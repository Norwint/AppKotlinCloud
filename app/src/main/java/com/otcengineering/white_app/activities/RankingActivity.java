package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.MyDrive;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.RankingAdapter;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.UserInRankingItem;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends EventActivity {

    private static final int PAGE_SIZE = 50;

    public static final int DAILY = Constants.TimeType.DAILY;
    public static final int WEEKLY = Constants.TimeType.WEEKLY;
    public static final int MONTHLY = Constants.TimeType.MONTHLY;

    private static final int GLOBAL = Constants.RankingType.GLOBAL;
    private static final int LOCAL = Constants.RankingType.LOCAL;

    private LinearLayout layoutRankingType;
    private TitleBar titleBar;
    private CustomTabLayout customTabLayout;
    private Button btnRankingType;
    private TextView btnGoToMyPosition;
    private View viewEmpty;
    private TextView btnGlobalRanking, btnLocalRanking;
    private ImageView imgGlobalRankingCheck, imgLocalRankingCheck;
    private RecyclerView recycler;
    private FrameLayout btnScrollUp;

    private int scrollY = 0;

    private RankingAdapter adapter;

    private int rankingMode;
    private int rankingTypeSelected;
    private int tabSelected;

    private int page = 1;
    private int myPosition = -1;
    private int lastIndexWhenScrollingUp = -1; // to control the visible row when adding a new page scrolling up
    private boolean retrievingNewPage = false;

    public RankingActivity() {
        super("RankingActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        retrieveExtras();
        retrieveViews();
        setEvents();
        configureTitle();
        configureAdapter();
        manageRankingTypeSelectedUI();
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            rankingMode = getIntent().getExtras().getInt(Constants.Extras.RANKING_MODE, Constants.RankingMode.BEST);
            rankingTypeSelected = getIntent().getExtras().getInt(Constants.Extras.RANKING_TYPE, Constants.RankingType.LOCAL);
            tabSelected = getIntent().getExtras().getInt(Constants.Extras.TIME_TYPE, Constants.TimeType.DAILY);
        }
    }

    private void retrieveViews() {
        layoutRankingType = findViewById(R.id.ranking_layoutRankingType);
        titleBar = findViewById(R.id.ranking_titleBar);
        customTabLayout = findViewById(R.id.ranking_customTabLayout);
        btnRankingType = findViewById(R.id.ranking_btnRankingType);
        btnGoToMyPosition = findViewById(R.id.ranking_btnGoToMyPosition);
        viewEmpty = findViewById(R.id.ranking_viewEmpty);
        btnGlobalRanking = findViewById(R.id.ranking_btnGlobalRanking);
        btnLocalRanking = findViewById(R.id.ranking_btnLocalRanking);
        imgGlobalRankingCheck = findViewById(R.id.ranking_imgGlobalRankingCheck);
        imgLocalRankingCheck = findViewById(R.id.ranking_imgLocalRankingCheck);
        recycler = findViewById(R.id.ranking_recycler);
        btnScrollUp = findViewById(R.id.ranking_btnScrollUp);
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

        customTabLayout.configure(tabSelected, this::manageTabChanged, DAILY, WEEKLY, MONTHLY);

        btnRankingType.setOnClickListener(v -> showLayoutRankingType());

        btnGoToMyPosition.setOnClickListener(v -> goToMyPosition());

        viewEmpty.setOnClickListener(v -> {
            //do nothing. This is a hack to avoid scroll the list when layoutRankingType is shown
        });

        btnGlobalRanking.setOnClickListener(v -> manageRankingTypeSelected(GLOBAL));

        btnLocalRanking.setOnClickListener(v -> manageRankingTypeSelected(LOCAL));

        btnScrollUp.setOnClickListener(view -> recycler.smoothScrollToPosition(0));

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollY = scrollY + dy;
                if (scrollY > 0) {
                    btnScrollUp.setVisibility(View.VISIBLE);
                } else {
                    btnScrollUp.setVisibility(View.GONE);
                }
            }
        });
    }

    private void goToMyPosition() {
        if (myPosition != -1) {
            moveListToMyPosition();
        } else {
            getMyPosition();
        }
    }

    private void moveListToMyPosition() {
        if (myPosition != -1) {
            recycler.smoothScrollToPosition(myPosition);
        }
    }

    private void manageRankingTypeSelected(int rankingType) {
        rankingTypeSelected = rankingType;
        manageRankingTypeSelectedUI();
        hideLayoutRankingType();
        resetParams();
        getRankingInfo();
    }

    private void manageRankingTypeSelectedUI() {
        imgGlobalRankingCheck.setVisibility(rankingTypeSelected == GLOBAL ? View.VISIBLE : View.INVISIBLE);
        imgLocalRankingCheck.setVisibility(rankingTypeSelected == LOCAL ? View.VISIBLE : View.INVISIBLE);
        btnRankingType.setText(rankingTypeSelected == GLOBAL ? R.string.global_ranking : R.string.local_ranking);
    }

    private void hideLayoutRankingType() {
        layoutRankingType.setVisibility(View.GONE);
    }

    private void showLayoutRankingType() {
        layoutRankingType.setVisibility(View.VISIBLE);
    }

    private void manageTabChanged(int tabSelected) {
        this.tabSelected = tabSelected;
        resetParams();
        getRankingInfo();
    }

    private void configureTitle() {
        switch (rankingMode) {
            case Constants.RankingMode.BEST:
                titleBar.setTitle(R.string.best_drive_ranking);
                break;
            case Constants.RankingMode.MILEAGE:
                titleBar.setTitle(R.string.mileage_ranking);
                break;
            case Constants.RankingMode.ECO:
                titleBar.setTitle(R.string.eco_driving_ranking);
                break;
            case Constants.RankingMode.SAFETY:
                titleBar.setTitle(R.string.safety_driving_ranking);
                break;
        }
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new RankingAdapter(this, position -> {
            Community.UserCommunity userCommunity = adapter.getUserCommunity(position);
            openProfile(userCommunity);
        });
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int lastVisibleItemIndex;
                boolean isGoingDown = dy > 0;
                boolean isGoingUp = !isGoingDown;
                if (isGoingDown) { //is scrolling down
                    lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
                } else { // is scrolling up
                    lastVisibleItemIndex = layoutManager.findFirstVisibleItemPosition();
                }
                if (!retrievingNewPage && lastVisibleItemIndex != -1) {
                    if (isGoingDown && isLastItemOnPage(lastVisibleItemIndex)) {
                        page = getPageOfAnItem(lastVisibleItemIndex) + 1;
                        retrievingNewPage = true;
                        getRankingInfo();
                    } else if (isGoingUp && isFirstItemOnPage(lastVisibleItemIndex)) {
                        page = getPageOfAnItem(lastVisibleItemIndex) - 1;
                        retrievingNewPage = true;
                        lastIndexWhenScrollingUp = layoutManager.findLastVisibleItemPosition() + PAGE_SIZE - 1; //-1 because it's the row that we just stopped seeing
                        getRankingInfo();
                    }
                }
            }

            private boolean isLastItemOnPage(int lastVisibleItemIndex) {
                boolean isLastItemInList = lastVisibleItemIndex == adapter.getItemCount() - 1;
                return (adapter.getItem(lastVisibleItemIndex).getNumber() % PAGE_SIZE == 0)
                        && isLastItemInList;
            }

            private boolean isFirstItemOnPage(int firstVisibleItemIndex) {
                int pageOfCurrentItem = getPageOfAnItem(firstVisibleItemIndex);
                int pageOfPreviousItem = getPageOfAnItem(firstVisibleItemIndex - 1);
                return pageOfCurrentItem != -1 && pageOfPreviousItem != -1 &&
                        pageOfCurrentItem - pageOfPreviousItem > 1;
            }

            private int getPageOfAnItem(int itemIndex) {
                UserInRankingItem item = adapter.getItem(itemIndex);
                if (item == null) return -1;
                int numPage;
                if (item.getNumber() % PAGE_SIZE != 0) {
                    numPage = item.getNumber() / PAGE_SIZE + 1;
                } else {
                    numPage = item.getNumber() / PAGE_SIZE;
                }
                return numPage;
            }
        });
    }

    private void openProfile(Community.UserCommunity user) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Constants.Extras.USER, user);
        startActivity(intent);
    }

    private void resetParams() {
        page = 1;
        myPosition = -1;
        configureAdapter();
    }

    private void getRankingInfo() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            String endpoint = getEndPoint();
            GetRankingInfoTask getRankingInfoTask = new GetRankingInfoTask();
            getRankingInfoTask.execute(endpoint);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void getMyPosition() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            String endpoint = Endpoints.RANKING_POSITION;
            GetRankingInfoTask getRankingInfoTask = new GetRankingInfoTask();
            getRankingInfoTask.execute(endpoint);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private String getEndPoint() {
        switch (rankingMode) {
            case Constants.RankingMode.BEST:
                return Endpoints.RANKING;
            case Constants.RankingMode.MILEAGE:
                return Endpoints.MILEAGE;
            case Constants.RankingMode.ECO:
                return Endpoints.ECO;
            case Constants.RankingMode.SAFETY:
                return Endpoints.SAFETY;
        }
        return "";
    }

    private boolean isFirstPage() {
        return page == 1;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetRankingInfoTask extends AsyncTask<String, Void, List<UserInRankingItem>> {
        private ProgressDialog progressDialog = new ProgressDialog(RankingActivity.this);
        private String endpoint;
        private Shared.OTCStatus m_status;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFirstPage()) {
                progressDialog.setMessage(getString(R.string.loading));
                if (!Utils.isActivityFinish(RankingActivity.this)) {
                    progressDialog.show();
                }
            }
        }

        @Override
        protected List<UserInRankingItem> doInBackground(String... strings) {
            List<UserInRankingItem> userInRankingItems = new ArrayList<>();
            endpoint = strings[0];
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                MyDrive.Ranking.Builder rankingBuilder = MyDrive.Ranking.newBuilder();
                General.TimeType typeTime = Constants.TimeType.fromIntToTimeType(tabSelected);

                MyDrive.RankingType rankingType = Constants.RankingType.fromIntToRankingType(rankingTypeSelected);
                rankingBuilder.setTypeTime(typeTime);
                rankingBuilder.setTypeRanking(rankingType);
                rankingBuilder.setPage(page);

                MyDrive.RankingResponse response = ApiCaller.doCall(endpoint, msp.getBytes("token"), rankingBuilder.build(), MyDrive.RankingResponse.class);

                if (response != null) {
                    page = response.getPage();
                    List<MyDrive.UserRanking> userRankingList = response.getUsersList();
                    for (int i = 0; i < userRankingList.size(); i++) {
                        MyDrive.UserRanking userRanking = userRankingList.get(i);
                        UserInRankingItem userInRankingItem = new UserInRankingItem(
                                userRanking.getId(),
                                userRanking.getPosition(),
                                userRanking.getNumber(),
                                userRanking.getName(),
                                userRanking.getImage(),
                                userRanking.getProfileType(),
                                userRanking.getFriend()
                        );
                        userInRankingItems.add(userInRankingItem);
                    }
                }
            } catch (ApiCaller.OTCException e) {
                if (e.getStatus() != Shared.OTCStatus.USER_NOT_RANKED) {
                    e.printStackTrace();
                }

                m_status = e.getStatus();
            }
            return userInRankingItems;
        }

        @Override
        protected void onPostExecute(List<UserInRankingItem> userInRankingItems) {
            super.onPostExecute(userInRankingItems);
            progressDialog.dismiss();
            if (m_status != Shared.OTCStatus.USER_NOT_RANKED)
            {
                retrievingNewPage = false;
                showRankingList(userInRankingItems);
                updateMyPosition();
                if (endpoint.equals(Endpoints.RANKING_POSITION)) {
                    moveListToMyPosition();
                }
            }
            else
            {
                runOnUiThread(() -> {
                    try {
                        if (!Utils.isActivityFinish(RankingActivity.this)) {
                            CustomDialog cd = new CustomDialog(RankingActivity.this, CloudErrorHandler.handleError(Shared.OTCStatus.USER_NOT_RANKED), true);
                            cd.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void updateMyPosition() {
        long myUserId = PrefsManager.getInstance().getMyUserId(getApplicationContext());
        List<UserInRankingItem> items = adapter.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == myUserId) {
                myPosition = i;
            }
        }
    }

    private void showRankingList(List<UserInRankingItem> userInRankingItems) {
        if (isFirstPage()) {
            adapter.clearItems();
        }
        adapter.addItems(userInRankingItems);
        adapter.notifyDataSetChanged();
        if (lastIndexWhenScrollingUp != -1) {
            recycler.scrollToPosition(lastIndexWhenScrollingUp);
            lastIndexWhenScrollingUp = -1;
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutRankingType.getVisibility() == View.VISIBLE) {
            hideLayoutRankingType();
            return;
        }
        super.onBackPressed();
    }
}
