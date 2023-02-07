package com.otcengineering.white_app.payment.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.function.ToDoubleFunction;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.interfaces.ExtendedCallback;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.payment.adapter.ReviewAdapter;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.network.PaymentNetwork;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nonnull;

public class ReviewPaymentActivity extends AppCompatActivity {
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 6969;
    private PaymentUtils.Method mPaymentMethod;
    private TextView mPayName, mSubtotal, mBase, mTax, mShipment;
    private ImageView mPayImage, mPayAlert;
    private RecyclerView mRecycler;
    private TitleBar mTitleBar;
    private Button mPay;
    private ArrayList<PaymentItem> mItems;
    private boolean mLastPage = false;
    private ReviewAdapter mAdapter;
    private HashMap<String, String> shipmentOptions;
    private PaymentUtils mPaymentUtils;
    private double mTotalPrice, mTaxPrice, mBasePrice, mShipmentPrice;
    private JSONObject mCreditCardData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_payment);

        mItems = new ArrayList<>();

        mPaymentUtils = new PaymentUtils(this);
        mPaymentUtils.getClientToken(new ExtendedCallback<Boolean>() {
            @Override
            public void onSuccess(@Nonnull Boolean success) {
                mPay.setEnabled(success);
            }

            @Override
            public void onError(@Nullable Shared.OTCStatus status, @Nullable String message) {
                mPay.setEnabled(false);
            }
        });

        getFullShoppingCart();
        getExtras();
        getViews();
        setEvents();
        fillViews();

        mPay.setEnabled(false);
    }

    private ProgressDialog mProgressDialog;

    private void getFullShoppingCart() {
        mItems.clear();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.show();

        getShoppingCart(1);
    }

    private void getShoppingCart(int page) {
        PaymentNetwork.getItemCart(page, new Callback<Payment.CartItemsResponse>() {
            @Override
            public void onSuccess(Payment.CartItemsResponse success) {
                mLastPage = success.getPage() == success.getPages();
                for (Payment.Item it : success.getItemsList()) {
                    mItems.add(new PaymentItem(it));
                }
                if (!mLastPage) {
                    mTotalPrice += success.getTotalAmount();
                    mTaxPrice += success.getTaxAmount();
                    mBasePrice += success.getBaseAmount();
                    mShipmentPrice += success.getShipAmount();
                    getShoppingCart(page + 1);
                } else {
                    mAdapter.setItems(mItems);
                    mProgressDialog.dismiss();

                    mTotalPrice += success.getTotalAmount();
                    mTaxPrice += success.getTaxAmount();
                    mBasePrice += success.getBaseAmount();
                    mShipmentPrice += success.getShipAmount();

                    mSubtotal.setText(String.format(Locale.US, "%01.02f %s", mTotalPrice, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                    mBase.setText(String.format(Locale.US, "%01.02f %s", mBasePrice, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                    mTax.setText(String.format(Locale.US, "%01.02f %s", mTaxPrice, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                    mShipment.setText(String.format(Locale.US, "%01.02f %s", mShipmentPrice, PaymentUtils.Currency.selectedCurrency.getSymbol()));
                }
            }

            @Override
            public void onError(Shared.OTCStatus status) {
                // Mec!
                mProgressDialog.dismiss();
            }
        });
    }

    private double sum(ToDoubleFunction<PaymentItem> fn) {
        int sum = 0;
        for (PaymentItem pi : mItems) {
            int val = (int) Math.ceil(fn.applyAsDouble(pi) * 100);
            sum += val;
        }
        return sum / 100.0;
    }

    private void fillViews() {
        switch (mPaymentMethod) {
            case GooglePay: mPayName.setText("Google Pay"); Glide.with(this).load(R.drawable.googlepay_mark_800_gray).into(mPayImage); checkGPay(); break;
            case Paypal: mPayName.setText("PayPal"); Glide.with(this).load(R.drawable.paypal_logo).into(mPayImage);  break;
            case CreditCard: mPayName.setText("Credit Card"); Glide.with(this).load(R.drawable.credit_card).into(mPayImage);  break;
        }
    }

    private void checkGPay() {
        mPay.setEnabled(false);
        ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Checking Google Pay... Please, wait.");
        pd.show();

        JSONObject isReadyToPayJson = PaymentUtils.getIsReadyToPayRequest();
        if (isReadyToPayJson == null) {
            setError(true);
            pd.dismiss();
            return;
        }

        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());
        if (request == null) {
            setError(true);
            pd.dismiss();
            return;
        }

        Task<Boolean> task = PaymentUtils.createPaymentsClient(this).isReadyToPay(request);
        task.addOnCompleteListener(this, task1 -> {
            runOnUiThread(pd::dismiss);
            if (task1.isSuccessful()) {
                runOnUiThread(() -> {
                    setError(!task1.getResult());
                    mPay.setEnabled(task1.getResult());
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "isReadyToPay failed: " + task1.getException().toString(), Toast.LENGTH_LONG).show();
                    setError(true);
                });
            }
        });
    }

    private void setError(boolean error) {
        mPayAlert.setVisibility(error ? View.VISIBLE : View.GONE);
        mPayAlert.setOnClickListener(v -> {
            // Toast.makeText(this, R.string.googlepay_status_unavailable);
        });
    }

    private void getViews() {
        mPayName = findViewById(R.id.paymentMethodName);
        mPayImage = findViewById(R.id.paymentMethodImage);
        mSubtotal = findViewById(R.id.paySubtotal);
        mShipment = findViewById(R.id.payShip);
        mBase = findViewById(R.id.payBase);
        mTax = findViewById(R.id.payTax);
        mRecycler = findViewById(R.id.recyclerItems);
        mTitleBar = findViewById(R.id.titlebar);
        mPay = findViewById(R.id.payButton);
        mPayAlert = findViewById(R.id.paymentMethodAlert);
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

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ReviewAdapter(this);
        mRecycler.setAdapter(mAdapter);

        mPay.setOnClickListener(v -> pay());
    }

    private void pay() {
        double subtotal = mTotalPrice;

        String amount;
        if (PaymentUtils.Currency.selectedCurrency == PaymentUtils.Currency.Yen) {
            amount = String.format(Locale.US, "%01d", (int) subtotal);
        } else {
            amount = String.format(Locale.US, "%01.02f", subtotal);
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        if (mPaymentMethod == PaymentUtils.Method.Paypal) {
            mProgressDialog.setMessage("Loading PayPal...");
            mProgressDialog.show();

            mPaymentUtils.payPayPal(amount, PaymentUtils.Currency.selectedCurrency.getCodeFromCurrent(), shipmentOptions, new ExtendedCallback<Payment.OrderId>() {
                @Override
                public void onSuccess(@Nonnull Payment.OrderId success) {
                    mProgressDialog.dismiss();
                    goToSummary(success.getId(), success.getCodeQR());
                }

                @Override
                public void onError(Shared.OTCStatus status, String message) {
                    mProgressDialog.dismiss();
                    showError("PayPal request error.", "Cannot complete the Payment: " + (status != null ? CloudErrorHandler.handleError(status) : message));
                }
            });
        } else if (mPaymentMethod == PaymentUtils.Method.GooglePay) {
            mProgressDialog.setMessage("Loading Google Pay...");
            mProgressDialog.show();

            JSONObject request = PaymentUtils.getPaymentDataRequest(amount, PaymentUtils.Currency.selectedCountry, PaymentUtils.Currency.selectedCurrency.getCodeFromCurrent());
            if (request != null) {
                PaymentDataRequest pdr = PaymentDataRequest.fromJson(request.toString());
                if (pdr != null) {
                    AutoResolveHelper.resolveTask(PaymentUtils.createPaymentsClient(this).loadPaymentData(pdr), this, LOAD_PAYMENT_DATA_REQUEST_CODE);
                } else {
                    mProgressDialog.dismiss();
                    showError("Google Pay request error.", "Cannot create Payment Request. Please, try again.");
                }
            } else {
                mProgressDialog.dismiss();
                showError("Google Pay request error.", "Cannot create Payment Request. Please, try again.");
            }
        } else if (mPaymentMethod == PaymentUtils.Method.CreditCard) {
            mProgressDialog.setMessage("Loading Credit Card...");
            mProgressDialog.show();

            try {
                mPaymentUtils.payCreditCard(amount, mCreditCardData.getString("card_number"), mCreditCardData.getString("card_cvv"),
                        mCreditCardData.getString("card_expiration"), shipmentOptions, new ExtendedCallback<Payment.OrderId>() {
                            @Override
                            public void onSuccess(@Nonnull Payment.OrderId orderId) {
                                mProgressDialog.dismiss();
                                goToSummary(orderId.getId(), orderId.getCodeQR());
                            }

                            @Override
                            public void onError(Shared.OTCStatus status, String message) {
                                mProgressDialog.dismiss();
                                showError("Credit Card Payment request error.", "Cannot complete the Payment: " + (status != null ? CloudErrorHandler.handleError(status) : message));
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void goToSummary(Long orderId, ByteString qtCode) {
        Intent intent = new Intent(this, ShoppingSummaryActivity.class);
        intent.putExtra("OrderId", orderId);
        if (qtCode != null) {
            intent.putExtra("OrderQR", qtCode.toStringUtf8());
        }
        startActivityForResult(intent, Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    PaymentData pd = PaymentData.getFromIntent(data);

                    try {
                        JSONObject paymentData = new JSONObject(pd.toJson());
                        String token = paymentData.getJSONObject("paymentMethodData").getJSONObject("tokenizationData").getString("token");

                        PaymentNetwork.payWithGooglePay(token, shipmentOptions, new Callback<Payment.OrderId>() {
                            @Override
                            public void onSuccess(Payment.OrderId success) {
                                mProgressDialog.dismiss();
                                goToSummary(success.getId(), success.getCodeQR());
                            }

                            @Override
                            public void onError(Shared.OTCStatus status) {
                                mProgressDialog.dismiss();
                                showError(getString(R.string.server_error), "Cannot complete the Payment: " +  CloudErrorHandler.handleError(status));
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mProgressDialog.dismiss();
                        showError("Google Pay transaction failed.", "Error processing response from Google Pay.");
                    }
                }
                break;
                case Activity.RESULT_CANCELED: {
                    mProgressDialog.dismiss();
                }
                break;
                case AutoResolveHelper.RESULT_ERROR: {
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    status.getStatusCode();
                    mProgressDialog.dismiss();
                }
            }
        } else if (requestCode == Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY) {
            if (resultCode == RESULT_OK) {
                goToBack();
            }
        }
    }

    private void goToBack() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(RESULT_CANCELED);
    }

    private void showError(String title, String message) {
        CustomDialog cd = new CustomDialog(this, message, true);
        cd.setTitle(title);
        cd.setMessage(message);
        cd.show();
    }

    private void getExtras() {
        int method = getIntent().getIntExtra("Method", 0);
        mPaymentMethod = PaymentUtils.Method.getValue(method);
        mPaymentUtils.paymentMethod = mPaymentMethod;
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        shipmentOptions = Utils.getGson().fromJson(getIntent().getStringExtra("shipping_options"), type);
        if (mPaymentMethod == PaymentUtils.Method.CreditCard) {
            String jsonData = getIntent().getStringExtra("credit_card_data");
            if (jsonData != null) {
                try {
                    mCreditCardData = new JSONObject(jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
