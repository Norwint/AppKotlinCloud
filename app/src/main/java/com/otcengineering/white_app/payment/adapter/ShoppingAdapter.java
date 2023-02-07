package com.otcengineering.white_app.payment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.payment.fragment.ShoppingFragment;
import com.otcengineering.white_app.utils.payment.PaymentUtils;
import com.otcengineering.white_app.utils.payment.PaymentItem;

import java.util.ArrayList;
import java.util.Locale;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingHolder> {
    private Context mContext;
    private ShoppingFragment mParent;
    private ArrayList<PaymentItem> mItems;
    private ArrayList<PaymentItem> mTempItems;

    public ShoppingAdapter(final ShoppingFragment parent) {
        mContext = parent.getContext();
        mParent = parent;
        mItems = new ArrayList<>();
        mTempItems = new ArrayList<>();
    }

    public void addItem(PaymentItem item) {
        mItems.add(item);
    }

    public void update() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShoppingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_shopping, parent, false);
        return new ShoppingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingHolder holder, int position) {
        holder.bind(mTempItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mTempItems.size();
    }

    public boolean setFilter(String filter) {
        mTempItems.clear();
        if (filter.isEmpty()) {
            Stream.of(mItems).forEach(it -> mTempItems.add(it));
        } else {
            Stream.of(mItems).filter(it -> it.getName().toLowerCase().contains(filter.toLowerCase())).forEach(it -> mTempItems.add(it));
        }
        return mTempItems.isEmpty();
    }

    public void clearItems() {
        mItems.clear();
        mTempItems.clear();
    }

    class ShoppingHolder extends RecyclerView.ViewHolder {
        private PaymentItem mItem;
        private ImageView mImage;
        private TextView mTitle, mPrice;
        private ConstraintLayout mItemLayout;

        public ShoppingHolder(@NonNull View itemView) {
            super(itemView);

            mItemLayout = itemView.findViewById(R.id.itemLayout);
            mImage = itemView.findViewById(R.id.image);
            mTitle = itemView.findViewById(R.id.title);
            mPrice = itemView.findViewById(R.id.price);
        }

        public void bind(PaymentItem item) {
            mItem = item;

            mTitle.setText(item.getName());
            mPrice.setText(String.format(Locale.US, "%02.02f %s", item.getPrice(), PaymentUtils.Currency.selectedCurrency.getSymbol()));
            Glide.with(mContext).load(item.getImage()).placeholder(R.drawable.icon_android).into(mImage);
            mItemLayout.setOnClickListener(v -> {
                mParent.showItem(item);
            });
        }
    }
}
