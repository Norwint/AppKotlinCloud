package com.otcengineering.white_app.keyless.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.otcengineering.white_app.R;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingHolder> {
    private ArrayList<BookingItem> mItems;
    private BookingItem mFirst, mLast;

    public BookingAdapter() {
        mItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public BookingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_booking, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookingHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    public void addItem(BookingItem item) {
        mItems.add(item);
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public LocalDateTime[] getBookedHours(LocalDate date) {
        LocalDateTime[] hours = new LocalDateTime[2];
        hours[0] = mFirst.getDate(0, date);
        hours[1] = mLast.getDate(1, date);
        return hours;
    }

    public boolean hasSelected() {
        return mFirst != null && mLast != null;
    }

    class BookingHolder extends RecyclerView.ViewHolder {
        private TextView mTime;
        private LinearLayout mLayout;
        private BookingItem mItem;

        BookingHolder(@NonNull View itemView) {
            super(itemView);

            mTime = itemView.findViewById(R.id.time);
            mLayout = itemView.findViewById(R.id.layout);
        }

        public void bind(BookingItem bookingItem) {
            mItem = bookingItem;
            int clr = 0;
            switch (bookingItem.bookedState) {
                case 0: clr = Color.argb(255, 51, 255, 51); break;
                case 1: clr = Color.argb(255, 255, 51, 51); break;
                case 2: clr = Color.argb(255, 0, 255, 255); break;
                case 3: clr = Color.argb(255, 115, 125, 132); break;
            }
            mLayout.setBackgroundColor(clr);
            mTime.setText(String.format(Locale.US, "%02d:%02d", bookingItem.hour, bookingItem.quarter * 15));

            mLayout.setOnClickListener(v -> {
                if (mItem.bookedState == 1 || mItem.bookedState == 3) return;
                if (mFirst == null) {
                    mItem.bookedState = 2;
                    mFirst = bookingItem;
                    update();
                } else if (mLast == null) {
                    mItem.bookedState = 2;
                    if (mItem.combineHourQuarter() < mFirst.combineHourQuarter()) {
                        mLast = mFirst;
                        mFirst = bookingItem;
                    } else {
                        mLast = bookingItem;
                    }
                    int firstHour = mFirst.combineHourQuarter();
                    int lastHour = mLast.combineHourQuarter();
                    for (int i1 = 0; i1 < mItems.size(); i1++) {
                        BookingItem i = mItems.get(i1);
                        int time = i.combineHourQuarter();
                        if (i.bookedState != 1 && time > firstHour && time < lastHour) {
                            i.bookedState = 2;
                        } else if (time > firstHour && time < lastHour && i.bookedState == 1) {
                            mLast.bookedState = 0;
                            mLast = mItems.get(i1 - 1);
                            update();
                            return;
                        }
                    }
                    update();
                } else {
                    mFirst = bookingItem;
                    mLast = null;
                    for (BookingItem i : mItems) if (i.bookedState == 2) i.bookedState = 0;
                    bookingItem.bookedState = 2;
                    update();
                }
            });
        }
    }

    public static class BookingItem {
        private int hour, quarter;
        private byte bookedState;

        public BookingItem(int h, int q, byte b) {
            hour = h;
            quarter = q;
            bookedState = b;
        }

        public void setBookedState(byte state) {
            this.bookedState = state;
        }

        public byte getBookedState() {
            return bookedState;
        }

        public int combineHourQuarter() {
            return hour * 100 + (quarter * 15);
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.US, "%02d:%02d:00", hour, quarter * 15);
        }

        public LocalDateTime getDate(int addQuarters, LocalDate date) {
            int q = quarter + addQuarters;
            int h = hour;
            while (q > 4) {
                h++;
                q -= 4;
            }
            return LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0).plusHours(h).plusMinutes(q * 15);
        }
    }
}
