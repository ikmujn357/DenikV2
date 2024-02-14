package com.usbapps.climbingdiary.custom.customCalendar.date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import androidx.annotation.NonNull;
import android.util.AttributeSet;

import com.usbapps.climbingdiary.R;


public class TextViewWithCircularIndicator extends androidx.appcompat.widget.AppCompatTextView {

    private static final int SELECTED_CIRCLE_ALPHA = 255;

    Paint mCirclePaint = new Paint();

    private int mCircleColor;
    private final String mItemIsSelectedText;

    private boolean mDrawCircle;

    public TextViewWithCircularIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mItemIsSelectedText = context.getResources().getString(R.string.mdtp_item_is_selected);

        init();
    }

    private void init() {
        mCirclePaint.setFakeBoldText(true);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setTextAlign(Align.CENTER);
        mCirclePaint.setStyle(Style.FILL);
        mCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
    }

    public void setAccentColor(int color, boolean darkMode) {
        mCircleColor = color;
        mCirclePaint.setColor(mCircleColor);
        setTextColor(createTextColor(color, darkMode));
    }

    /**
     * Programmatically set the color state list (see mdtp_date_picker_year_selector)
     * @param accentColor pressed state text color
     * @param darkMode current theme mode
     * @return ColorStateList with pressed state
     */
    private ColorStateList createTextColor(int accentColor, boolean darkMode) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed}, // pressed
                new int[]{android.R.attr.state_selected}, // selected
                new int[]{}
        };
        int[] colors = new int[]{
                accentColor,
                Color.WHITE,
                darkMode ? Color.WHITE : Color.BLACK
        };
        return new ColorStateList(states, colors);
    }

    public void drawIndicator(boolean drawCircle) {
        mDrawCircle = drawCircle;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (mDrawCircle) {
            final int width = getWidth();
            final int height = getHeight();
            int radius = Math.min(width, height) / 2;
            canvas.drawCircle((float) width / 2, (float) height / 2, radius, mCirclePaint);
        }
        setSelected(mDrawCircle);
        super.onDraw(canvas);
    }

    @SuppressLint("GetContentDescriptionOverride")
    @Override
    public CharSequence getContentDescription() {
        CharSequence itemText = getText();
        if (mDrawCircle) {
            return String.format(mItemIsSelectedText, itemText);
        } else {
            return itemText;
        }
    }
}
