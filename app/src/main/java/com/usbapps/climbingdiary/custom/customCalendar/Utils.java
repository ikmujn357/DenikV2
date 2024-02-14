package com.usbapps.climbingdiary.denikv1.custom.customCalendar;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

import java.util.Calendar;

@SuppressWarnings("WeakerAccess")
public class Utils {

    public static void tryAccessibilityAnnounce(View view, CharSequence text) {
        if (view != null && text != null) {
            view.announceForAccessibility(text);
        }
    }

    public static int dpToPx(float dp, Resources resources){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static boolean isDarkTheme(boolean current) {
        return current;
    }

    public static Calendar trimToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
