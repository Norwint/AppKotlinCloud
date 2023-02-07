package com.otcengineering.white_app.utils.payment;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.BraintreeException;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PayPalRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.interfaces.ExtendedCallback;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nonnull;

public class PaymentUtils {
    public Method paymentMethod;
    private BraintreeFragment mBraintreeFragment;
    private String mClientToken;
    private String mNonce;
    private Activity mContext;
    private PaymentsClient mPaymentClient;
    private String mDeviceData;

    public BraintreeFragment getBraintreeFragment() {
        return mBraintreeFragment;
    }

    public enum Method {
        GooglePay, Paypal, CreditCard, Count, Invalid;

        public static Method getValue(int ordinal) {
            switch (ordinal) {
                case 0: return GooglePay;
                case 1: return Paypal;
                case 2: return CreditCard;
                case 3: return Count;
                default: return Invalid;
            }
        }
    }

    public PaymentUtils(final Activity ctx) {
        mContext = ctx;
        mPaymentClient = createPaymentsClient(ctx);
    }

    public enum Currency {
        Euro, Dollar, Yen, Rubles, BritishPound, Count, Invalid;

        public static Currency selectedCurrency;
        public static String selectedCountry;

        @NonNull
        public String getCodeFromCurrent() {
            switch (this) {
                case Euro: return "EUR";
                case Dollar: return "USD";
                case Yen: return "JPY";
                case Rubles: return "RUB";
                case BritishPound: return "GBP";
                default: return "";
            }
        }

        public static Currency[] getValues() {
            Currency[] currencies = new Currency[Count.ordinal()];
            for (int i = 0; i < currencies.length; ++i) {
                currencies[i] = getCurrency(i);
            }
            return currencies;
        }

        public static Currency getByCode(String code) {
            switch (code) {
                case "EUR": return Euro;
                case "USD": return Dollar;
                case "JPY": return Yen;
                case "RUB": return Rubles;
                case "GBP": return BritishPound;
                default: return Invalid;
            }
        }

        public String getName() {
            switch (this) {
                case Euro: return "Euro";
                case Dollar: return "US Dollar";
                case Yen: return "Japanese Yen";
                case Rubles: return "Russian Ruble";
                case BritishPound: return "British Pound";
                default: return null;
            }
        }

        @Nullable
        public static Currency getCurrency(int pos) {
            switch (pos) {
                case 0: return Euro;
                case 1: return Dollar;
                case 2: return Yen;
                case 3: return Rubles;
                case 4: return BritishPound;
                default: return Invalid;
            }
        }

        public double getCurrencyRatio() {
            switch (this) {
                case Euro: return 1.0;
                case Dollar: return 1.11;
                case Yen: return 120.63;
                case Rubles: return 71.05;
                case BritishPound: return 0.85;
                default: return 0.0;
            }
        }

        public String getSymbol() {
            switch (this) {
                case Euro: return "€";
                case Dollar: return "$";
                case Yen: return "¥";
                case Rubles: return "\u20BD";
                case BritishPound: return "£";
                default: return null;
            }
        }

        public String getCountryFromCurrent() {
            switch (this) {
                case Euro: return "ES";
                case Dollar: return "US";
                case Yen: return "JP";
                case Rubles: return "RU";
                case BritishPound: return "UK";
                default: return null;
            }
        }
    }

    public String getAmount(double basePrice, Currency currency) {
        double finalPrice = (basePrice / 100.0) * currency.getCurrencyRatio();
        return String.format(Locale.getDefault(), "%01.02f", finalPrice);
    }

    public void getClientToken(ExtendedCallback<Boolean> onTokenRetrieved) {
        TypedTask<Payment.PaypalToken> getClientToken = new TypedTask<>(Endpoints.Payment.PAYPAL_CLIENT_TOKEN, null, true, Payment.PaypalToken.class,
                new TypedCallback<Payment.PaypalToken>() {

                    @Override
                    public void onSuccess(@Nonnull Payment.PaypalToken value) {
                        mClientToken = value.getToken();
                        try {
                            mBraintreeFragment = BraintreeFragment.newInstance(mContext, mClientToken);
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                        try {
                            onTokenRetrieved.onSuccess(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                        try {
                            onTokenRetrieved.onError(status, message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        getClientToken.execute();
    }

    public void payPayPal(String amount, String currency, HashMap<String, String> params, ExtendedCallback<Payment.OrderId> onOperationEnded) {
        mBraintreeFragment.addListener((PaymentMethodNonceCreatedListener) paymentMethodNonce -> {
            mNonce = paymentMethodNonce.getNonce();
            Payment.PaypalCheckout pc = Payment.PaypalCheckout.newBuilder()
                    .setPaymentMethodNonce(mNonce)
                    .setClientIdToken(mClientToken)
                    .setAddress(params.get("address"))
                    .setCity(params.get("city"))
                    .setCp(params.get("postal_code"))
                    .setRegion(params.get("region"))
                    .setCountry(params.get("country"))
                    .setName(params.get("name"))
                    .build();
            TypedTask<Payment.OrderId> sendPay = new TypedTask<>(Endpoints.Payment.PAYPAL_CHECKOUT, pc, true, Payment.OrderId.class, new TypedCallback<Payment.OrderId>() {
                @Override
                public void onSuccess(@Nonnull Payment.OrderId value) {
                    try {
                        onOperationEnded.onSuccess(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                    try {
                        onOperationEnded.onError(status, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            sendPay.execute();
        });

        mBraintreeFragment.addListener((BraintreeCancelListener) requestCode -> {
            try {
                onOperationEnded.onError(null, "Operation Cancelled");
                CustomDialog cd = new CustomDialog(mContext, "Payment canceled.", true);
                cd.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mBraintreeFragment.addListener((BraintreeErrorListener) error -> {
            try {
                onOperationEnded.onError(null, error.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (error instanceof ErrorWithResponse) {
                ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
                BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
                if (cardErrors != null) {
                    // There is an issue with the credit card.
                    BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                    if (expirationMonthError != null) {
                        // There is an issue with the expiration month.
                        showError(expirationMonthError.getMessage());
                    }
                }
            } else if (error instanceof BraintreeException) {
                BraintreeException ex = (BraintreeException) error;
                showError(ex.getMessage());
            }
        });

        PayPalRequest request = new PayPalRequest(amount)
                .currencyCode(currency)
                .intent(PayPalRequest.INTENT_AUTHORIZE);
        PayPal.requestOneTimePayment(mBraintreeFragment, request);
    }

    public void payCreditCard(String amount, String creditCard, String cvv, String expirationDate, HashMap<String, String> params, ExtendedCallback<Payment.OrderId> onOperationEnded) {
        mBraintreeFragment.addListener((PaymentMethodNonceCreatedListener) paymentMethodNonce -> {
            mNonce = paymentMethodNonce.getNonce();

            Payment.CreditCardCheckout ccc = Payment.CreditCardCheckout.newBuilder()
                    .setPaymentMethodNonce("fake-valid-nonce")
                    .setAddress(params.get("address"))
                    .setCity(params.get("city"))
                    .setCp(params.get("postal_code"))
                    .setRegion(params.get("region"))
                    .setCountry(params.get("country"))
                    .setName(params.get("name"))
                    .setDeviceData(mDeviceData)
                    .build();

            TypedTask<Payment.OrderId> payWithCreditCard = new TypedTask<>(Endpoints.Payment.CREDIT_CARD_CHECKOUT, ccc, true, Payment.OrderId.class, new TypedCallback<Payment.OrderId>() {
                @Override
                public void onSuccess(@Nonnull Payment.OrderId value) {
                    try {
                        onOperationEnded.onSuccess(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                    try {
                        onOperationEnded.onError(status, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            payWithCreditCard.execute();
        });

        mBraintreeFragment.addListener((BraintreeCancelListener) requestCode -> {
            try {
                onOperationEnded.onError(null, "Operation cancelled.");
                CustomDialog cd = new CustomDialog(mContext, "Payment canceled.", true);
                cd.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mBraintreeFragment.addListener((BraintreeErrorListener) error -> {
            try {
                onOperationEnded.onError(null, error.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (error instanceof ErrorWithResponse) {
                ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
                BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
                if (cardErrors != null) {
                    // There is an issue with the credit card.
                    BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                    if (expirationMonthError != null) {
                        // There is an issue with the expiration month.
                        showError(expirationMonthError.getMessage());
                    }
                }
            } else if (error instanceof BraintreeException) {
                BraintreeException ex = (BraintreeException) error;
                showError(ex.getMessage());
            }
        });

        DataCollector.collectDeviceData(mBraintreeFragment, s -> {
            mDeviceData = s;

            CardBuilder cardBuilder = new CardBuilder()
                    .cardNumber(creditCard)
                    .cvv(cvv)
                    .expirationDate(expirationDate);

            Card.tokenize(mBraintreeFragment, cardBuilder);
        });
    }

    public static JSONObject getPaymentDataRequest(String price, String country, String currency) {
        try {
            JSONObject paymentDataRequest = getBaseRequest();
            paymentDataRequest.put(
                    "allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
            paymentDataRequest.put("transactionInfo", getTransactionInfo(price, country, currency));
            paymentDataRequest.put("merchantInfo", getMerchantInfo());

            paymentDataRequest.put("shippingAddressRequired", true);

            JSONObject shippingAddressParameters = new JSONObject();
            shippingAddressParameters.put("phoneNumberRequired", false);

            JSONArray allowedCountryCodes = new JSONArray(Arrays.asList("ID", "IN", "ES"));

            shippingAddressParameters.put("allowedCountryCodes", allowedCountryCodes);
            paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters);
            return paymentDataRequest;
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject getTransactionInfo(String amt, String cnt, String cur) throws JSONException {
        return new JSONObject().put("totalPrice", amt).put("totalPriceStatus", "FINAL")
                .put("countryCode", cnt).put("currencyCode", cur);
    }

    private static JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", "Example Merchant");
    }

    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
    }

    private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
        return new JSONObject(){{      put("type", "PAYMENT_GATEWAY");
            put("parameters", new JSONObject(){{
                put("gateway", "example");
                put("gatewayMerchantId", "exampleGatewayMerchantId");
            }
            });
        }};
    }

    private static JSONArray getAllowedCardNetworks() {
        return new JSONArray()
                .put("AMEX")
                .put("DISCOVER")
                .put("INTERAC")
                .put("JCB")
                .put("MASTERCARD")
                .put("VISA");
    }

    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS");
    }

    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
        parameters.put("allowedCardNetworks", getAllowedCardNetworks());
        // Optionally, you can add billing address/phone number associated with a CARD payment method.
        parameters.put("billingAddressRequired", true);

        JSONObject billingAddressParameters = new JSONObject();
        billingAddressParameters.put("format", "FULL");

        parameters.put("billingAddressParameters", billingAddressParameters);

        cardPaymentMethod.put("parameters", parameters);

        return cardPaymentMethod;
    }

    private static JSONObject getCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", getGatewayTokenizationSpecification());

        return cardPaymentMethod;
    }

    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }

    public static JSONObject getIsReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put(
                    "allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));

            return isReadyToPayRequest;
        } catch (JSONException e) {
            return null;
        }
    }

    private void showError(String message) {
        mContext.runOnUiThread(() -> Toast.makeText(mContext, message, Toast.LENGTH_LONG).show());
    }

    public PaymentsClient getClient() {
        return mPaymentClient;
    }
}
