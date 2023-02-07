package com.otcengineering.white_app.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.SendPostActivity;
import com.otcengineering.white_app.adapter.InvitationAdapter;
import com.otcengineering.white_app.adapter.PostAdapter;
import com.otcengineering.white_app.interfaces.ShowMenuPostsListener;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;

import java.util.List;


public class PostsFragment extends BaseFragment {

    private LinearLayout layoutButtons;
    private View separatorButtons;
    private Button btnGeneral, btnFriends, btnInvitations;
    private RecyclerView recyclerPosts;
    private RecyclerView recyclerInvitations;
    private FrameLayout btnScrollUp;
    private LinearLayout layoutSendPost;
    private Button btnSendPost;

    private PostAdapter adapterPosts;
    private InvitationAdapter adapterInvitations;

    private Community.UserCommunity user;

    private int mode = Constants.Posts.GENERAL;

    private int page = 1;
    private boolean retrievingNewPage = false;

    private ShowMenuPostsListener listener;

    public void configure(Community.UserCommunity user, ShowMenuPostsListener listener) {
        this.user = user;
        this.listener = listener;
        if (isVisible()) {
            manageUI();
        }
    }

    public void setMode(int newMode) {
        mode = newMode;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_posts, container, false);
        retrieveViews(v);
        setEvents();
        configureAdapters();
        initializeButtonSelection();
        manageUI();
        return v;
    }

    private void retrieveViews(View v) {
        layoutButtons = v.findViewById(R.id.posts_layoutButtons);
        separatorButtons = v.findViewById(R.id.posts_separatorButtons);
        btnGeneral = v.findViewById(R.id.posts_btnGeneral);
        btnFriends = v.findViewById(R.id.posts_btnFriends);
        btnInvitations = v.findViewById(R.id.posts_btnInvitations);
        recyclerPosts = v.findViewById(R.id.posts_recyclerPosts);
        recyclerInvitations = v.findViewById(R.id.posts_recyclerInvitations);
        btnScrollUp = v.findViewById(R.id.posts_btnScrollUp);
        layoutSendPost = v.findViewById(R.id.posts_layoutSendPost);
        btnSendPost = v.findViewById(R.id.posts_btnSendPost);
    }

    private void setEvents() {
        btnGeneral.setOnClickListener(view -> changeMode(Constants.Posts.GENERAL, view));

        btnFriends.setOnClickListener(view -> changeMode(Constants.Posts.FRIENDS, view));

        btnInvitations.setOnClickListener(view -> changeMode(Constants.Posts.INVITATIONS, view));

        btnSendPost.setOnClickListener(view -> openSendPost());

        btnScrollUp.setOnClickListener(view -> {
            recyclerInvitations.smoothScrollToPosition(0);
            recyclerPosts.smoothScrollToPosition(0);
        });
    }

    private void openSendPost() {
        Intent intent = new Intent(getActivity(), SendPostActivity.class);
        startActivity(intent);
    }

    private void changeMode(int mode, View view) {
        resetPage();
        this.mode = mode;
        unselectAll();
        view.setSelected(true);
        adapterPosts.clearItems();
        adapterPosts.notifyDataSetChanged();
        adapterInvitations.clearItems();
        adapterInvitations.notifyDataSetChanged();
        getListInfo();
    }

    private void getListInfo() {
        if (mode == Constants.Posts.INVITATIONS) {
            recyclerPosts.setVisibility(View.GONE);
            adapterPosts.clearItems();
            recyclerInvitations.setVisibility(View.VISIBLE);
            getInvitations();
            btnSendPost.setVisibility(View.GONE);
        } else {
            recyclerPosts.setVisibility(View.VISIBLE);
            recyclerInvitations.setVisibility(View.GONE);
            adapterInvitations.clearItems();
            getPostInfo();
            btnSendPost.setVisibility(View.VISIBLE);
        }
    }

    private void unselectAll() {
        btnGeneral.setSelected(false);
        btnInvitations.setSelected(false);
        btnFriends.setSelected(false);
    }

    private boolean isFromCommunications() {
        return user == null;
    }

    private void configureAdapters() {
        configurePostAdapter();
        configureInvitationAdapter();
    }

    private void configurePostAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerPosts.setLayoutManager(layoutManager);
        adapterPosts = new PostAdapter(getActivity(), isFromCommunications(), listener);
        recyclerPosts.setAdapter(adapterPosts);
        recyclerPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                manageButtonScrollUpVisibility(dy);
                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int lastVisibleItemIndex;
                boolean isGoingDown = dy > 0;
                boolean isGoingUp = !isGoingDown;
                if (isGoingDown) { //is scrolling down
                    lastVisibleItemIndex = layoutManager.findLastVisibleItemPosition();
                } else { // is scrolling up
                    lastVisibleItemIndex = layoutManager.findFirstVisibleItemPosition();
                }
                if (!retrievingNewPage && lastVisibleItemIndex != -1) {
                    if (isGoingDown && ((visibleItemCount + lastVisibleItemIndex) >= totalItemCount)) {
                        page++;
                        retrievingNewPage = true;
                        getPostInfo();
                    } else if (isGoingUp && page > 0 && ((visibleItemCount + lastVisibleItemIndex) <= 0)) {
                        page--;
                        retrievingNewPage = true;
                        getPostInfo();
                    }
                }
            }
        });
    }

    private void manageButtonScrollUpVisibility(int dy) {
        btnScrollUp.setVisibility(dy > 0 ? View.VISIBLE : View.GONE);
    }

    private void configureInvitationAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerInvitations.setLayoutManager(layoutManager);
        //to refresh the invitation list content
        adapterInvitations = new InvitationAdapter(getActivity(), this::getInvitations);
        recyclerInvitations.setAdapter(adapterInvitations);
        recyclerInvitations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                manageButtonScrollUpVisibility(dy);

                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (!retrievingNewPage) {
                            retrievingNewPage = true;
                            page++;
                            getInvitations();
                        }
                    }
                }
            }
        });
    }

    private void initializeButtonSelection() {
        switch (mode) {
            case Constants.Posts.GENERAL: btnGeneral.setSelected(true); break;
            case Constants.Posts.FRIENDS: btnFriends.setSelected(true); break;
            case Constants.Posts.INVITATIONS: btnInvitations.setSelected(true); break;
        }
    }

    private void manageUI() {
        layoutButtons.setVisibility(isFromCommunications() ? View.VISIBLE : View.GONE);
        separatorButtons.setVisibility(isFromCommunications() ? View.VISIBLE : View.GONE);
        layoutSendPost.setVisibility(isFromCommunications() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetPage();
        getListInfo();
    }

    private void resetPage() {
        page = 1;
    }

    private void getPostInfo() {
        if (ConnectionUtils.isOnline(getContext())) {
            GetPostInfoTask getPostInfoTask = new GetPostInfoTask();
            getPostInfoTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            getPostsFromDb();
        }
    }

    private void getPostsFromDb() {
        if (mode == Constants.Posts.GENERAL) {
            Community.UserPostsResponse postsGeneralResponse = PrefsManager.getInstance().getPostsGeneral(getContext());
            if (postsGeneralResponse != null) {
                actionsAfterGetPosts(postsGeneralResponse.getPostsList());
            }
        } else if (mode == Constants.Posts.FRIENDS) {
            Community.UserPostsResponse postsFriendsResponse = PrefsManager.getInstance().getPostsFriends(getContext());
            if (postsFriendsResponse != null) {
                actionsAfterGetPosts(postsFriendsResponse.getPostsList());
            }
        } else if (mode == Constants.Posts.INVITATIONS) {
            Community.FriendRequests invitationsResponse = PrefsManager.getInstance().getInvitations(getContext());
            if (invitationsResponse != null) {
                actionsAfterGetInvitations(invitationsResponse.getUsersList());
            }
        }
    }

    private void getInvitations() {
        if (ConnectionUtils.isOnline(getContext())) {
            GetInvitationsTask getInvitationsTask = new GetInvitationsTask();
            getInvitationsTask.execute();
        } else {
            ConnectionUtils.showOfflineToast();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetPostInfoTask extends AsyncTask<String, Void, List<Community.Post>> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFirstPage()) {
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.show();
            }
        }

        @Override
        protected List<Community.Post> doInBackground(String... strings) {
            try {
                MySharedPreferences pref = MySharedPreferences.createLogin(getContext());

                Community.UserPosts.Builder builder = Community.UserPosts.newBuilder();
                builder.setPage(page);

                Community.UserPostsResponse response;
                if (mode == Constants.Posts.GENERAL) {
                    if (isFromCommunications()) {
                        General.Page postPage = General.Page.newBuilder().setPage(page).build();
                        response = ApiCaller.doCall(Endpoints.POSTS, pref.getBytes("token"), postPage, Community.UserPostsResponse.class);
                        Gson gson = new GsonBuilder().create();
                        String generalPostsJson = gson.toJson(response, Community.UserPostsResponse.class);
                        MySharedPreferences.createLogin(getContext()).putString(Constants.Prefs.DB_POSTS_GENERAL, generalPostsJson);
                    } else {
                        builder.setUserId(user.getUserId());
                        response = ApiCaller.doCall(Endpoints.USER_POSTS, pref.getBytes("token"), builder.build(), Community.UserPostsResponse.class);
                    }
                } else {
                    General.Page postPage = General.Page.newBuilder().setPage(page).build();
                    response = ApiCaller.doCall(Endpoints.FRIENDS_POSTS, pref.getBytes("token"), postPage, Community.UserPostsResponse.class);
                    PrefsManager.getInstance().savePostsFriends(response, getContext());
                }

                return response.getPostsList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Community.Post> posts) {
            super.onPostExecute(posts);
            progressDialog.dismiss();

            retrievingNewPage = false;
            actionsAfterGetPosts(posts);
        }
    }

    private void actionsAfterGetPosts(List<Community.Post> posts) {
        if (posts != null) {
            showPostList(posts);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetInvitationsTask extends AsyncTask<String, Void, List<Community.FriendRequest>> {
        private ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFirstPage()) {
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.show();
            }
        }

        @Override
        protected List<Community.FriendRequest> doInBackground(String... strings) {
            try {
                General.Page.Builder builder = General.Page.newBuilder();
                builder.setPage(page);

                Community.FriendRequests response = ApiCaller.doCall(Endpoints.FRIEND_REQUESTS, PrefsManager.getInstance().getToken(getContext()), builder.build(), Community.FriendRequests.class);
                PrefsManager.getInstance().saveInvitations(response, getContext());

                return response.getUsersList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Community.FriendRequest> invitations) {
            super.onPostExecute(invitations);
            progressDialog.dismiss();
            retrievingNewPage = false;
            actionsAfterGetInvitations(invitations);
        }
    }

    private void actionsAfterGetInvitations(List<Community.FriendRequest> invitations) {
        if (invitations != null) {
            showInvitationList(invitations);
        }
    }

    private boolean isFirstPage() {
        return page == 1;
    }

    private void showPostList(List<Community.Post> posts) {
        if (isFirstPage()) {
            adapterPosts.clearItems();
        }
        adapterPosts.addItems(posts);
        
        adapterPosts.notifyDataSetChanged();
    }

    private void showInvitationList(List<Community.FriendRequest> invitations) {
        if (isFirstPage()) {
            adapterInvitations.clearItems();
        }
        adapterInvitations.addItems(invitations);
        adapterInvitations.notifyDataSetChanged();
    }
}
