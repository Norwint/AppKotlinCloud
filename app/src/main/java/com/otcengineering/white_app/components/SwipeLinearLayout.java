package com.otcengineering.white_app.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class SwipeLinearLayout extends LinearLayout {
    private GestureDetector gestureDetector;
    private OnSwipeListener listener;
    private int lastMotionX;
    private int lastMotionY;

    public interface OnSwipeListener {
        void onSwipeLeft();

        void onSwipeRight();
    }

    public SwipeLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(getContext(), new SwipeListener());
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            lastMotionX = (int) event.getX();
            lastMotionY = (int) event.getY();
        }

        final int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int diffX = Math.abs(x - lastMotionX);
        final int diffY = Math.abs(y - lastMotionY);
        boolean isSwipingSideways = diffX > scaledTouchSlop && diffX > diffY;

        // Start sending all events to our onTouchEvent from this point
        return action == MotionEvent.ACTION_MOVE && isSwipingSideways;

    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (lastMotionX - e2.getX() > SWIPE_MIN_DISTANCE) {
                if (listener != null) {
                    listener.onSwipeLeft();
                    return true;
                }
            } else {
                if (listener != null) {
                    listener.onSwipeRight();
                    return true;
                }
            }

            return false;
        }
    }
}