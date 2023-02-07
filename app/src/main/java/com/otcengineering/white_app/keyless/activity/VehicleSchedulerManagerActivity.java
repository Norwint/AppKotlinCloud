package com.otcengineering.white_app.keyless.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.CalendarView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.google.gson.reflect.TypeToken;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.keyless.adapter.BookingItemAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.serialization.BookingInfo;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class VehicleSchedulerManagerActivity extends BaseActivity {
    private TitleBar mTitleBar;
    private CalendarView mCalendarView;
    private Calendar mCalendar;
    private RecyclerView mRecycler;
    private BookingItemAdapter mAdapter;

    public VehicleSchedulerManagerActivity() {
        super("VehicleSchedulerManagerActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vehicle_scheduler_manager);

        mCalendar = Calendar.getInstance();

        getViews();
        setEvents();
        getBookings();
    }

    private void setEvents() {
        mTitleBar.setListener(new TitleBar.TitleBarListener() {
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

        mCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            mCalendar = Calendar.getInstance();
            mCalendar.set(year, month, dayOfMonth);

            getBookings();
        });

        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecycler.getContext(), LinearLayoutManager.VERTICAL);
        mRecycler.addItemDecoration(dividerItemDecoration);
    }

    private void getBookings() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.loading));
        pd.setCancelable(false);
        pd.show();

        String date = DateUtils.dateTimeToString(LocalDateTime.ofInstant(Instant.ofEpochMilli(mCalendar.getTimeInMillis()), ZoneId.systemDefault()), "yyyy-MM-dd");
        NetTask task = new NetTask("v2/user/booking-list/" + date, true, new NetworkCallback<NetTask.JsonResponse>() {
            @Override
            public void onSuccess(NetTask.JsonResponse response) {
                pd.dismiss();
                Type type = new TypeToken<ArrayList<BookingInfo>>() {}.getType();
                ArrayList<BookingInfo> bids = response.getResponse(type);
                bids = orderByDate(bids);
                mAdapter.clear();
                for (BookingInfo info : bids) {
                    BookingItemAdapter.BookingItem bi = new BookingItemAdapter.BookingItem(info);
                    mAdapter.addItem(bi);
                }
                mAdapter.update();
            }

            @Override
            public void onFailure(int code, String errorMsg) {
                pd.dismiss();
                showCustomDialogError(errorMsg);
            }
        });
        task.execute();
    }

    private ArrayList<BookingInfo> orderByDate(ArrayList<BookingInfo> bids) {
        return (ArrayList<BookingInfo>) Stream.of(bids).sorted((a, b) -> a.getStartDateInMilitarFormat() - b.getStartDateInMilitarFormat()).toList();
    }

    private void getViews() {
        mTitleBar = findViewById(R.id.titleBar);
        mCalendarView = findViewById(R.id.calendarView2);
        mRecycler = findViewById(R.id.recyclerView);

        mAdapter = new BookingItemAdapter();
    }
}
