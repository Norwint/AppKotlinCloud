package com.otcengineering.white_app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.BadgeAdapter;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.serialization.models.Badge;

import java.util.ArrayList;

public class BadgesActivity extends BaseActivity {

    public static final int TAB_MILEAGE = 0;
    public static final int TAB_ECO_DRIVING = 1;
    public static final int TAB_LOCAL_RANK = 2;
    public static final int TAB_BEHAVIOR = 3;

    private static final int YOU_HAVE = Constants.BadgesMode.YOU_HAVE;

    private CustomTabLayout customTabLayout;

    private TitleBar titleBar;

    private RecyclerView recyclerViewBadges;
    private BadgeAdapter mAdapter;

    private int badgesModeSelected = 0;
    private int badgeTabSelected = TAB_MILEAGE;
    public static ArrayList<Badge> badgesList = new ArrayList<>();
    public static ArrayList<Badge> badgesListMileage = new ArrayList<>();
    public static ArrayList<Badge> badgesListEcoDriving = new ArrayList<>();
    public static ArrayList<Badge> badgesListLocalRank = new ArrayList<>();
    public static ArrayList<Badge> badgesListBehavior = new ArrayList<>();

    public BadgesActivity() {
        super("Badges");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);
        badgesModeSelected = Constants.BadgesMode.VIEW_ALL;
        retrieveViews();
        initBadges();
        setEvents();
    }

    private void retrieveViews() {
        customTabLayout = findViewById(R.id.badges_customTabLayout);
        titleBar = findViewById(R.id.badges_titleBar);
        recyclerViewBadges = findViewById(R.id.badges_listBadges);
    }

    private void setEvents() {
        customTabLayout.configure(this::manageTabChanged, TAB_MILEAGE, TAB_ECO_DRIVING, TAB_LOCAL_RANK, TAB_BEHAVIOR);
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

    private void manageTabChanged(int tabSelect) {
        switch (tabSelect) {
            case TAB_MILEAGE:
                badgesList = badgesListMileage;
                break;
            case TAB_ECO_DRIVING:
                badgesList = badgesListEcoDriving;
                break;
            case TAB_LOCAL_RANK:
                badgesList = badgesListLocalRank;
                break;
            case TAB_BEHAVIOR:
                badgesList = badgesListBehavior;
                break;
        }
        recyclerViewBadges.stopScroll();
        recyclerViewBadges.scrollToPosition(0);
        managesBadgesTypeSelected(badgesModeSelected, badgesList);
    }

    private void managesBadgesTypeSelected(int badgesMode, ArrayList<Badge> badgeArrayList) {
        badgesModeSelected = badgesMode;

        if (badgeArrayList == null) {
            return;
        }
        if (badgesModeSelected == YOU_HAVE) {
            ArrayList<Badge> filtre = new ArrayList<>();
            for (Badge b : badgeArrayList) {
                if (b.getState() == 1) {
                    filtre.add(b);
                }
            }
            mAdapter.setBadgeList(filtre);
        } else {
            mAdapter.setBadgeList(badgeArrayList);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void initBadges() {
        switch (badgeTabSelected) {
            case TAB_MILEAGE:
                badgesList = badgesListMileage;
                break;
            case TAB_ECO_DRIVING:
                badgesList = badgesListEcoDriving;
                break;
            case TAB_LOCAL_RANK:
                badgesList = badgesListLocalRank;
                break;
            case TAB_BEHAVIOR:
                badgesList = badgesListBehavior;
                break;
        }

        if (badgesList == null) {
            badgesList = new ArrayList<>();
        }
        mAdapter = new BadgeAdapter(getBaseContext(), badgesList);
        recyclerViewBadges.setAdapter(mAdapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewBadges.setLayoutManager(layoutManager);
        recyclerViewBadges.setAdapter(mAdapter);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.isBig(position)) {
                    return 2;
                } else {
                    return 1;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isTaskRoot()) {
            Intent intent = new Intent(this, Home2Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
