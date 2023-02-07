package com.otcengineering.white_app.activities;


import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.otc.alice.api.model.Payment;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.components.DialogMultiple;
import com.otcengineering.white_app.components.DialogPaymentMultiple;
import com.otcengineering.white_app.components.TitleBar;
import com.otcengineering.white_app.network.PaymentNetwork;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemDetailsActivity extends BaseActivity {
    private ImageView mImage;
    private TextView mTitle, mDescription, mPrice, mAmount;
    private Button mAdd, mRemove, mAddToCart;
    private PaymentItem mItem;
    private TitleBar mTitleBar;
    private int cnt = -2;

    public ItemDetailsActivity() {
        super("ItemDetailsActivity");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_details);

        getExtras();
        getViews();
        setEvents();
        setUI();
    }

    private void getExtras() {
        String json = getIntent().getStringExtra("Item");
        mItem = Utils.getGson().fromJson(json, PaymentItem.class);
    }

    private void setUI() {
        Glide.with(this).load(mItem.getImage()).placeholder(R.drawable.icon).into(mImage);
        mTitle.setText(mItem.getName());
        mDescription.setText(mItem.getDescription());
        mPrice.setText(String.format(Locale.US, "Base price: %02.02f %s", mItem.getPrice(), PaymentUtils.Currency.selectedCurrency.getSymbol()));

        setAmount(cnt = 1);
    }

    private void addItem(long itemId, int qty, long shipId) {
        PaymentNetwork.addItemToShoppingCart(itemId, qty, shipId, (success, data) -> {
            if (success) {
                CustomDialog cd = new CustomDialog(this, "The item was added to the cart.", false);
                cd.show();
            } else {
                CustomDialog cd = new CustomDialog(this, "Cannot add the item to the cart.", true);
                cd.show();
            }
        });
    }

    private void setEvents() {
        mAdd.setOnClickListener(v -> {
            if (cnt < 100) setAmount(++cnt);
        });

        mRemove.setOnClickListener(v -> {
            if (cnt > 1) setAmount(--cnt);
        });

        mAddToCart.setOnClickListener(v -> {
            PaymentItem cp = PaymentItem.copy(mItem);
            cp.setAmount(cnt);

            DialogPaymentMultiple dm = new DialogPaymentMultiple(this);
            dm.setTitle("Shipping Method");
            dm.setDescription("How do you want to have this item delivered?");

            for (Payment.ItemShippingCosts sc : cp.getShipment()) {
                String costs = String.format(Locale.US, " (+%01.02f%s)", sc.getCosts(), PaymentUtils.Currency.selectedCurrency.getSymbol());
                if (sc.getCosts() == 0) {
                    costs = "";
                }
                dm.addButton(sc.getDesc() + costs, () -> {
                    addItem(cp.getId(), cnt, sc.getId());
                });
            }
            dm.addButton("Cancel", () -> {});

            dm.show();
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

    private void setAmount(int cnt) {
        mAmount.setText(String.format(Locale.US, "%d", cnt));
    }

    private void getViews() {
        mImage = findViewById(R.id.itemImage);
        mTitle = findViewById(R.id.itemTitle);
        mDescription = findViewById(R.id.itemDescription);
        mPrice = findViewById(R.id.itemPrice);
        mAdd = findViewById(R.id.itemPlus);
        mRemove = findViewById(R.id.itemMinus);
        mAddToCart = findViewById(R.id.addToCart);
        mAmount = findViewById(R.id.itemAmount);
        mTitleBar = findViewById(R.id.titlebar);
    }

}
