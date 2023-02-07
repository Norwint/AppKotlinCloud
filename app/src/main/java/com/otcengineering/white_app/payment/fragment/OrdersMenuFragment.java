package com.otcengineering.white_app.payment.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.fragments.BaseFragment;

import java.util.Calendar;

public class OrdersMenuFragment extends BaseFragment {
    private Button mFilter, mInvoice;
    private DateRangeCalendarView mDateRangeCalendarView;
    private OrdersFragment parent;

    public OrdersMenuFragment(OrdersFragment parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu_orders, container, false);
        retrieveViews(v);
        setEvents();
        return v;
    }

    private void retrieveViews(View v) {
        mDateRangeCalendarView = v.findViewById(R.id.dateRangeCalendarView);
        mFilter = v.findViewById(R.id.button5);
        mInvoice = v.findViewById(R.id.button6);
    }

    private void setEvents() {
        mDateRangeCalendarView.resetAllSelectedViews();

        mFilter.setOnClickListener(this::applyFilter);
        mInvoice.setOnClickListener(this::generateInvoice);
    }

    public void generateInvoice(View view) {
        Calendar start = mDateRangeCalendarView.getStartDate();
        Calendar end = mDateRangeCalendarView.getEndDate();

        parent.generateInvoice(start, end);
        closeMenu();
    }

    public void applyFilter(View view) {
        Calendar start = mDateRangeCalendarView.getStartDate();
        Calendar end = mDateRangeCalendarView.getEndDate();

        parent.filter(start, end);
        closeMenu();
    }

    private void closeMenu() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}
