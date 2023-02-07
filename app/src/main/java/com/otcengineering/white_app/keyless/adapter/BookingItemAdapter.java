package com.otcengineering.white_app.keyless.adapter;

import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.interfaces.NetworkCallback;
import com.otcengineering.white_app.serialization.BookingInfo;
import com.otcengineering.white_app.tasks.NetTask;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

public class BookingItemAdapter extends RecyclerView.Adapter<BookingItemAdapter.BookingItemHolder> {
    private ArrayList<BookingItem> mItemList = new ArrayList<>();

    @NonNull
    @Override
    public BookingItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookingItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_booking_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookingItemHolder holder, int position) {
        holder.bind(mItemList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void clear() {
        mItemList.clear();
        update();
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void addItem(BookingItem bi) {
        mItemList.add(bi);
    }

    class BookingItemHolder extends RecyclerView.ViewHolder {
        private BookingItem mItem;
        private TextView mItemReserve, mItemRemove;
        private SwipeRevealLayout mSwipeLayout;

        public BookingItemHolder(@NonNull View itemView) {
            super(itemView);

            mItemRemove = itemView.findViewById(R.id.itemRemove);
            mItemReserve = itemView.findViewById(R.id.itemReserve);
            mSwipeLayout = itemView.findViewById(R.id.swipeLayout);
        }

        public void bind(BookingItem bookingItem, int position) {
            mItem = bookingItem;

            mItemReserve.setText(String.format(Locale.US, "From %s to %s - Vehicle %d", mItem.getStartHour(), mItem.getEndHour(), mItem.mInfo.getVehicleId()));
            mItemRemove.setOnClickListener(v -> {
                ProgressDialog pd = new ProgressDialog(MyApp.getContext());
                pd.setMessage(MyApp.getContext().getString(R.string.loading));
                pd.setCancelable(false);
                pd.show();
                NetTask task = new NetTask("v2/booking/" + mItem.mInfo.getId(), null, true, "DELETE", new NetworkCallback<NetTask.JsonResponse>() {
                    @Override
                    public void onSuccess(NetTask.JsonResponse response) {
                        pd.dismiss();
                        mItemList.remove(position);
                        update();
                    }

                    @Override
                    public void onFailure(int code, String errorMsg) {
                        Toast.makeText(MyApp.getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
                task.execute();
            });
            mSwipeLayout.close(false);
        }
    }

    public static class BookingItem {
        private BookingInfo mInfo;

        public BookingItem(BookingInfo info) {
            mInfo = info;
        }

        public String getStartHour() {
            LocalTime ldt = DateUtils.utcStringToDateTime(mInfo.getStartDate(), "yyyy-MM-dd HH:mm:ss", ZoneId.systemDefault()).toLocalTime();
            return ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        public String getEndHour() {
            LocalTime ldt = DateUtils.utcStringToDateTime(mInfo.getEndDate(), "yyyy-MM-dd HH:mm:ss", ZoneId.systemDefault()).toLocalTime();
            return ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }
}
