package com.otcengineering.white_app.payment.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.fragments.BaseFragment;
import com.otcengineering.white_app.payment.adapter.OrderAdapter;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.interfaces.Callback;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import java.util.Calendar;
import java.util.Locale;

import javax.annotation.Nonnull;

public class OrdersFragment extends BaseFragment {
    private Button mNext, mPrevious;
    private ImageButton mInvoices;
    private int mPage = 1;
    private OrderAdapter mAdapter;
    private RecyclerView mRecycler;
    private ProgressDialog mProgressDialog;

    private Callback<OrdersFragment> listener;

    public OrdersFragment(Callback<OrdersFragment> listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_orders, container, false);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.loading));

        findViews(v);
        setEvents();
        setData();

        return v;
    }

    private void setData() {
        getOrders(1);
    }

    public void generateInvoice(Calendar start, Calendar end) {
        if (start == null || end == null) {
            showCustomDialogError("Invalid date range selected. Please, select a valid date range.");
            mProgressDialog.dismiss();
            return;
        }

        mProgressDialog.show();

        String startDate = stringfy(start, false);
        String endDate = stringfy(end, true);

        Payment.Orders orders = Payment.Orders.newBuilder()
                .setInitialDate(startDate)
                .setFinalDate(endDate)
                .build();

        TypedTask<Shared.OTCResponse> task = new TypedTask<>(Endpoints.Payment.SEND_ORDERS_BY_DATE, orders, true, Shared.OTCResponse.class,
                new TypedCallback<Shared.OTCResponse>() {
                    @Override
                    public void onSuccess(@Nonnull Shared.OTCResponse value) {
                        showCustomDialog("The email was sent succesfully!");
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                        showCustomDialogError(CloudErrorHandler.handleError(status));
                        mProgressDialog.dismiss();
                    }
                });
        task.execute();
    }

    public void filter(Calendar start, Calendar end) {
        if (start == null || end == null) {
            mPage = 1;
            getOrders(mPage);
            return;
        }

        String startDate = stringfy(start, false);
        String endDate = stringfy(end, true);

        Payment.Orders orders = Payment.Orders.newBuilder()
                .setInitialDate(startDate)
                .setFinalDate(endDate)
                .build();

        mProgressDialog.show();
        TypedTask<Payment.OrderResponse> task = new TypedTask<>(Endpoints.Payment.ORDERS_BY_DATE, orders, true, Payment.OrderResponse.class,
                new TypedCallback<Payment.OrderResponse>() {
                    @Override
                    public void onSuccess(@Nonnull Payment.OrderResponse value) {
                        mAdapter.setItems(value.getOrdersList());
                        mPrevious.setEnabled(false);
                        mNext.setEnabled(false);
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                        mProgressDialog.dismiss();
                        showCustomDialogError(CloudErrorHandler.handleError(status));
                    }
                });
        task.execute();
    }

    private String stringfy(Calendar cal, boolean isEnd) {
        String fmt = "%04d-%02d-%02d";
        if (isEnd) {
            fmt += " 23:59:59";
        } else {
            fmt += " 00:00:00";
        }

        return String.format(Locale.US, fmt, cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    private void checkButtons(int maxPages) {
        if (mPage > 1) {
            mPrevious.setEnabled(true);
        } else {
            mPrevious.setEnabled(false);
        }
        if (maxPages > mPage) {
            mNext.setEnabled(true);
        } else {
            mNext.setEnabled(false);
        }
    }

    private void getOrders(int page) {
        Payment.Orders orders = Payment.Orders.newBuilder().setPage(page).build();
        mProgressDialog.show();
        TypedTask<Payment.OrderResponse> task = new TypedTask<>(Endpoints.Payment.ORDERS, orders, true, Payment.OrderResponse.class, new TypedCallback<Payment.OrderResponse>() {
            @Override
            public void onSuccess(@Nonnull Payment.OrderResponse value) {
                mAdapter.setItems(value.getOrdersList());
                mProgressDialog.dismiss();
                checkButtons(value.getPages());
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                mProgressDialog.dismiss();
                showCustomDialogError(CloudErrorHandler.handleError(status));
            }
        });
        task.execute();
    }

    private void setEvents() {
        mPrevious.setOnClickListener(v -> getOrders(--mPage));
        mNext.setOnClickListener(v -> getOrders(++mPage));

        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration did = new DividerItemDecoration(mRecycler.getContext(), DividerItemDecoration.VERTICAL);
        did.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.separator_shopping));
        mRecycler.addItemDecoration(did);

        mInvoices.setOnClickListener(v -> {
            try {
                listener.run(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void findViews(View v) {
        mNext = v.findViewById(R.id.next);
        mPrevious = v.findViewById(R.id.previous);
        mRecycler = v.findViewById(R.id.recyclerOrders);
        mInvoices = v.findViewById(R.id.button2);

        mAdapter = new OrderAdapter(getContext());
    }

}
