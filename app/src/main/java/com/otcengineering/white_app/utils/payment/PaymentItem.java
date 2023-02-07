package com.otcengineering.white_app.utils.payment;

import com.otc.alice.api.model.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentItem {
    private int mAmount;
    private Payment.Item mItem;

    public PaymentItem(Payment.Item item) {
        this.mItem = item;
        this.mAmount = item.getQuantity();
    }

    public static PaymentItem fromOrderLine(Payment.OrderLine toCopy, String currencyCode) {
        Payment.Price price = Payment.Price.newBuilder()
                .setBaseAmount(toCopy.getBasePrice())
                .setTaxAmount(toCopy.getTaxAmount())
                .setTotalAmount(toCopy.getTotalPrice())
                .setTaxPercent(toCopy.getTaxPercent())
                .setCurrencyCode(currencyCode)
                .build();

        Payment.Item it = Payment.Item.newBuilder()
                .setDescription(toCopy.getDescription())
                .setId(toCopy.getItemId())
                .setImageId(0)
                .setName(toCopy.getName())
                .setQuantity(toCopy.getQuantity())
                .addPrices(price)
                .build();
        return new PaymentItem(it);
    }

    public String getName() {
        return mItem.getName();
    }

    public String getDescription() {
        return mItem.getDescription();
    }

    public Long getImage() {
        return mItem.getImageId();
    }

    public int getAmount() {
        return mAmount;
    }

    public void addAmount(int amount) {
        mAmount += amount;
    }

    public void decreaseAmount() {
        --mAmount;
    }

    public void increaseAmount() {
        ++mAmount;
    }

    public void setAmount(int amount) {
        this.mAmount = amount;
    }

    private double decimalize(double qty) {
        return (int)(Math.ceil(qty * 100)) / 100.0;
    }

    public List<Payment.ItemShippingCosts> getShipment() {
        return mItem.getShipmentCostsList();
    }

    public double getPrice() {
        try {
            return decimalize(mItem.getPricesList().get(0).getTotalAmount());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getBasePrice() {
        try {
            return decimalize(mItem.getPricesList().get(0).getBaseAmount());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getTaxAmount() {
        try {
            return decimalize(mItem.getPricesList().get(0).getTaxAmount());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static PaymentItem copy(final PaymentItem toCopy) {
        return new PaymentItem(toCopy.mItem);
    }

    public long getId() {
        return mItem.getId();
    }

    public boolean getPromo() {
        return mItem.getIsPromotion();
    }

    public boolean isEnabled() {
        return mItem.getStatus().equals("ENABLED");
    }

    public double getTotalPrice() {
        return mItem.getPrices(0).getTotalAmount() * mItem.getQuantity() + mItem.getShipmentCosts(0).getCosts();
    }
}