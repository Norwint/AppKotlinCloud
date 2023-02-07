package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Community;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.ProfileActivity;
import com.otcengineering.white_app.adapter.UsersAdapter;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;

import java.util.List;

/**
 * Created by cenci7
 */

public class FriendsFragment extends BaseFragment {
    private RecyclerView recycler;

    private UsersAdapter adapter;

    private int page = 1;
    private boolean retrievingNewPage = false;

    private Community.UserCommunity user;

    public void setUser(Community.UserCommunity user) {
        this.user = user;
        if (isVisible()) {
            getUserFriends();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);
        retrieveViews(v);
        configureAdapter();
        getUserFriends();
        return v;
    }

    private void retrieveViews(View v) {
        recycler = v.findViewById(R.id.friends_recycler);
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(layoutManager);
        adapter = new UsersAdapter(this, new UsersAdapter.UserSelectedListener() {
            @Override
            public void onUserSelected(int position) {
                Community.UserCommunity user = adapter.getItem(position);
                openProfile(user);
            }

            @Override
            public void onUnfriend(int position) {

            }
        });
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
                if (dy > 0 && !retrievingNewPage) {
                    if ((visibleItemCount + lastVisibleItemIndex) >= totalItemCount) {
                        page++;
                        retrievingNewPage = true;
                        getUserFriends();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserFriends();
    }

    private void getUserFriends() {
        if (ConnectionUtils.isOnline(getContext())) {
            GetUserFriendsTask getUsersInfoTask = new GetUserFriendsTask();
            getUsersInfoTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    private void openProfile(Community.UserCommunity user) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.Extras.USER, user);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetUserFriendsTask extends AsyncTask<String, Void, List<Community.UserCommunity>> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (page == 1) {
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.show();
            }
        }

        @Override
        protected List<Community.UserCommunity> doInBackground(String... strings) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());

                Community.UserFriends.Builder builder = Community.UserFriends.newBuilder();
                builder.setUserId(user.getUserId());
                builder.setPage(page);

                Community.UserFriendsResponse res = ApiCaller.doCall(Endpoints.USER_FRIENDS, msp.getBytes("token"), builder.build(), Community.UserFriendsResponse.class);
                return res.getUsersList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Community.UserCommunity> result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            retrievingNewPage = false;
            if (result != null) {
                showUserList(result);
            }
        }
    }

    private void showUserList(List<Community.UserCommunity> users) {
        adapter.clearItems();
        adapter.addItems(users);
        adapter.notifyDataSetChanged();
    }
}