package com.otcengineering.white_app.payment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.payment.PaymentItem;
import com.otcengineering.white_app.utils.payment.PaymentUtils;

import java.util.ArrayList;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemHolder> {
    private ArrayList<PaymentItem> mItems;
    private Context mContext;

    public OrderItemAdapter(final Context ctx) {
        mContext = ctx;
        mItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_review, parent, false);
        return new OrderItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemHolder holder, int position) {
        PaymentItem item = mItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(ArrayList<PaymentItem> items) {
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    class OrderItemHolder extends RecyclerView.ViewHolder {
        private TextView mTitle, mPrice;
        private ConstraintLayout mItemLayout;

        public OrderItemHolder(@NonNull View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.text);
            mPrice = itemView.findViewById(R.id.price);
            mItemLayout = itemView.findViewById(R.id.itemLayout);
        }

        public void bind(PaymentItem item) {
            mTitle.setText(String.format(Locale.US, "%dx %s", item.getAmount(), item.getName()));
            mPrice.setText(String.format(Locale.US, "%02.02f %s", item.getAmount() * item.getPrice(), PaymentUtils.Currency.selectedCurrency.getSymbol()));
            /*mItemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                intent.putExtra("Item", Utils.getGson().toJson(item));
                mContext.startActivity(intent);
            });*/
        }
    }
}
