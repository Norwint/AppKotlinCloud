package com.otcengineering.white_app.payment.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.activities.QRActivity;
import com.otcengineering.white_app.payment.adapter.PaymentAdapter;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.network.PaymentNetwork;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends BaseActivity {

    private PaymentAdapter mAdapter;
    private Button mInvoice;
    private TextView mPrice;
    private ArrayList<PaymentItem> mItems;
    private TitleBar mTitleBar;
    private SurfaceView mSurfaceView;

    public CartActivity() {
        super("CartActivity");
        mItems = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);

        getViews();
        setEvents();
        setUI();

        getCart();
    }

    private void setUI() {
        mAdapter.setItems(mItems);
    }

    public void getCart() {
        mItems.clear();
        getCart(1);
    }

    private void getCart(int page) {
        PaymentNetwork.getItemCart(page, new Callback<>() {
            @Override
            public void onSuccess(Payment.CartItemsResponse success) {
                for (Payment.Item item : success.getItemsList()) {
                    mItems.add(new PaymentItem(item));
                }
                if (success.getPages() > page) {
                    getCart(page + 1);
                } else {
                    mAdapter.setItems(mItems);
                    mAdapter.notifyDataSetChanged();

                    mTitleBar.setTitle(String.format(Locale.US, "Shopping Cart (%d)", mItems.size()));
                    String shipment = "";
                    if (success.getShipAmount() > 0) {
                        shipment = String.format(Locale.US, "(Shipment cost: %01.02f %s)", success.getShipAmount(), PaymentUtils.Currency.selectedCurrency.getSymbol());
                    }
                    mPrice.setText(String.format(Locale.US, "Total Price: %01.02f %s %s", success.getTotalAmount(), PaymentUtils.Currency.selectedCurrency.getSymbol(), shipment));
                }
                calculatePrice();
            }

            @Override
            public void onError(Shared.OTCStatus status) {
                if (page == 1 && status == Shared.OTCStatus.INVALID_PAGE_NUMBER) {
                    mAdapter.addItems(new ArrayList<>());
                    mAdapter.notifyDataSetChanged();

                    mTitleBar.setTitle(String.format(Locale.US, "Shopping Cart (%d)", 0));
                    calculatePrice();
                    mInvoice.setEnabled(false);
                }
            }
        });
    }

    private void setEvents() {
        mInvoice.setOnClickListener(v -> checkCheckout());

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
                Intent intent = new Intent(CartActivity.this, QRActivity.class);
                intent.putExtra("QR_PATTERN", "promo:(code=\\d+|type=\\w+)&(code=\\d+|type=\\w+)");
                startActivityForResult(intent, QRActivity.QR_RESULT);
            }
        });
    }

    private void checkCheckout() {
        GenericTask task = new GenericTask(Endpoints.Payment.CHECK_CHECKOUT, null, true, response -> {
            if (response.getStatus() == Shared.OTCStatus.SUCCESS) {
                startActivityForResult(new Intent(this, ShippingSelectionActivity.class), Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY);
            } else {
                showCustomDialogError(CloudErrorHandler.handleError(response.getStatus()));
            }
        });
        task.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY && resultCode == RESULT_OK) {
            goToBack();
        } else if (requestCode == QRActivity.QR_RESULT && resultCode == RESULT_OK) {
            String code = data.getStringExtra("QR_RESULT");
            Utils.runOnMainThread(() -> {
                DialogYesNo dyn = new DialogYesNo(this);
                dyn.setMessage("Do you want to add the promotion to the cart?");
                dyn.setYesButtonClickListener(() -> PaymentNetwork.addPromo(code.replace("promo:", ""), new Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        getCart();
                    }

                    @Override
                    public void onError(Shared.OTCStatus status) {
                        CustomDialog cd = new CustomDialog(CartActivity.this);
                        cd.setMessage(CloudErrorHandler.handleError(status));
                        cd.show();
                    }
                }));
                dyn.setNoButtonClickListener(this::finish);
                dyn.show();
            });
        }
    }

    private void goToBack() {
        setResult(RESULT_OK);
        finish();
    }


    public ArrayList<PaymentItem> getItems() {
        return mItems;
    }

    private void getViews() {
        mInvoice = findViewById(R.id.invoice);
        mInvoice.setEnabled(false);
        mAdapter = new PaymentAdapter(this, this::calculatePrice);
        mPrice = findViewById(R.id.totalPrice);
        RecyclerView items = findViewById(R.id.items);

        items.setAdapter(mAdapter);
        items.setLayoutManager(new LinearLayoutManager(this));

        mTitleBar = findViewById(R.id.titlebar);
        mSurfaceView = findViewById(R.id.surfaceView);
    }

    private void calculatePrice() {
        double price = mAdapter.getCheckedPrice();
        String shipment = "";
        if (mAdapter.getShipAmount() > 0) {
            shipment = String.format(Locale.US, "(Shipment cost: %01.02f %s)", mAdapter.getShipAmount(), PaymentUtils.Currency.selectedCurrency.getSymbol());
        }
        mPrice.setText(String.format(Locale.US, "Total Price: %01.02f %s %s", price, PaymentUtils.Currency.selectedCurrency.getSymbol(), shipment));
        mInvoice.setEnabled(!mAdapter.hasRottenItem());
    }
}
