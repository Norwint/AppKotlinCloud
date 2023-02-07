package com.otcengineering.white_app.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomTabLayout;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.fragments.FriendsFragment;
import com.otcengineering.white_app.fragments.InfoFragment;
import com.otcengineering.white_app.fragments.PostMenuFragment;
import com.otcengineering.white_app.fragments.PostsFragment;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

/**
 * Created by cenci7
 */

public class ProfileActivity extends BaseActivity {
    private static final int TAB_INFO = 1;
    private static final int TAB_POSTS = 2;
    private static final int TAB_FRIENDS = 3;

    private TitleBar titleBar;
    private CustomTabLayout customTabLayout;
    private LinearLayout layoutMenuUser;
    private View viewEmpty;
    private TextView btnBlockOrUnblock;
    private TextView btnCancel;

    private Community.UserCommunity user;

    private GetUserFriendsTask getUserFriendsTask;

    private boolean isMenuPostShown;

    public ProfileActivity() {
        super("ProfileActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        retrieveViews();
        retrieveExtras();
        setEvents();
        getUserFriends();
    }

    private void retrieveViews() {
        titleBar = findViewById(R.id.profile_titleBar);
        customTabLayout = findViewById(R.id.profile_customTabLayout);
        layoutMenuUser = findViewById(R.id.profile_layoutMenuUser);
        viewEmpty = findViewById(R.id.profile_viewEmpty);
        btnBlockOrUnblock = findViewById(R.id.profile_btnBlockOrUnblock);
        btnCancel = findViewById(R.id.profile_btnCancel);
    }

    private void retrieveExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            user = (Community.UserCommunity) getIntent().getExtras().getSerializable(Constants.Extras.USER);
            if (user != null) {
                titleBar.setTitle(user.getName());
            }
        }
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

        customTabLayout.configure(this::manageTabChanged, TAB_INFO, TAB_POSTS, TAB_FRIENDS);

        viewEmpty.setOnClickListener(v -> {
            //do nothing. This is a hack to avoid scroll the list when layoutRankingType is shown
        });

        btnBlockOrUnblock.setOnClickListener(view -> {
            if (!isMyUser()) {
                blockUser();
            }
        });

        btnCancel.setOnClickListener(view -> hideMenuUser());
    }

    private boolean isMyUser() {
        long myUserId = PrefsManager.getInstance().getMyUserId(getApplicationContext());
        return user == null || user.getUserId() == myUserId;
    }

    private void blockUser() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            BlockOrUnblockUserTask blockOrUnblockUserTask = new BlockOrUnblockUserTask();
            blockOrUnblockUserTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class BlockOrUnblockUserTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                Community.BlockUser.Builder builder = Community.BlockUser.newBuilder();
                builder.setUserId(user.getUserId());

                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.BLOCK_USER, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                return response.getStatusValue();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == Shared.OTCStatus.SUCCESS_VALUE) {
                showCustomDialog(R.string.user_blocked);
            } else {
                showCustomDialogError();
            }
            hideMenuUser();
        }
    }

    private void showMenuUser() {
        layoutMenuUser.setVisibility(View.VISIBLE);
        viewEmpty.setBackgroundColor(getResources().getColor(R.color.colorPrimaryTrans));
    }

    private void hideMenuUser() {
        layoutMenuUser.setVisibility(View.GONE);
    }

    private void manageTabChanged(int tabSelected) {
        switch (tabSelected) {
            case TAB_INFO:
                InfoFragment infoFragment = new InfoFragment();
                infoFragment.configure(user, this::showMenuUser);
                changeFragment(infoFragment);
                break;
            case TAB_POSTS:
                PostsFragment postsFragment = new PostsFragment();
                postsFragment.configure(user, this::showMenuPost);
                changeFragment(postsFragment);
                break;
            case TAB_FRIENDS:
                FriendsFragment friendsFragment = new FriendsFragment();
                friendsFragment.setUser(user);
                changeFragment(friendsFragment);
                break;
        }
    }

    private void showMenuPost(Object object) {
        isMenuPostShown = true;
        PostMenuFragment postMenuFragment = new PostMenuFragment();
        postMenuFragment.setPostSelected(object);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profile_contentMenu, postMenuFragment)
                .commit();
    }

    private void hideMenuPost() {
        isMenuPostShown = false;
        Fragment menuFragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
        getSupportFragmentManager()
                .beginTransaction()
                .remove(menuFragment)
                .commit();
    }

    private void changeFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.profile_layoutContainer, fragment)
                    .commit();
        } catch (Exception e) {
            //Log.e(TAG, "Exception", e);
        }
    }

    private void getUserFriends() {
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            getUserFriendsTask = new GetUserFriendsTask();
            getUserFriendsTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetUserFriendsTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

                Community.UserFriends.Builder builder = Community.UserFriends.newBuilder();
                builder.setUserId(user.getUserId());
                builder.setPage(1);

                Community.UserFriendsResponse res = ApiCaller.doCall(Endpoints.USER_FRIENDS, msp.getBytes("token"), builder.build(), Community.UserFriendsResponse.class);
                return res.getTotal();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer != null) {
                String tabText = getString(R.string.friends) + " (" + integer + ")";
                customTabLayout.setTextTab3(tabText);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getUserFriendsTask != null) {
            getUserFriendsTask.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutMenuUser.getVisibility() == View.VISIBLE) {
            hideMenuUser();
            return;
        }

        if (isMenuPostShown) {
            hideMenuPost();
            return;
        }

        super.onBackPressed();
    }
}
