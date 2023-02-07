package com.otcengineering.white_app.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otcengineering.white_app.R;
import com.otcengineering.white_app.utils.PrefsManager;

/**
 * Created by cenci7
 */

public class CustomTabLayout extends FrameLayout {

    private static final int TEXT_SIZE = 14;

    private static final int SIDE_LINE_WIDTH = 15;
    private static final int SIDE_LINE_WIDTH_4_TABS = 8;
    private static final int SHADOW_HEIGHT = 5;

    public interface ChangeTabListener {
        void onTabChanged(int tabSelected);
    }

    private LinearLayout layoutRoot;
    private TextView txtTab1, txtTab2, txtTab3, txtTab4;
    private View tab1shadow, tab2shadow, tab3shadow, tab4shadow;
    private ImageView imgIndicator;

    private Context context;

    private ChangeTabListener listener;

    private boolean showIndicator;

    int selectedTextColor = getResources().getColor(R.color.tab_text_selected);
    int unselectedTextColor = getResources().getColor(R.color.tab_text_unselected);
    Drawable selectedBackground = getResources().getDrawable(R.drawable.tab_sel);
    Drawable unselectedBackground = getResources().getDrawable(R.drawable.tab_unsel);

    private String tab1text;
    private String tab2text;
    private String tab3text;
    private String tab4text;

    private int tabs;

    private int tab1value;
    private int tab2value;
    private int tab3value;
    private int tab4value;

    private Integer tabSelected;

    public CustomTabLayout(@NonNull Context context) {
        super(context);
    }

    public CustomTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        configureView(attrs);
    }

    public void configure(ChangeTabListener listener, Integer... values) {
        this.listener = listener;
        setValues(values);
        manageTabChanged(tab1value); //initialize selected tab
    }

    public void configure(int tabSelected, ChangeTabListener listener, Integer... values) {
        this.listener = listener;
        setValues(values);
        manageTabChanged(tabSelected); //initialize selected tab
    }

    private void setValues(Integer[] values) {
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if (i == 0) {
                tab1value = value;
            } else if (i == 1) {
                tab2value = value;
            } else if (i == 2) {
                tab3value = value;
            } else if (i == 3) {
                tab4value = value;
            }
        }
    }

    private void configureView(AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.layout_custom_tab, this, true);
            layoutRoot = view.findViewById(R.id.layout_custom_tab_layoutRoot);
            retrieveAttributes(attrs);
            createViews();
        }
    }

    private void retrieveAttributes(AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomTabLayout);
        tab1text = attributes.getString(R.styleable.CustomTabLayout_tab1text);
        tab2text = attributes.getString(R.styleable.CustomTabLayout_tab2text);
        tab3text = attributes.getString(R.styleable.CustomTabLayout_tab3text);
        tab4text = attributes.getString(R.styleable.CustomTabLayout_tab4text);
        tabs = attributes.getInt(R.styleable.CustomTabLayout_tabs, 3);
        showIndicator = attributes.getBoolean(R.styleable.CustomTabLayout_showTabIndicator, false);
        attributes.recycle();
    }

    private void createViews() {
        createSideLine();
        addTab1();
        addTab2();
        addTab3();
        addTab4();
        createSideLine();
    }

    private void addTab1() {
        txtTab1 = createTab(tab1text, true);
        txtTab1.setOnClickListener(view -> manageTabChanged(tab1value));
        tab1shadow = createTabShadow(true);
        addTab(txtTab1, tab1shadow, null);
    }

    private void addTab2() {
        txtTab2 = createTab(tab2text, false);
        txtTab2.setOnClickListener(view -> manageTabChanged(tab2value));
        tab2shadow = createTabShadow(false);
        imgIndicator = createImageViewIndicator();
        addTab(txtTab2, tab2shadow, imgIndicator);
    }

    private ImageView createImageViewIndicator() {
        boolean hasNewContent = PrefsManager.getInstance().getHasNewContent(getContext());
        if (showIndicator && hasNewContent) {
            ImageView imgIndicator = new ImageView(context);
            imgIndicator.setImageResource(R.drawable.indicator);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int margin = (int) (metrics.density * 7);
            params.setMargins(0, margin, margin, 0);
            imgIndicator.setLayoutParams(params);
            return imgIndicator;
        }
        return null;
    }

    private void addTab3() {
        if (tab3text != null) {
            txtTab3 = createTab(tab3text, false);
            txtTab3.setOnClickListener(view -> manageTabChanged(tab3value));
            tab3shadow = createTabShadow(false);
            addTab(txtTab3, tab3shadow, null);
        }
    }

    private void addTab4() {
        if (tab4text != null) {
            tabs = 4;
            txtTab4 = createTab(tab4text, false);
            txtTab4.setOnClickListener(view -> manageTabChanged(tab4value));
            tab4shadow = createTabShadow(false);
            addTab(txtTab4, tab4shadow, null);
        }
    }

    private void addTab(TextView txtTab, View tabShadow, View indicator) {
        FrameLayout frameLayout = new FrameLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        frameLayout.setLayoutParams(params);

        frameLayout.addView(txtTab);
        frameLayout.addView(tabShadow);
        if (indicator != null) {
            frameLayout.addView(indicator);
        }

        layoutRoot.addView(frameLayout);
    }

    @NonNull
    private TextView createTab(String tabText, boolean selected) {
        TextView txtTab;
        txtTab = new TextView(context);
        txtTab.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        txtTab.setGravity(Gravity.CENTER);
        txtTab.setBackground(selected ? selectedBackground : unselectedBackground);
        txtTab.setText(tabText);
        txtTab.setTextColor(selected ? selectedTextColor : unselectedTextColor);
        txtTab.setTextSize(TEXT_SIZE);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
        txtTab.setTypeface(typeface);
        return txtTab;
    }

    @NonNull
    private View createTabShadow(boolean selected) {
        float density = getResources().getDisplayMetrics().density;
        int shadowHeight = (int) (SHADOW_HEIGHT * density);

        View tabShadow;
        tabShadow = new View(context);
        tabShadow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, shadowHeight, Gravity.BOTTOM));
        tabShadow.setBackground(getResources().getDrawable(R.drawable.tab_shadow));
        tabShadow.setVisibility(selected ? View.GONE : View.VISIBLE);
        return tabShadow;
    }

    private void createSideLine() {
        float density = getResources().getDisplayMetrics().density;
        int sideLineWidth = (int) ((3 * 15 / tabs) * density);
        int shadowHeight = (int) (SHADOW_HEIGHT * density);

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new LayoutParams(sideLineWidth, ViewGroup.LayoutParams.MATCH_PARENT));

        View view = new View(context);
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, shadowHeight, Gravity.BOTTOM));
        view.setBackground(getResources().getDrawable(R.drawable.tab_shadow));

        frameLayout.addView(view);

        layoutRoot.addView(frameLayout);
    }

    private void manageTabChanged(int tabSelected) {
        this.tabSelected = tabSelected;
        if (listener != null) {
            listener.onTabChanged(tabSelected);
        }
        manageTabSelectedUI();
    }

    private void manageTabSelectedUI() {
        manageTabTextColor();
        manageTabBackground();
        manageTabShadow();
    }

    private void manageTabTextColor() {
        txtTab1.setTextColor(tabSelected == tab1value ? selectedTextColor : unselectedTextColor);
        txtTab2.setTextColor(tabSelected == tab2value ? selectedTextColor : unselectedTextColor);
        if (txtTab3 != null) {
            txtTab3.setTextColor(tabSelected == tab3value ? selectedTextColor : unselectedTextColor);
        }
        if (txtTab4 != null) {
            txtTab4.setTextColor(tabSelected == tab4value ? selectedTextColor : unselectedTextColor);
        }
    }

    private void manageTabBackground() {
        txtTab1.setBackground(tabSelected == tab1value ? selectedBackground : unselectedBackground);
        txtTab2.setBackground(tabSelected == tab2value ? selectedBackground : unselectedBackground);
        if (txtTab3 != null) {
            txtTab3.setBackground(tabSelected == tab3value ? selectedBackground : unselectedBackground);
        }
        if (txtTab4 != null) {
            txtTab4.setBackground(tabSelected == tab4value ? selectedBackground : unselectedBackground);
        }
    }

    private void manageTabShadow() {
        tab1shadow.setVisibility(tabSelected == tab1value ? View.GONE : View.VISIBLE);
        tab2shadow.setVisibility(tabSelected == tab2value ? View.GONE : View.VISIBLE);
        if (tab3shadow != null) tab3shadow.setVisibility(tabSelected == tab3value ? View.GONE : View.VISIBLE);
        if (tab4shadow != null) {
            tab4shadow.setVisibility(tabSelected == tab4value ? View.GONE : View.VISIBLE);
        }
    }

    public void setTextTab3(String text) {
        txtTab3.setText(text);
    }

    public void manageIndicatorUI() {
        if (imgIndicator != null) {
            boolean hasNewContent = PrefsManager.getInstance().getHasNewContent(context);
            imgIndicator.setVisibility(hasNewContent ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void clickTab(int tabNo) {
        switch (tabNo) {
            case 1: txtTab1.performClick(); break;
            case 2: txtTab2.performClick(); break;
            case 3: txtTab3.performClick(); break;
            case 4: txtTab4.performClick(); break;
        }
    }

}
