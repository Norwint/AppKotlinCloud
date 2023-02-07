package com.otcengineering.white_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.otcengineering.white_app.utils.Constants;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.activities.ChartActivity;
import com.otcengineering.white_app.serialization.pojo.EcoItem;
import com.otcengineering.white_app.serialization.pojo.MileageItem;
import com.otcengineering.white_app.serialization.pojo.SafetyItem;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class ChartItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface ChartItemListener {
        void onShare(int position);

        void onMileageSelected(int position);
    }

    private Context context;
    private List<ChartItemInList> chartItems = new ArrayList<>();
    private int chartItemType;
    private int timeType;
    private ChartItemListener listener;

    public ChartItemAdapter(Context context, int chartItemType, ChartItemListener listener) {
        super();
        this.context = context;
        this.chartItemType = chartItemType;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return chartItems.size();
    }

    public Object getItem(int position) {
        return chartItems.get(position).object;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_chart_item, viewGroup, false);
        return new ChartItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ChartItemHolder itemHolder = (ChartItemHolder) holder;
        int globalPosition = 0;
        if (chartItemType == Constants.ChartMode.MILEAGE) {
            MileageItem mileageItem = (MileageItem) chartItems.get(position).object;
            showMileageInfo(itemHolder, mileageItem);
            globalPosition = mileageItem.getGlobalRanking();
        } else if (chartItemType == Constants.ChartMode.ECO) {
            EcoItem ecoItem = (EcoItem) chartItems.get(position).object;
            showEcoInfo(itemHolder, ecoItem);
            globalPosition = ecoItem.getGlobalRanking();
        } else if (chartItemType == Constants.ChartMode.SAFETY) {
            SafetyItem safetyItem = (SafetyItem) chartItems.get(position).object;
            showSafetyInfo(itemHolder, safetyItem);
        }

        boolean selected = chartItems.get(position).selected;

        if (selected) {
            itemHolder.layoutRoot.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            itemHolder.txtDate.setTextColor(context.getResources().getColor(R.color.colorWhite));
            itemHolder.txtUp.setTextColor(context.getResources().getColor(R.color.colorWhite));
            itemHolder.txtDown.setTextColor(context.getResources().getColor(R.color.colorWhite));
            itemHolder.imgRedLines.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_4));

            if (globalPosition == 2) {
                itemHolder.imgTrophy.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_9));
            } else if (globalPosition == 1) {
                itemHolder.imgTrophy.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_5));
            }
        } else {
            itemHolder.layoutRoot.setBackgroundColor(context.getResources().getColor(R.color.chart_row_color));
            itemHolder.txtDate.setTextColor(context.getResources().getColor(R.color.signup_sep));
            itemHolder.txtUp.setTextColor(context.getResources().getColor(R.color.quantum_black_100));
            itemHolder.txtDown.setTextColor(context.getResources().getColor(R.color.signup_sep));
            itemHolder.imgRedLines.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_3));

            if (globalPosition == 2) {
                itemHolder.imgTrophy.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_10));
            } else if (globalPosition == 1) {
                itemHolder.imgTrophy.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_17));
            } else {
                itemHolder.imgTrophy.setImageDrawable(null);
            }
        }

        itemHolder.layoutRoot.setOnClickListener(v -> {
            setRowSelected(position);
            listener.onMileageSelected(position);
        });

        itemHolder.btnShare.setOnClickListener(v -> {
            listener.onShare(position);
            itemHolder.layoutSwipe.close(true);
        });
    }

    private void showMileageInfo(ChartItemHolder itemHolder, MileageItem mileageItem) {
        itemHolder.txtDate.setText(getDateFormatted(mileageItem.getDate()));
        itemHolder.txtUp.setText(String.format(Locale.US, "%.1f km - %s", mileageItem.getDistanceInKms(), mileageItem.getDuration()));
        int globalRanking = mileageItem.getGlobalRanking();
        int localRanking = mileageItem.getLocalRanking();
        itemHolder.txtDown.setVisibility(View.VISIBLE);
        itemHolder.txtDown.setText(String.format(Locale.US, "Local: Top %d", localRanking));

        if (globalRanking == 2) {
            itemHolder.imgTrophy.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_10));
        } else if (globalRanking == 1) {
            itemHolder.imgTrophy.setImageDrawable(context.getResources().getDrawable(R.drawable.my_drive_icons_17));
        } else {
            itemHolder.imgTrophy.setImageDrawable(null);
        }
    }

    private void showEcoInfo(ChartItemHolder itemHolder, EcoItem ecoItem) {
        itemHolder.txtDate.setText(getDateFormatted(ecoItem.getDate()));
        itemHolder.txtUp.setText(String.format(Locale.US, "%.1f l - %.1f km/l - %s", ecoItem.getTotalConsumption() / 100.0f, ecoItem.getAverageConsumption() * 100, ecoItem.getDuration()));
        int localRanking = ecoItem.getLocalRanking();
        itemHolder.txtDown.setVisibility(View.VISIBLE);
        itemHolder.txtDown.setText(String.format(Locale.US, "Local: Top %d", localRanking));
    }


    private void showSafetyInfo(ChartItemHolder itemHolder, SafetyItem safetyItem) {
        itemHolder.txtDate.setText(getDateFormatted(safetyItem.getDate()));
        itemHolder.txtUp.setText(String.format(Locale.US, "Score %.1f - %s", safetyItem.getDrivingTechinique(), safetyItem.getDuration()));
        itemHolder.txtDown.setVisibility(View.GONE);
    }

    private String getDateFormatted(LocalDate date) {
        if (timeType == ChartActivity.DAILY) {
            return String.format(Locale.US, "%s\n%d", DateUtils.getDayAndMonthFormatted(date), date.getYear());
        } else if (timeType == ChartActivity.WEEKLY) {
            LocalDate endOfWeek = date.plus(6, ChronoUnit.DAYS);
            return String.format(Locale.US, "%s\n%s\n%d", DateUtils.getDayAndMonthFormatted(date), DateUtils.getDayAndMonthFormatted(endOfWeek), endOfWeek.getYear());
        } else if (timeType == ChartActivity.MONTHLY) {
            return String.format(Locale.US, "%s\n%d", DateUtils.getMonthFormatted(date), date.getYear());
        }
        return "";
    }

    private Date getEndOfWeek(Date dateStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateStart);
        calendar.add(Calendar.DATE, 6);
        return calendar.getTime();
    }

    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    public void clearItems() {
        this.chartItems.clear();
    }

    public void addMileageItems(List<MileageItem> items) {
        for (MileageItem mileageItem : items) {
            this.chartItems.add(new ChartItemInList(mileageItem));
        }
    }

    public void addEcoItems(List<EcoItem> items) {
        for (EcoItem ecoItem : items) {
            this.chartItems.add(new ChartItemInList(ecoItem));
        }
    }

    public void addSafetyItems(List<SafetyItem> items) {
        for (SafetyItem safetItem : items) {
            this.chartItems.add(new ChartItemInList(safetItem));
        }
    }

    public void setRowSelected(int position) {
        for (int i = 0; i < chartItems.size(); i++) {
            ChartItemInList chartItemInList = chartItems.get(i);
            chartItemInList.selected = i == position;
        }
        notifyDataSetChanged();
    }

    public String[] getData(int position) {
        String[] data = new String[4];
        if (chartItemType == Constants.ChartMode.MILEAGE) {
            MileageItem mileageItem = (MileageItem) chartItems.get(position).object;
            data[0] = getDateFormatted(mileageItem.getDate());
            data[1] = String.format(Locale.US, "%.1f km - %s", mileageItem.getDistanceInKms(), mileageItem.getDuration());
            int localRanking = mileageItem.getLocalRanking();
            data[2] = String.format(Locale.US, "Local: Top %d", localRanking);
            data[3] = String.valueOf(chartItemType);
        } else if (chartItemType == Constants.ChartMode.ECO) {
            EcoItem ecoItem = (EcoItem) chartItems.get(position).object;
            data[0] = getDateFormatted(ecoItem.getDate());
            data[1] = String.format(Locale.US, "%.1f l - %.1f km/l - %s", ecoItem.getTotalConsumption() / 100.0f, ecoItem.getAverageConsumption(), ecoItem.getDuration());
            int localRanking = ecoItem.getLocalRanking();
            data[2] = String.format(Locale.US, "Local: Top %d", localRanking);
            data[3] = String.valueOf(chartItemType);
        } else if (chartItemType == Constants.ChartMode.SAFETY) {
            SafetyItem safetyItem = (SafetyItem) chartItems.get(position).object;
            data[0] = getDateFormatted(safetyItem.getDate());
            data[1] = String.format(Locale.US, "%s %.1f - %s", context.getString(R.string.score), safetyItem.getDrivingTechinique(), safetyItem.getDuration());
            data[2] = "";
            data[3] = String.valueOf(chartItemType);
        }

        return data;
    }

    private class ChartItemInList {
        private Object object;
        private boolean selected;

        ChartItemInList(Object object) {
            this.object = object;
            this.selected = false;
        }
    }

    protected class ChartItemHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutRoot;
        private FrameLayout btnShare;
        private TextView txtDate;
        private TextView txtUp;
        private TextView txtDown;
        private ImageView imgTrophy;
        private ImageView imgRedLines;
        private SwipeRevealLayout layoutSwipe;

        ChartItemHolder(View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.row_chart_item_layoutRoot);
            btnShare = itemView.findViewById(R.id.row_chart_item_btnShare);
            txtDate = itemView.findViewById(R.id.row_chart_item_txtDate);
            txtUp = itemView.findViewById(R.id.row_chart_item_txtUp);
            txtDown = itemView.findViewById(R.id.row_chart_item_txtDown);
            imgTrophy = itemView.findViewById(R.id.row_chart_item_imgTrophy);
            imgRedLines = itemView.findViewById(R.id.row_chart_item_imgRedLines);
            layoutSwipe = itemView.findViewById(R.id.row_chart_item_layoutSwipeRoot);
        }
    }

}