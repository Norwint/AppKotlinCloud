package com.otcengineering.white_app.network;

import androidx.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.otc.alice.api.model.Payment;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.interfaces.OnServerResponse;
import com.otcengineering.white_app.tasks.GenericTask;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.tasks.TypedTask;
import com.otcengineering.white_app.utils.interfaces.TypedCallback;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nonnull;

public class PaymentNetwork {
    public static void addItemToShoppingCart(long itemId, int quantity, long shippingId, OnServerResponse response) {
        Payment.ItemQuantity iq = Payment.ItemQuantity.newBuilder().setId(itemId).setQuantity(quantity).setCostsId(shippingId).build();
        GenericTask task = new GenericTask(Endpoints.Payment.ADD_ITEM_CART, iq, true, response1 -> {
            if (response1.getStatus() == Shared.OTCStatus.SUCCESS) {
                response.onResponse(true, null);
            } else {
                response.onResponse(false, response1);
            }
        });
        task.execute();
    }

    public static void modifyItemToShoppingCart(PaymentItem item, OnServerResponse response) {
        Payment.ItemQuantity iq = Payment.ItemQuantity.newBuilder().setId(item.getId()).setQuantity(item.getAmount()).build();
        GenericTask task = new GenericTask(Endpoints.Payment.MODIFY_ITEM_CART, iq, true, response1 -> {
            if (response1.getStatus() == Shared.OTCStatus.SUCCESS) {
                response.onResponse(true, null);
            } else {
                response.onResponse(false, response1);
            }
        });
        task.execute();
    }

    public static void getItemCart(int page, Callback<Payment.CartItemsResponse> response) {
        Payment.Items request = Payment.Items.newBuilder().setPage(page).build();
        TypedTask<Payment.CartItemsResponse> tt = new TypedTask<>(Endpoints.Payment.GET_ITEM_CART, request, true, Payment.CartItemsResponse.class, new TypedCallback<Payment.CartItemsResponse>() {
            @Override
            public void onSuccess(@Nonnull Payment.CartItemsResponse value) {
                response.onSuccess(value);
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                response.onError(status);
            }
        });
        tt.execute();
    }

    public static void getOrder(long id, Callback<Payment.OrderData> onResponse) {
        TypedTask<Payment.OrderData> task = new TypedTask<>(String.format(Locale.US, Endpoints.Payment.ORDER_BY_ID, id), null, true, Payment.OrderData.class, new TypedCallback<Payment.OrderData>() {
            @Override
            public void onSuccess(@Nonnull Payment.OrderData value) {
                onResponse.onSuccess(value);
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                onResponse.onError(status);
            }
        });
        task.execute();
    }

    public static void payWithGooglePay(final String token, HashMap<String, String> params, Callback<Payment.OrderId> onResponse) {
        Payment.GooglePayCheckout request = Payment.GooglePayCheckout.newBuilder()
                .setPaymentMethodNonce(token)
                .setAddress(params.get("address"))
                .setCity(params.get("city"))
                .setCp(params.get("postal_code"))
                .setRegion(params.get("region"))
                .setCountry(params.get("country"))
                .setName(params.get("name"))
                .build();
        TypedTask<Payment.OrderId> task = new TypedTask<>(Endpoints.Payment.GOOGLE_PAY_CHECKOUT, request, true, Payment.OrderId.class, new TypedCallback<Payment.OrderId>() {
            @Override
            public void onSuccess(@Nonnull Payment.OrderId value) {
                onResponse.onSuccess(value);
            }

            @Override
            public void onError(@Nonnull Shared.OTCStatus status, @Nullable String message) {
                onResponse.onError(status);
            }
        });
        task.execute();
    }

    public static void addPromo(String input, Callback<Boolean> onResponse) {
        // type=x&code=y
        HashMap<String, String> parameters = (HashMap<String, String>) Stream.of(Arrays.asList(input.split("&")))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(e -> e[0], e -> e[1]));

        Payment.Promotion promo = Payment.Promotion.newBuilder()
                .setId(Long.parseLong(parameters.get("code")))
                .setQuantity(1)
                .build();
        GenericTask gt = new GenericTask(Endpoints.Payment.PROMO, promo, true, response -> {
            if (response.getStatus() == Shared.OTCStatus.SUCCESS) {
                onResponse.onSuccess(true);
            } else {
                onResponse.onError(response.getStatus());
            }
        });
        gt.execute();
    }
}
