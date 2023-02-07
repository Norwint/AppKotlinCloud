package com.otcengineering.white_app.payment.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.payment.adapter.OrderItemAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;
import com.otcengineering.white_app.utils.payment.PaymentItem;
import com.otcengineering.white_app.utils.payment.PaymentUtils;

import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

public class OrderActivity extends BaseActivity {
    private long mId;
    private TextView mOrderId, mOrderBasePrice, mOrderTaxPrice, mOrderTotalPrice, mOrderShipPrice;
    private TextView mOrderFirstName, mOrderLastName, mOrderAddress, mOrderCity, mOrderPostalCode, mOrderCounty;
    private TitleBar mTitleBar;
    private OrderItemAdapter mAdapter;
    private RecyclerView mRecycler;
    private ImageView mQrCode;

    public OrderActivity() {
        super("OrderActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
 
        getExtras();
        findViews();
        setEvents();
        getData();
    }

    private ProgressDialog mPd;

    private void getData() {
        mOrderId.setText(String.format(Locale.US, "Order Id: #%019d", mId));

        mPd = new ProgressDialog(this);
        mPd.setCancelable(false);
        mPd.setMessage(getString(R.string.loading));
        mPd.show();

        TypedTask<Payment.OrderData> task = new TypedTask<>(String.format(Locale.US, Endpoints.Payment.ORDER_BY_ID, mId), null, true, Payment.OrderData.class,
                new TypedCallback<Payment.OrderData>() {
            @Override
            public void onSuccess(@Nonnull Payment.OrderData value) {
                String currencyCode = value.getCurrencyCode();
                PaymentUtils.Currency currency = PaymentUtils.Currency.getByCode(currencyCode);

                mOrderBasePrice.setText(String.format(Locale.US, "%01.02f %s", value.getBaseAmount(), currency.getSymbol()));
                mOrderTaxPrice.setText(String.format(Locale.US, "%01.02f %s", value.getTaxAmount(), currency.getSymbol()));
                mOrderTotalPrice.setText(String.format(Locale.US, "%01.02f %s", value.getTotalAmount(), currency.getSymbol()));
                mOrderShipPrice.setText(String.format(Locale.US, "%01.02f %s", value.getShipAmount(), currency.getSymbol()));

                mOrderFirstName.setText(value.getShipName());
                mOrderAddress.setText(value.getShipAddress());
                mOrderCity.setText(value.getShipCity());
                mOrderPostalCode.setText(value.getShipCP());
                mOrderCounty.setText(value.getShipCountry());

                ArrayList<PaymentItem> mItems = new ArrayList<>();

                for (Payment.OrderLine ol : value.getOrderLineList()) {
                    PaymentItem pi = PaymentItem.fromOrderLine(ol, value.getCurrencyCode());
                    mItems.add(pi);
                }

                byte[] bs = value.getCodeQR().toByteArray();
                if (bs != null) {
                    mQrCode.setVisibility(View.VISIBLE);
                    byte[] data = Base64.decode(bs, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Glide.with(OrderActivity.this).load(bmp).into(mQrCode);
                } else {
                    mQrCode.setVisibility(View.GONE);
                }

                mAdapter.setItems(mItems);
                mAdapter.notifyDataSetChanged();

                mPd.dismiss();
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                mPd.dismiss();
                showCustomDialogError(CloudErrorHandler.handleError(status));
            }
        });
        task.execute();
    }

    private void getExtras() {
        Intent intent = getIntent();
        mId = intent.getLongExtra("OrderId", 0);
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

        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        mQrCode.setVisibility(View.GONE);
    }

    private void findViews() {
        mOrderId = findViewById(R.id.orderId);
        mOrderBasePrice = findViewById(R.id.orderBasePrice);
        mOrderTaxPrice = findViewById(R.id.orderTaxPrice);
        mOrderTotalPrice = findViewById(R.id.orderTotalPrice);
        mOrderShipPrice = findViewById(R.id.orderShipPrice);

        mOrderFirstName = findViewById(R.id.orderFirstName);
        mOrderLastName = findViewById(R.id.orderLastName);
        mOrderAddress = findViewById(R.id.orderAddress);
        mOrderCity = findViewById(R.id.orderCity);
        mOrderPostalCode = findViewById(R.id.orderPostalCode);
        mOrderCounty = findViewById(R.id.orderCountry);
        mQrCode = findViewById(R.id.qrCode);

        mTitleBar = findViewById(R.id.titleBar);

        mAdapter = new OrderItemAdapter(this);
        mRecycler = findViewById(R.id.recyclerView);
    }
}
