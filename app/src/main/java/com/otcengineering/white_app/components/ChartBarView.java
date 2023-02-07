package com.otcengineering.white_app.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.otc.alice.api.model.General;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.DateUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.List;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class ChartBarView extends FrameLayout {

    private static final int CHART_BAR_WIDTH_THIN = 30;
    private static final int CHART_BAR_WIDTH_FAT = 50;

    private static final int FRAGMENT_HEIGHT = 3;

    private static final int CHART_BAR_THIN_RADIUS = 15;
    private static final int CHART_BAR_FAT_RADIUS = 30;

    private static final int TEXT_SIZE_BIG = 18;
    private static final int TEXT_SIZE_SMALL = 14;

    private static final int DAILY = General.TimeType.DAILY.getNumber();
    private static final int WEEKLY = General.TimeType.WEEKLY.getNumber();
    private static final int MONTHLY = General.TimeType.MONTHLY.getNumber();

    private FrameLayout layoutChart;
    private TextView txtUp;
    private TextView txtDown;
    private View chartBar;

    private Context context;
    private int timeType = DAILY;
    private int layoutChartHeight;
    private int chartBarCornerRadius;
    private int chartBarHeight;
    private int chartBarWidth;

    private int colorRed;
    private int colorBlue;
    private int colorWhiteAlpha;

    private LocalDate dateStart;
    private List<Double> fragments;
    private double value;
    private double maxValue;

    private boolean highlighted;

    public ChartBarView(Context context) {
        super(context);
    }

    public ChartBarView(Context context, int timeType, LocalDate dateStart, List<Double> fragments, double value, double maxValue) {
        super(context);
        this.context = context;
        this.timeType = timeType;
        this.dateStart = dateStart;
        this.fragments = fragments;
        this.value = value;
        this.maxValue = maxValue;
        initView();
    }

    private void initView() {
        inflateView(context);
        calculateLayoutHeight();
        colorRed = context.getResources().getColor(R.color.marcadorMileage);
        colorBlue = context.getResources().getColor(R.color.signup_sep);
        colorWhiteAlpha = context.getResources().getColor(R.color.colorWhiteTrans);
    }

    private void calculateLayoutHeight() {
        layoutChart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layoutChart.getMeasuredHeight() != 0) {
                    layoutChartHeight = layoutChart.getMeasuredHeight();
                    layoutChart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showBarAndTexts();
                }
            }
        });
    }

    private void inflateView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_chart_bar, this, true);
        layoutChart = view.findViewById(R.id.chart_bar_layoutChart);
        txtUp = view.findViewById(R.id.chart_bar_txtUp);
        txtDown = view.findViewById(R.id.chart_bar_txtDown);
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        txtUp.setTextColor(highlighted ? colorRed : colorBlue);
        txtDown.setTextColor(highlighted ? colorRed : colorBlue);
        chartBar.setBackground(createBarDrawable());
    }

    private void showBarAndTexts() {
        calculateMetrics();
        showTexts();
        drawChartBar();
        drawFragments();
    }

    private void calculateMetrics() {
        chartBarHeight = (int) (value * layoutChartHeight / maxValue);
        if (timeType == DAILY) {
            chartBarWidth = CHART_BAR_WIDTH_THIN;
            chartBarCornerRadius = CHART_BAR_THIN_RADIUS;
        } else if (timeType == WEEKLY) {
            chartBarWidth = CHART_BAR_WIDTH_FAT;
            chartBarCornerRadius = CHART_BAR_FAT_RADIUS;
        } else if (timeType == MONTHLY) {
            chartBarWidth = CHART_BAR_WIDTH_THIN;
            chartBarCornerRadius = CHART_BAR_THIN_RADIUS;
        }
    }

    private void showTexts() {
        if (timeType == DAILY) {
            txtUp.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_BIG);
            txtDown.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL);
            txtUp.setText("" + dateStart.getDayOfMonth());
            txtDown.setText(dateStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
        } else if (timeType == WEEKLY) {
            txtUp.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL);
            txtDown.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL);
            txtUp.setText(DateUtils.getDayAndMonthFormatted(dateStart));
            LocalDate dateEnd = dateStart.plus(6, ChronoUnit.DAYS);
            txtDown.setText(DateUtils.getDayAndMonthFormatted(dateEnd));
        } else if (timeType == MONTHLY) {
            txtUp.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_BIG);
            txtDown.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL);
            txtUp.setText(dateStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            txtDown.setText("" + dateStart.getYear());
        }
    }

    private void drawChartBar() {
        if (layoutChartHeight == 0) return;

        chartBar = new View(context);
        LayoutParams layoutParams = new LayoutParams(chartBarWidth, chartBarHeight);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        chartBar.setLayoutParams(layoutParams);
        chartBar.setBackground(createBarDrawable());
        layoutChart.addView(chartBar);
    }

    private void drawFragments() {
        if (fragments == null) return;

        for (Double fragment : fragments) {
            if (fragment <= value) {
                View view = new View(context);
                LayoutParams layoutParams = new LayoutParams(chartBarWidth, FRAGMENT_HEIGHT);
                int marginBottom = (int) (fragment * chartBarHeight / value);
                layoutParams.setMargins(0, 0, 0, marginBottom);
                layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(colorWhiteAlpha);
                layoutChart.addView(view);
            }
        }
    }

    private GradientDrawable createBarDrawable() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(chartBarCornerRadius);
        shape.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        shape.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shape.setColors(highlighted ? getRedColors() : getBlueColors());
        return shape;
    }

    private int[] getBlueColors() {
        return new int[]{
                getResources().getColor(R.color.chart_bar_blue_start),
                getResources().getColor(R.color.chart_bar_blue_center),
                getResources().getColor(R.color.chart_bar_blue_end)};
    }

    private int[] getRedColors() {
        return new int[]{
                getResources().getColor(R.color.chart_bar_red_start),
                getResources().getColor(R.color.chart_bar_red_center),
                getResources().getColor(R.color.chart_bar_red_end)};
    }


}
