package com.otcengineering.white_app.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.otcengineering.white_app.R;


/**
 * Created by cenci7
 */

public class RoundedCornersImageView extends androidx.appcompat.widget.AppCompatImageView {

    private static final float DEFAULT_CORNER_RADIUS = 15F;

    private float radius = DEFAULT_CORNER_RADIUS;

    public RoundedCornersImageView(Context context) {
        super(context);
    }

    public RoundedCornersImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornersImageView);
        retrieveAttributes(array);
    }

    public RoundedCornersImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornersImageView);
        retrieveAttributes(array);
    }

    private void retrieveAttributes(TypedArray attributes) {
        //radius = attributes.getFloat(R.styleable.RoundedCornersImageView_cornerRadius, DEFAULT_CORNER_RADIUS);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        radius = radius * metrics.density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}
