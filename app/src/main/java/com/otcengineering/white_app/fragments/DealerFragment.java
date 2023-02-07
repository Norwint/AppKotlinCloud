package com.otcengineering.white_app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Community;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.ConnecTechDealerAdapter;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.interfaces.ShowMenuPostsListener;
import com.otcengineering.white_app.network.CommunityNetwork;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.serialization.pojo.OemDealerItem;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.PrefsManager;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

public class DealerFragment extends BaseFragment {

    private Button btnGeneral, btnMessageToMe;
    private TextView txtName, txtPhoneAndEmail, txtAddress;
    private RecyclerView recycler;
    private ConstraintLayout callToDealer;
    private TextView likes, posts;
    private String m_phone;

    private ConnecTechDealerAdapter adapter;

    private int mode = Constants.DatsunDealer.GENERAL;

    private int page = 1;
    private boolean retrievingNewPage = false;
    private boolean fromCache = false;

    boolean oem = false;

    private ShowMenuPostsListener listener;

    public void setListener(ShowMenuPostsListener listener) {
        this.listener = listener;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dealer, container, false);
        retrieveViews(v);
        setEvents();

        configureAdapter();
        getDealerInfoFromDb();
        getPostsFromDb();
        showPostStatsCache();

        initializeButtonSelection();
        getDealerInfo();
        getPostsInfo();
        return v;
    }

    private void getPostsInfo() {
        TypedTask<Community.PostStats> getPostStats = new TypedTask<>(Endpoints.POST_STATS, null, true, Community.PostStats.class,
            new TypedCallback<Community.PostStats>() {
                @Override
                public void onSuccess(@Nonnull @NonNull Community.PostStats value) {
                    showPostStats(value);
                }

                @Override
                public void onError(@NonNull Shared.OTCStatus status, String str) {

                }
            });
        getPostStats.execute();
    }

    private void showPostStats(Community.PostStats value) {
        String strPost = String.format(Locale.US, "%d/%d", value.getMonthlyPosts(), value.getTotalPosts());
        String strLikes = String.format(Locale.US, "%d/%d", value.getMonthlyLikes(), value.getTotalLikes());

        posts.setText(strPost);
        likes.setText(strLikes);

        MySharedPreferences msp = MySharedPreferences.createDefault(getContext());
        msp.putString("PostStatsPosts", strPost);
        msp.putString("PostStatsLikes", strLikes);
    }

    private void showPostStatsCache() {
        MySharedPreferences msp = MySharedPreferences.createDefault(getContext());

        String strPost = msp.getString("PostStatsPosts");
        String strLikes = msp.getString("PostStatsLikes");

        posts.setText(strPost);
        likes.setText(strLikes);

    }

    private void retrieveViews(View v) {
        btnGeneral = v.findViewById(R.id.dealer_btnGeneral);
        btnMessageToMe = v.findViewById(R.id.dealer_btnMessageToMe);
        txtName = v.findViewById(R.id.dealer_txtName);
        txtPhoneAndEmail = v.findViewById(R.id.dealer_txtPhoneAndEmail);
        txtAddress = v.findViewById(R.id.dealer_txtAddress);
        recycler = v.findViewById(R.id.dealer_recycler);
        callToDealer = v.findViewById(R.id.callToDealer);
        likes = v.findViewById(R.id.likes);
        posts = v.findViewById(R.id.posts);
    }

    private void setEvents() {
        btnGeneral.setOnClickListener(view -> changeMode(Constants.DatsunDealer.GENERAL, view));

        btnMessageToMe.setOnClickListener(view -> changeMode(Constants.DatsunDealer.MESSAGES_TO_ME, view));

        callToDealer.setOnClickListener(v -> {
            DialogYesNo dyn = new DialogYesNo(getContext(), getString(R.string.you_call_dealer), this::callDealer, () -> {});
            dyn.show();
        });
    }

    private void callDealer() {
        try {
            if (!Utils.isActivityFinish(getActivity())) {
                try {
                    String uri = "tel:" + m_phone;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeMode(int mode, View view) {
        resetPage();
        this.mode = mode;
        unselectAll();
        view.setSelected(true);
        getPostsFromDb();
    }

    private void unselectAll() {
        btnGeneral.setSelected(false);
        btnMessageToMe.setSelected(false);
    }

    private void configureAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(layoutManager);
        adapter = new ConnecTechDealerAdapter(getActivity(), !oem, mode == Constants.DatsunDealer.MESSAGES_TO_ME, listener);
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!fromCache && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (!retrievingNewPage) {
                            retrievingNewPage = true;
                            page++;
                            getPostInfo();
                        }
                    }
                }
            }
        });
    }

    private void initializeButtonSelection() {
        if (mode == Constants.DatsunDealer.GENERAL) {
            btnGeneral.setSelected(true);
        } else {
            btnMessageToMe.setSelected(true);
        }
    }

    private void getDealerInfo() {
        if (ConnectionUtils.isOnline(getContext())) {
            CommunityNetwork.getDealer(this::showDealerInfo);
        } else {
            ConnectionUtils.showOfflineToast();
            getDealerInfoFromDb();
        }
    }

    private void getDealerInfoFromDb() {
        Community.DealerResponse dealerInfo = PrefsManager.getInstance().getDealerInfo(getContext());
        showDealerInfo(dealerInfo);
    }

    private void showDealerInfo(Community.DealerResponse result) {
        if (result == null) {
            return;
        }

        txtName.setText(result.getName());
        txtPhoneAndEmail.setText(String.format("%s / %s", result.getPhone(), result.getEmail()));
        txtAddress.setText(String.format("%s / %s", result.getAddress(), result.getCountry()));
        m_phone = result.getPhone();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetPage();
    }

    private void resetPage() {
        page = 1;
    }

    private String getUrl() {
        if (oem) {
            if (mode == 0) {
                return Endpoints.CONNECTECH_POSTS;
            } else {
                return Endpoints.CONNECTECH_MESSAGES;
            }
        } else {
            if (mode == 0) {
                return Endpoints.DEALER_POSTS;
            } else {
                return Endpoints.DEALER_MESSAGES;
            }
        }
    }

    private void getPostInfo() {
        if (ConnectionUtils.isOnline(getContext())) {
            General.Page page = General.Page.newBuilder().setPage(this.page).build();
            GenericTask getPosts = new GenericTask(getUrl(), page, true, otcResponse -> {
                if (otcResponse.getStatus() == Shared.OTCStatus.SUCCESS) {
                    int type = (oem ? 0b00 : 0b10) | mode;
                    fromCache = false;
                    if (oem) {
                        Community.ConnecTechPosts mmcPosts = otcResponse.getData().unpack(Community.ConnecTechPosts.class);
                        showPostList(mmcPosts.getPostsList());

                        ImageUtils.deletePosts(type);

                        for (Community.ConnecTechPost post : mmcPosts.getPostsList()) {
                            String json = Utils.getGson().toJson(post);
                            ImageUtils.putPost(type, "pst_" + post.getId(), json);
                        }
                        if (mode == 0) {
                            PrefsManager.getInstance().saveDatsunPosts(mmcPosts, getContext());
                        } else {
                            PrefsManager.getInstance().saveDatsunMessages(mmcPosts, getContext());
                        }
                    } else {
                        Community.DealerPosts dePosts = otcResponse.getData().unpack(Community.DealerPosts.class);
                        if (mode == 0) {
                            PrefsManager.getInstance().saveDealerPosts(dePosts, getContext());
                        } else {
                            PrefsManager.getInstance().saveDealerMessages(dePosts, getContext());
                        }
                        showPostListDealer(dePosts.getPostsList());
                    }
                }
            });
            getPosts.execute();
        } else {
            ConnectionUtils.showOfflineToast();
            getPostsFromDb();
        }
    }

    private void getPostsFromDb() {
        adapter.clearItems();
        adapter.notifyDataSetChanged();

        Utils.runOnBackThread(() -> {
            fromCache = true;
            int type = (oem ? 0b00 : 0b10) | mode;
            List<String> posts = ImageUtils.getKeysFromType(type);
            for (String post : posts) {
                String success = ImageUtils.getPost(type, post);
                if (oem) {
                    Community.ConnecTechPost pst = Utils.getGson().fromJson(success, Community.ConnecTechPost.class);
                    adapter.addItem(new OemDealerItem(pst));
                } else {
                    Community.DealerPost pst = Utils.getGson().fromJson(success, Community.DealerPost.class);
                    adapter.addItem(new OemDealerItem(pst));
                }
            }
            Utils.runOnMainThread(() -> {
                adapter.notifyDataSetChanged();
                getPostInfo();
            });
        });
    }


    private boolean isFirstPage() {
        return page == 1;
    }

    private void showPostListDealer(List<Community.DealerPost> dealerPosts) {
        if (dealerPosts == null) {
            return;
        }

        if (isFirstPage()) {
            adapter.clearItems();
        }
        List<OemDealerItem> oemDealerItems = new ArrayList<>();
        for (Community.DealerPost dealerPost : dealerPosts) {
            OemDealerItem oemDealerItem = new OemDealerItem(dealerPost);
            oemDealerItems.add(oemDealerItem);
        }
        adapter.addItems(oemDealerItems);
        adapter.notifyDataSetChanged();
    }

    private void showPostList(List<Community.ConnecTechPost> connectechPosts) {
        if (connectechPosts == null) {
            return;
        }

        if (isFirstPage()) {
            adapter.clearItems();
        }
        List<OemDealerItem> oemDealerItems = new ArrayList<>();
        for (Community.ConnecTechPost connectechPost : connectechPosts) {
            OemDealerItem oemDealerItem = new OemDealerItem(connectechPost);
            oemDealerItems.add(oemDealerItem);
        }
        adapter.addItems(oemDealerItems);
        adapter.notifyDataSetChanged();
    }
}

