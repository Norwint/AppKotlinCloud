package com.otcengineering.white_app.fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.otc.alice.api.model.General.Page;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.adapter.NotificationAdapter;
import com.otcengineering.white_app.interfaces.FragmentBackPresser;
import com.otcengineering.white_app.interfaces.INotificable;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.ProfileNetwork;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.BadgeUtils;
import com.otcengineering.white_app.utils.Utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

public class NotificationFragment extends EventFragment implements FragmentBackPresser, INotificable {

    private RecyclerView recyclerView;
    public ArrayList<NotificationAdapter.NotificationDummy> dummyList = new ArrayList<>();
    private Button editButton;
    private ConstraintLayout deleteLayout;
    private int RED_COLOR, WHITE_COLOR;
    private View cancelButton, selectAllButton, deleteButton;
    private NotificationAdapter na;

    public boolean updating = false;

    public int page = 1;
    private boolean lock = false;

    public NotificationFragment() {
        super("NotificationActivity");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RED_COLOR = ContextCompat.getColor(getContext(), R.color.colorPrimary);
        WHITE_COLOR = ContextCompat.getColor(getContext(), R.color.colorWhite);
        na = new NotificationAdapter();
        na.setOnItemClickListener(item -> {
            if (!item.wasChecked) {
                ProfileAndSettings.IdUserNotification idu = ProfileAndSettings.IdUserNotification.newBuilder().setId(item.id).build();
                GenericTask gt = new GenericTask(Endpoints.READ_NOTIFICATION, idu, true, (resp) -> {
                    if (resp.getStatus() == Shared.OTCStatus.SUCCESS) {
                        item.wasChecked = true;
                        na.notifyDataSetChanged();
                        ProfileNetwork.getNotificationCount(getContext());
                    }
                });
                gt.execute();
            }
        });
        getNotifications();
    }

    public void getNotifications() {
        Page pg = Page.newBuilder().setPage(page).build();
        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.loading));
        try {
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updating = true;
        GenericTask gt = new GenericTask(Endpoints.GET_USER_NOTIFICATIONS, pg, true, (resp) -> {
            try {
                updating = false;
                pd.dismiss();
                ProfileAndSettings.UserNotifications un = ProfileAndSettings.UserNotifications.parseFrom(resp.getData().getValue());
                if (un.getNotificationListList().size() == 0) {
                    --page;
                    lock = true;
                    new Timer("NotificationTimer").schedule(new TimerTask() {
                        @Override
                        public void run() {
                            lock = false;
                        }
                    }, 15000);
                } else {
                    for (ProfileAndSettings.UserNotification not : un.getNotificationListList()) {
                        boolean found = false;
                        for (NotificationAdapter.NotificationDummy dummy : dummyList) {
                            if (dummy.id == not.getId()) {
                                found = true;
                            }
                        }
                        if (!found) {
                            NotificationAdapter.NotificationDummy nd = na.make(not.getTitle(), not.getDescription(), not.getTimestamp());
                            try {
                                nd.id = not.getId();
                                nd.wasChecked = not.getReaded();
                                nd.img = getBitmapByType(not.getType(), not.getTitle());
                                nd.imgId = getBitmapId(not.getType(), not.getTitle());
                                nd.type = not.getType();
                            } catch (IllegalStateException iae) {
                                iae.printStackTrace();
                            }
                            dummyList.add(nd);
                        }
                    }
                    na.update(dummyList);
                    ProfileNetwork.getNotificationCount(getContext());
                }
            } catch (NullPointerException | InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        });
        gt.execute();
    }

    private Bitmap getBitmapByType(ProfileAndSettings.NotificationType type, String title) {
        int id = getBitmapId(type, title);
        return BitmapFactory.decodeResource(getResources(), id);
    }

    private int getBitmapId(ProfileAndSettings.NotificationType type, String title) {
        int res;
        switch (type) {
            case ERROR: res = R.drawable.error; break;
            case MILEAGE_BADGE: case ECO_BADGE: case RANKING_BADGE: case BEHAVIOUR_BADGE: res = BadgeUtils.getNotificationImageId(title);
                 break;
            case FRIEND: res = R.drawable.post_and_friendship; break;
            case CONNECTECH_POST: case CONNECTECH_MESSAGE: res = R.drawable.icon; break;
            case DEALER_POST: case DEALER_MESSAGE: res = R.drawable.dealer; break;
            case DOORS: res = R.drawable.notification_history5; break;
            case ENGINE: res = R.drawable.notification_history9; break;
            case FUEL: res = R.drawable.vechicle_condition_card2; break;
            case DONGLE: res = R.drawable.dongle_extract; break;
            case UPDATE_DONGLE: res = R.drawable.update_app; break;
            case RESTART_DONGLE: res = R.drawable.restart_dongle; break;
            case VEHICLE_CONDITION: {
                switch (title.replace("VEHICLE_CONDITION_", "")) {
                    case "ABS_FAULT": res = R.drawable.vechicle_condition_card4; break;
                    case "AIRBAG_SYSTEM_FAULT": res = R.drawable.vechicle_condition_card8; break;
                    case "ASC_SYSTEM_FAULT": res = R.drawable.vechicle_condition_card13; break;
                    case "LOW_BRAKE_FLUID": case "BRAKE_FAULT": res = R.drawable.vechicle_condition_card7; break;
                    case "CHARGING_SYSTEM_FAULT": res = R.drawable.vechicle_condition_card6; break;
                    case "ELECTRICAL_SYSTEM_FAULT": res = R.drawable.vechicle_condition_card10; break;
                    case "ENGINE_FAULT": res = R.drawable.vechicle_condition_card14; break;
                    case "IMMOBILIZER_SYSTEM_FAULT": res = R.drawable.vechicle_condition_card3; break;
                    case "KEYLESS_OPERATION_FAULT": res = R.drawable.vechicle_condition_card9; break;
                    case "OIL_PRESSURE": res = R.drawable.vechicle_condition_card5; break;
                    case "POWER_STEERING_FAULT": res = R.drawable.vechicle_condition_card11; break;
                    case "STEERING_LOCK": res = R.drawable.vechicle_condition_card1; break;
                    case "TRANSMISSION_FAULT": res = R.drawable.vechicle_condition_card12; break;
                    default: res = R.drawable.error; break;
                }
            }
            break;
            default: {
                res = R.drawable.error;

            } break;
        }
        if (title.equals("ROUTE")) {
            res = R.drawable.notification_history8;
        } else if (title.equals("VEHICLE_IN_SERVICE_TIMING")) {
            res = R.drawable.notification_history42;
        } else if (title.equals("USER_ALL_SURVEYS_DONE")) {
            res = R.drawable.menu_icons16;
        }
        return res;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notification, container, false);

        fetchVariables(view);
        makeActions();

        return view;
    }

    private void makeActions() {
        editButton.setOnClickListener(v -> {
            boolean activated = editButton.isActivated();
            makeAction(activated);
        });

        cancelButton.setOnClickListener(v -> cancelSelection());
        selectAllButton.setOnClickListener(v -> selectAll());
        deleteButton.setOnClickListener(v -> deleteSelection());

        na.addNotifications(dummyList);
        recyclerView.setAdapter(na);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1) && !lock) {
                    ++page;
                    getNotifications();
                }
            }
        });

        na.notifyDataSetChanged();
    }

    private void fetchVariables(@Nonnull final View view) {
        recyclerView = view.findViewById(R.id.notifications_recycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        editButton = view.findViewById(R.id.edit_button);
        deleteLayout = view.findViewById(R.id.delete_layout);

        cancelButton = view.findViewById(R.id.cancelButton);
        selectAllButton = view.findViewById(R.id.selectButton);
        deleteButton = view.findViewById(R.id.deleteButton);
    }

    private void cancelSelection() {
        makeAction(true);
        deselectAll();
    }

    private void deleteSelection() {
        ArrayList<NotificationAdapter.NotificationDummy> toDelete = new ArrayList<>();

        for (NotificationAdapter.NotificationDummy nd : dummyList) {
            if (nd.selected) {
                ProfileAndSettings.IdUserNotification dun = ProfileAndSettings.IdUserNotification.newBuilder().setId(nd.id).build();
                GenericTask gt = new GenericTask(Endpoints.NOTIFICATIONS_DELETE, dun, true, (resp) -> {});
                gt.execute();
                toDelete.add(nd);
            }
        }
        for (int i = 0; i < toDelete.size(); ++i) {
            dummyList.remove(toDelete.get(i));
        }
        toDelete.clear();
        na.update(dummyList);

        deselectAll();
        makeAction(true);

        ProfileNetwork.getNotificationCount(getContext());
    }

    private void selectAll() {
        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.loading));
        pd.show();
        Thread th = new Thread(() -> {
            int count;
            do {
                try {
                    Page pg = Page.newBuilder().setPage(page++).build();
                    ProfileAndSettings.UserNotifications un = ApiCaller.doCall(Endpoints.GET_USER_NOTIFICATIONS, true, pg, ProfileAndSettings.UserNotifications.class);
                    count = un.getNotificationListCount();
                    for (ProfileAndSettings.UserNotification not : un.getNotificationListList()) {
                        boolean found = false;
                        for (NotificationAdapter.NotificationDummy dummy : dummyList) {
                            if (dummy.id == not.getId()) {
                                found = true;
                            }
                        }
                        if (!found) {
                            NotificationAdapter.NotificationDummy nd = na.make(not.getTitle(), not.getDescription(), not.getTimestamp());
                            try {
                                nd.id = not.getId();
                                nd.wasChecked = not.getReaded();
                                nd.img = getBitmapByType(not.getType(), not.getTitle());
                                nd.imgId = getBitmapId(not.getType(), not.getTitle());
                            } catch (IllegalStateException iae) {
                                iae.printStackTrace();
                            }
                            dummyList.add(nd);
                        }
                    }
                } catch (Exception e) {
                    count = 0;
                }
            } while (count > 0);
            for (NotificationAdapter.NotificationDummy dummy : dummyList) {
                dummy.selected = true;
            }
            Utils.runOnMainThread(() -> {
                na.update(dummyList);
                pd.dismiss();
            });
        });
        th.start();
    }

    private void deselectAll() {
        for (NotificationAdapter.NotificationDummy dummy : dummyList) {
            dummy.selected = false;
        }
        na.update(dummyList);
    }

    private void makeAction(boolean activated) {
        na.deleteMode = !activated;
        editButton.setActivated(!activated);
        deleteLayout.setVisibility(activated ? View.GONE : View.VISIBLE);
        editButton.setBackgroundTintList(!activated ? ColorStateList.valueOf(RED_COLOR) : null);
        editButton.setTextColor(!activated ? WHITE_COLOR : RED_COLOR);
        deselectAll();
    }

    @Override
    public boolean onBackPressed() {
        if (na.deleteMode) {
            cancelSelection();
            return false;
        }
        return true;
    }

    @Override
    public void onNotificationReceived() {
        if (!updating) {
            page = 1;
            Utils.runOnMainThread(() -> {
                dummyList.clear();
                getNotifications();
            });
        }
    }
}
