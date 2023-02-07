package com.otcengineering.white_app.payment.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.payment.adapter.ReviewAdapter;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.network.PaymentNetwork;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.payment.PaymentItem;
import com.otcengineering.white_app.utils.payment.PaymentUtils;

import java.util.ArrayList;
import java.util.Locale;

public class ShoppingSummaryActivity extends BaseActivity {
    private TextView mSummaryText, mSubtotal, mBase, mTax, mShipping;
    private RecyclerView mRecyclerView;
    private ReviewAdapter mAdapter;
    private ArrayList<PaymentItem> mItems;
    private Button mGoToMenu;
    private TitleBar mTitleBar;
    private boolean mLastPage;
    private ProgressDialog mProgressDialog;
    private long mOrderId;
    private ImageView mQrCode;

    public ShoppingSummaryActivity() {
        super("ShoppingSummaryActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_summary);

        mItems = new ArrayList<>();

        mOrderId = getIntent().getLongExtra("OrderId",0);
        String qr = getIntent().getStringExtra("OrderQR");

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        getOrderData();

        getViews();
        setEvents();
        fillData();

        if (qr != null) {
            byte[] data = Base64.decode(qr, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            Glide.with(this).load(bmp).into(mQrCode);
        } else {
            mQrCode.setVisibility(View.GONE);
        }
    }

    private void fillData() {
        mSummaryText.setText(String.format(Locale.US, "Order nº #%019d", mOrderId));
    }

    private void getOrderData() {
        PaymentNetwork.getOrder(mOrderId, new Callback<Payment.OrderData>() {
            @Override
            public void onSuccess(Payment.OrderData success) {
                double subtotal = success.getTotalAmount();
                double tax = success.getTaxAmount();
                double base = success.getBaseAmount();
                double shipping = success.getShipAmount();

                mSubtotal.setText(String.format(Locale.US, "%01.02f %s", subtotal, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                mShipping.setText(String.format(Locale.US, "%01.02f %s", shipping, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                mTax.setText(String.format(Locale.US, "%01.02f %s", tax, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                mBase.setText(String.format(Locale.US, "%01.02f %s", base, PaymentUtils.Currency.selectedCurrency.getSymbol()));

                for (Payment.OrderLine x : success.getOrderLineList()) {
                    mItems.add(PaymentItem.fromOrderLine(x, success.getCurrencyCode()));
                }
                mAdapter.setItems(mItems);
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(Shared.OTCStatus status) {
                mProgressDialog.dismiss();
                showCustomDialogError(CloudErrorHandler.handleError(status));
            }
        });
    }

    private void goToMenu() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        goToMenu();
    }

    private void setEvents() {
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mTitleBar.setLeftButtonImage(0);

        mGoToMenu.setOnClickListener(v -> goToMenu());
    }

    private void getViews() {
        mSummaryText = findViewById(R.id.orderText);
        mSubtotal = findViewById(R.id.paySubtotal);
        mBase = findViewById(R.id.payBase);
        mTax = findViewById(R.id.payTax);
        mShipping = findViewById(R.id.payShip);

        mRecyclerView = findViewById(R.id.recyclerItems);
        mGoToMenu = findViewById(R.id.goToMenu);
        mTitleBar = findViewById(R.id.titleBar);

        mAdapter = new ReviewAdapter(this);

        mQrCode = findViewById(R.id.qrCode);
    }
}
