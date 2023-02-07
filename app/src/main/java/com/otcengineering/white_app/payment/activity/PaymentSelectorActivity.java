package com.otcengineering.white_app.payment.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.BaseActivity;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.utils.payment.PaymentUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentSelectorActivity extends BaseActivity {
    private Button mContinue;
    private ImageButton mScanCard;
    private RadioGroup mRadioGroup;
    private TitleBar mTitleBar;
    private String mShipmentOptions;
    private EditText mCardNumber, mCvv, mExpirationMonth, mExpirationYear;
    private ConstraintLayout mCardLayout;

    static final int REQUEST_CODE_SCAN_CARD = 69;

    public PaymentSelectorActivity() {
        super("PaymentSelectorActivity");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_selector);

        getExtras();
        findViews();
        setEvents();
    }

    private void getExtras() {
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra("shipping_options") != null) {
            mShipmentOptions = intent.getStringExtra("shipping_options");
        } else {
            mShipmentOptions = "{}";
        }
    }

    private void setEvents() {
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.credit) {
                mCardLayout.setVisibility(View.VISIBLE);
            } else {
                mCardLayout.setVisibility(View.GONE);
            }
        });
        mCardLayout.setVisibility(View.GONE);

        mScanCard.setOnClickListener(v -> {
            mExpirationYear.setText("21");
            mExpirationMonth.setText("12");
            mCvv.setText("123");
            mCardNumber.setText("4111 1111 1111 1111");
        });

        mCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (' ' == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(' ')).length <= 3) {
                        s.insert(s.length() - 1, String.valueOf(' '));
                    }
                }
            }
        });

        mContinue.setOnClickListener(v -> {
            PaymentUtils.Method paymentMethod;

            switch (mRadioGroup.getCheckedRadioButtonId()) {
                case -1: Toast.makeText(this, "Please, select one method of payment.", Toast.LENGTH_LONG).show(); return;
                case R.id.google_pay: paymentMethod = PaymentUtils.Method.GooglePay; break;
                case R.id.paypal: paymentMethod = PaymentUtils.Method.Paypal; break;
                case R.id.credit: paymentMethod = PaymentUtils.Method.CreditCard; break;
                default: return;
            }

            if (paymentMethod == PaymentUtils.Method.CreditCard && checkCardFields()) {
                Toast.makeText(this, "Please, fill the card information.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(this, ReviewPaymentActivity.class);
            intent.putExtra("Method", paymentMethod.ordinal());
            intent.putExtra("shipping_options", mShipmentOptions);

            if (paymentMethod == PaymentUtils.Method.CreditCard) {
                String cred = mCardNumber.getText().toString().replace(" ", "");
                JSONObject obj = new JSONObject();
                try {
                    obj.put("card_number", cred);
                    obj.put("card_cvv", mCvv.getText().toString());
                    obj.put("card_expiration", mExpirationMonth.getText().toString() + "/" + mExpirationYear.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Please, fill the card information.", Toast.LENGTH_LONG).show();
                    return;
                }
                intent.putExtra("credit_card_data", obj.toString());
            }

            startActivityForResult(intent, Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY);
        });

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
    }

    private boolean checkCardFields() {
        return mCardNumber.getText().toString().isEmpty() ||
                mCvv.getText().toString().isEmpty() ||
                mExpirationMonth.getText().toString().isEmpty() ||
                mExpirationYear.getText().toString().isEmpty() ||
                mCardNumber.getText().toString().replace(" ", "").length() < 16;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.ACTIVITY_RESULT_FROM_PAYMENT_SUMMARY && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_CODE_SCAN_CARD && resultCode == RESULT_OK) {

        }
    }

    private void findViews() {
        mContinue = findViewById(R.id.continueButton);
        mRadioGroup = findViewById(R.id.radio_group);
        mTitleBar = findViewById(R.id.titlebar);

        mCardNumber = findViewById(R.id.cardNumber);
        mCvv = findViewById(R.id.cvv);
        mExpirationMonth = findViewById(R.id.expirationMonth);
        mExpirationYear = findViewById(R.id.expirationYear);

        mCardLayout = findViewById(R.id.layoutCreditCard);
        mScanCard = findViewById(R.id.scanCard);
    }

}
