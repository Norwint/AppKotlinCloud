package com.otcengineering.white_app.payment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.payment.activity.CartActivity;
import com.otcengineering.white_app.components.DialogYesNo;
import com.otcengineering.white_app.network.PaymentNetwork;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import java.util.ArrayList;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentHolder> {
    private ArrayList<PaymentItem> mItems;
    private CartActivity mContext;
    private Runnable mCallback;
    private PaymentUtils.Currency mCurrency;

    public PaymentAdapter(final CartActivity ctx, final Runnable callback) {
        mContext = ctx;
        mCallback = callback;
        mItems = new ArrayList<>();
        mCurrency = PaymentUtils.Currency.selectedCurrency;
    }

    public void changeCurrency(PaymentUtils.Currency newCurrency) {
        mCurrency = newCurrency;
        mCallback.run();
        notifyDataSetChanged();
    }

    public ArrayList<PaymentItem> getItems() {
        return mItems;
    }

    @NonNull
    @Override
    public PaymentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_payment, parent, false);
        return new PaymentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentHolder holder, int position) {
        PaymentItem item = mItems.get(position);

        holder.bind(item);
    }

    public String getCurrency() {
        return mCurrency.getCodeFromCurrent();
    }

    public void addItem(PaymentItem item) {
        mItems.add(item);
    }

    public void deleteItem(PaymentItem item) {
        setItemAmount(item, 0);
    }

    private void setItemAmount(PaymentItem item, int cnt) {
        PaymentItem cp = PaymentItem.copy(item);
        cp.setAmount(cnt);
        PaymentNetwork.modifyItemToShoppingCart(cp, (success, data) -> {
            if (success && cnt == 0) {
                mContext.getCart();
            }
        });
    }

    public boolean hasRottenItem() {
        return Stream.of(mItems).filter(it -> !it.isEnabled()).count() > 0;
    }

    public double getCheckedPrice() {
        double sum = 0;
        for (PaymentItem pi : mItems) {
            if (pi.isEnabled()) {
                double val = pi.getAmount() * pi.getPrice() + pi.getShipment().get(0).getCosts();
                sum += val;
            }
        }
        return sum;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(ArrayList<PaymentItem> list) {
        mItems = list;
    }

    public void addItems(ArrayList<PaymentItem> items) {
        mItems.addAll(items);
    }

    public double getShipAmount() {
        double amount = 0;
        for (PaymentItem pi : mItems) {
            amount += pi.getShipment().get(0).getCosts();
        }
        return amount;
    }

    class PaymentHolder extends RecyclerView.ViewHolder {
        private TextView mItemName, mItemPrice, mItemAmount;
        private ImageView mItemImage;
        private Button mItemPlus, mItemMinus, mItemDelete;

        private PaymentItem mItem;

        PaymentHolder(@NonNull View itemView) {
            super(itemView);

            mItemName = itemView.findViewById(R.id.text);
            mItemPrice = itemView.findViewById(R.id.itemPrice);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mItemPlus = itemView.findViewById(R.id.itemPlus);
            mItemMinus = itemView.findViewById(R.id.itemMinus);
            mItemAmount = itemView.findViewById(R.id.itemAmount);
            mItemDelete = itemView.findViewById(R.id.itemDelete);

            mItemPlus.setOnClickListener(v -> {
                if (mItem.getAmount() < 100) {
                    mItem.increaseAmount();
                }
                mItemAmount.setText(Integer.toString(mItem.getAmount()));
                mCallback.run();
                setItemAmount(mItem, mItem.getAmount());
            });

            mItemMinus.setOnClickListener(v -> {
                if (mItem.getAmount() > 1) {
                    mItem.decreaseAmount();
                }
                mItemAmount.setText(Integer.toString(mItem.getAmount()));
                mCallback.run();
                setItemAmount(mItem, mItem.getAmount());
            });

            mItemDelete.setOnClickListener(v -> {
                DialogYesNo dyn = new DialogYesNo(mContext);
                dyn.setMessage("Do you want to delete the item from the cart?");
                dyn.setYesButtonClickListener(() -> {
                    deleteItem(mItem);
                    notifyDataSetChanged();
                    mCallback.run();
                });
                dyn.show();
            });
        }

        private void bind(PaymentItem item) {
            mItem = item;

            mItemName.setText(item.getName());
            String symbol = mCurrency.getSymbol();
            mItemPrice.setText(String.format(Locale.US, "%01.02f%s", mItem.getPrice(), symbol));
            Glide.with(mContext).load(item.getImage()).placeholder(R.drawable.icon).into(mItemImage);
            mItemAmount.setText(String.format(Locale.US, "%d", mItem.getAmount()));

            if (item.getPromo() || !item.isEnabled()) {
                mItemPlus.setVisibility(View.INVISIBLE);
                mItemMinus.setVisibility(View.INVISIBLE);
                mItemAmount.setVisibility(View.INVISIBLE);
            } else {
                mItemPlus.setVisibility(View.VISIBLE);
                mItemMinus.setVisibility(View.VISIBLE);
                mItemAmount.setVisibility(View.VISIBLE);
            }

            if (!item.isEnabled()) {
                mItemPrice.setText("Out of stock");
            }
        }
    }
}