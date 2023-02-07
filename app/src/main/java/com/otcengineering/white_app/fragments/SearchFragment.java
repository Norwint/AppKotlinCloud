package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.ProfileActivity;
import com.otcengineering.white_app.adapter.FriendsAdapter;
import com.otcengineering.white_app.adapter.UsersAdapter;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.otcengineering.white_app.utils.Constants.Prefs.DB_ALL_USERS;
import static com.otcengineering.white_app.utils.Constants.Prefs.DB_FRIENDS;


public class SearchFragment extends BaseFragment {
    private static final int TAB_ALL = 1;
    private static final int TAB_NEAR = 2;
    private static final int TAB_FRIENDS = 3;

    private Button btnAll, btnNear, btnFriends;
    private RecyclerView recycler;
    private FrameLayout btnScrollUp;
    private TextView txtNoResults;
    private LinearLayout layoutSearch;
    private EditText editSearch;
    private ImageView btnDeleteSearch;
    private LinearLayout layoutMenu;
    private TextView btnCancel, btnSelectAll, btnUnfriend;

    private UsersAdapter adapter;
    private FriendsAdapter adapterFriends;
    private ArrayList<Community.UserCommunity> users = new ArrayList<>();

    private int tab = TAB_ALL;

    private int page = 1;
    //private int pagePrev = page;
    private int pagePrev = 0;
    private boolean retrievingNewPage = false;

    private boolean isEditMode;

    private boolean isSearching;

    private boolean withCache = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        retrieveViews(v);
        setEvents();
        configureAdapter();
        initializeButtonSelection();
        withCache = loadCacheData();
        getUsersInfo();
        return v;
    }

    private void retrieveViews(View v) {
        btnAll = v.findViewById(R.id.search_btnAll);
        btnNear = v.findViewById(R.id.search_btnNear);
        btnFriends = v.findViewById(R.id.search_btnFriends);
        recycler = v.findViewById(R.id.search_recycler);
        btnScrollUp = v.findViewById(R.id.search_btnScrollUp);
        txtNoResults = v.findViewById(R.id.search_txtNoResults);
        layoutSearch = v.findViewById(R.id.search_layoutSearch);
        editSearch = v.findViewById(R.id.search_editSearch);
        btnDeleteSearch = v.findViewById(R.id.search_btnDeleteSearch);
        layoutMenu = v.findViewById(R.id.friends_layoutMenu);
        btnCancel = v.findViewById(R.id.friends_btnCancel);
        btnSelectAll = v.findViewById(R.id.friends_btnSelectAll);
        btnUnfriend = v.findViewById(R.id.friends_btnUnfriend);
    }

    private void setEvents() {
        btnAll.setOnClickListener(this::manageChangeTab);

        btnNear.setOnClickListener(this::manageChangeTab);

        btnFriends.setOnClickListener(this::manageChangeTab);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearching = true;
                String searchedText = editable.toString();
                if (searchedText.isEmpty() || searchedText.length() < 3) {
                    if (btnDeleteSearch.getVisibility() == View.VISIBLE) {
                        btnDeleteSearch.setVisibility(View.INVISIBLE);
                        page = 1;
                        pagePrev = 0;
                        getUsersInfo();
                    }
                } else {
                    btnDeleteSearch.setVisibility(View.VISIBLE);
                    page = 1;
                    pagePrev = 0;
                    getUsersInfo();
                }
            }
        });

        btnDeleteSearch.setOnClickListener(view -> {
            editSearch.setText("");
            getUsersInfo();
        });

        btnCancel.setOnClickListener(view -> hideMenu());

        btnSelectAll.setOnClickListener(view -> {
            adapter.selectAll();
            adapter.notifyDataSetChanged();
        });

        btnUnfriend.setOnClickListener(view -> {
            List<Community.UserCommunity> itemsSelected = adapter.getSelected();
            unfriend(itemsSelected);
        });

        btnScrollUp.setOnClickListener(view -> recycler.smoothScrollToPosition(0));
    }

    private void manageChangeTab(View view) {
        hideMenu();
        pagePrev = 0;
        adapter.clearItems();
        adapter.notifyDataSetChanged();
        switch (view.getId()) {
            case R.id.search_btnAll:
                hideKeyboard();
                tab = TAB_ALL;
                break;
            case R.id.search_btnNear:
                hideKeyboard();
                tab = TAB_NEAR;
                break;
            case R.id.search_btnFriends:
                hideKeyboard();
                tab = TAB_FRIENDS;
                break;
        }
        unselectAll();
        view.setSelected(true);
        layoutSearch.setVisibility(tab == TAB_FRIENDS ? View.GONE : View.VISIBLE);
        page = 1;
        editSearch.setText("");
        withCache = loadCacheData();
        getUsersInfo();
    }

    private void unselectAll() {
        btnAll.setSelected(false);
        btnNear.setSelected(false);
        btnFriends.setSelected(false);
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
                selectUser(position);
            }
        });

        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                manageButtonScrollUpVisibility(dy);

                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int lastVisibleItemIndex;
                boolean isGoingDown = dy > 0;
                if (isGoingDown) { //is scrolling down
                    lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
                } else { // is scrolling up
                    lastVisibleItemIndex = layoutManager.findFirstVisibleItemPosition();
                }
                if (!retrievingNewPage && lastVisibleItemIndex != -1) {
                    if (isGoingDown && ((visibleItemCount + lastVisibleItemIndex) >= totalItemCount) && page > pagePrev) {
                        pagePrev = page;
                        page++;
                        retrievingNewPage = true;
                        getUsersInfo();
                    }
                }
            }
        });
    }

    private void manageButtonScrollUpVisibility(int dy) {
        btnScrollUp.setVisibility(dy > 0 ? View.VISIBLE : View.GONE);
    }

    private void unfriend(List<Community.UserCommunity> friends) {
        if (ConnectionUtils.isOnline(getContext())) {
            List<Long> friendIds = new ArrayList<>();
            for (Community.UserCommunity friend : friends) {
                friendIds.add(friend.getUserId());
            }
            UnfriendTask unfriendTask = new UnfriendTask();
            unfriendTask.execute(friendIds);
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean loadCacheData() {
        String cache = "SearchCache";
        if (tab == TAB_FRIENDS) {
            cache = "SearchFriends";
        } else if (tab == TAB_NEAR) {
            cache = "SearchNear";
        }
        String json = MySharedPreferences.createSocial(getContext()).getString(cache);
        if (!json.equals("")) {
            Type t = new TypeToken<List<Community.UserCommunity>>() {}.getType();
            List<Community.UserCommunity> users = new Gson().fromJson(json, t);
            showUserList(users);
        }
        return json.equals("");
    }

    @SuppressLint("StaticFieldLeak")
    private class UnfriendTask extends AsyncTask<List<Long>, Void, Integer> {

        @SafeVarargs
        @Override
        protected final Integer doInBackground(List<Long>... params) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());
                Community.Unfriend.Builder builder = Community.Unfriend.newBuilder();
                builder.addAllUsersId(params[0]);

                Shared.OTCResponse response = ApiCaller.doCall(Endpoints.UNFRIEND, msp.getBytes("token"), builder.build(), Shared.OTCResponse.class);
                return response.getStatusValue();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != Shared.OTCStatus.SUCCESS_VALUE) {
                Toast.makeText(getActivity(), R.string.error_default, Toast.LENGTH_SHORT).show();
            }
            hideMenu();
            getUsersInfo();
        }
    }

    private void showMenu() {
        layoutMenu.setVisibility(View.VISIBLE);
        isEditMode = true;
    }

    private void hideMenu() {
        layoutMenu.setVisibility(View.GONE);
        isEditMode = false;
        if (adapter != null) {
            adapter.unselectAll();
            adapter.notifyDataSetChanged();
        }
    }

    private void selectUser(int position) {
        adapter.setSelected(position);
        adapter.notifyDataSetChanged();
        if (adapter.getSelected().size() == 0) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private void selectFriend(int position) {
        adapterFriends.setSelected(position);
        adapterFriends.notifyDataSetChanged();
    }

    private void openProfile(Community.UserCommunity user) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.Extras.USER, user);
        startActivity(intent);
    }

    private void initializeButtonSelection() {
        btnAll.setSelected(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        withCache = loadCacheData();
        getUsersInfo();
    }

    private void getUsersInfo() {
        if (ConnectionUtils.isOnline(getContext())) {
            GetUsersInfoTask getUsersInfoTask = new GetUsersInfoTask();
            getUsersInfoTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            // getInfoFromDb();
        }
    }

    private void getInfoFromDb() {
        switch (tab) {
            case TAB_ALL:
                Community.SearchUsersResponse allUsersResponse = PrefsManager.getInstance().getAllUsers(getContext());
                if (allUsersResponse != null) {
                    showUsers(allUsersResponse.getUsersList());
                }
                break;
            case TAB_NEAR:
                Community.SearchUsersResponse nearUserResponse = PrefsManager.getInstance().getNearUsers(getContext());
                if (nearUserResponse != null) {
                    showUsers(nearUserResponse.getUsersList());
                }
                break;
            case TAB_FRIENDS:
                Community.FriendsResponse friendsResponse = PrefsManager.getInstance().getFriends(getContext());
                if (friendsResponse != null) {
                    showUsers(friendsResponse.getUsersList());
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetUsersInfoTask extends AsyncTask<String, Void, List<Community.UserCommunity>> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());
        private String text;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (page == 1) {
                progressDialog.setMessage(getString(R.string.loading));
                if (!withCache) progressDialog.show();
            }
            text = editSearch.getText().toString();
        }

        @Override
        protected List<Community.UserCommunity> doInBackground(String... strings) {
            try {
                MySharedPreferences msp = MySharedPreferences.createLogin(getActivity());
                if (tab == TAB_FRIENDS) {
                    if (page > pagePrev) {
                        General.Page.Builder builder = General.Page.newBuilder();
                        builder.setPage(page);

                        Community.FriendsResponse res = ApiCaller.doCall(Endpoints.FRIENDS, msp.getBytes("token"), builder.build(), Community.FriendsResponse.class);
                        PrefsManager.getInstance().saveFriends(res, getContext());
                        return res.getUsersList();
                    }
                } else {
                    if (page > pagePrev) {
                        Community.SearchUsers.Builder builder = Community.SearchUsers.newBuilder();
                        builder.setPage(page);
                        builder.setSearchText(text);


                        Community.SearchUsersResponse res;
                        if (tab == TAB_ALL) {
                            res = ApiCaller.doCall(Endpoints.SEARCH_USERS, msp.getBytes("token"), builder.build(), Community.SearchUsersResponse.class);
                            List<Community.UserCommunity> a = res.getUsersList();
                            if (a.size() == 0) {
                                pagePrev = page;
                            }

                            Gson gson = new GsonBuilder().create();
                            String allUsersJson = gson.toJson(res, Community.SearchUsersResponse.class);
                            MySharedPreferences.createLogin(getContext()).putString(DB_ALL_USERS, allUsersJson);
                        } else {
                            res = ApiCaller.doCall(Endpoints.SEARCH_USERS_NEAR, msp.getBytes("token"), builder.build(), Community.SearchUsersResponse.class);
                            List<Community.UserCommunity> a = res.getUsersList();
                            if (a.size() == 0) {
                                pagePrev = page;
                            }
                            Gson gson = new GsonBuilder().create();
                            String nearUsersJson = gson.toJson(res, Community.SearchUsersResponse.class);
                            MySharedPreferences.createLogin(getContext()).putString(DB_FRIENDS, nearUsersJson);
                        }
                        return res.getUsersList();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                pagePrev = page;
            }
            return Collections.emptyList();
        }

        @Override
        protected void onPostExecute(List<Community.UserCommunity> result) {
            super.onPostExecute(result);
            try {
                if (!withCache) progressDialog.dismiss();
                retrievingNewPage = false;
                if (result != null && result.size() != 0) {
                    String cache = "SearchCache";
                    if (tab == TAB_FRIENDS) {
                        cache = "SearchFriends";
                    } else if (tab == TAB_NEAR) {
                        cache = "SearchNear";
                    }
                    MySharedPreferences.createSocial(getContext()).putString(cache, new Gson().toJson(result));
                    showUsers(result);
                } else {
                    page = pagePrev;
                    if (isSearching) {
                        hideList();
                    }
                }
                isSearching = false;
            } catch (Exception ignored) {

            }
        }
    }

    private void showUsers(List<Community.UserCommunity> result) {
        showList();
        showUserList(result);
    }

    private void showList() {
        recycler.setVisibility(View.VISIBLE);
        txtNoResults.setVisibility(View.GONE);
    }

    private void hideList() {
        recycler.setVisibility(View.GONE);
        txtNoResults.setVisibility(View.VISIBLE);
    }

    private void showUserList(List<Community.UserCommunity> users) {
        if (adapter == null) {
            configureAdapter();
        }
        if (page == 1) {
            adapter.clearItems();
        }
        adapter.addItems(users);
        adapter.notifyDataSetChanged();
    }


    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
