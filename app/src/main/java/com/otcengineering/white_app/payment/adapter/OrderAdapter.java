package com.otcengineering.white_app.payment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.otc.alice.api.model.Payment;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.payment.activity.OrderActivity;
import com.otcengineering.white_app.utils.DateUtils;
import com.otcengineering.white_app.utils.payment.PaymentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderHolder> {
    private ArrayList<Payment.Order> mList;
    private Context mContext;

    public OrderAdapter(final Context ctx) {
        mContext = ctx;
        mList = new ArrayList<>();
    }

    public void setItems(List<Payment.Order> orders) {
        mList.clear();
        mList.addAll(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderHolder(LayoutInflater.from(mContext).inflate(R.layout.row_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class OrderHolder extends RecyclerView.ViewHolder {
        private TextView mOrderId, mOrderDate, mOrderPrice;
        private Payment.Order mOrder;

        OrderHolder(@NonNull View itemView) {
            super(itemView);

            mOrderId = itemView.findViewById(R.id.orderId);
            mOrderDate = itemView.findViewById(R.id.date);
            mOrderPrice = itemView.findViewById(R.id.price);
            itemView.findViewById(R.id.orderLayout).setOnClickListener(v -> {
                Intent intent = new Intent(mContext, OrderActivity.class);
                intent.putExtra("OrderId", mOrder.getOrderId());
                mContext.startActivity(intent);
            });
        }

        void bind(Payment.Order order) {
            mOrder = order;

            mOrderId.setText(String.format(Locale.US, "#%019d", order.getOrderId()));
            mOrderDate.setText(DateUtils.utcStringToLocalString(order.getDate(), "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy - HH:mm:ss"));
            String curr = order.getCurrencyCode();
            mOrderPrice.setText(String.format(Locale.US, "%01.02f %s", order.getTotalAmount(), PaymentUtils.Currency.getByCode(curr).getSymbol()));
        }
    }
}
