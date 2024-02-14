package com.example.denikv1.custom.customCalendar.date;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

import com.example.denikv1.R;
import com.example.denikv1.custom.customCalendar.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class DatePickerDialog extends AppCompatDialogFragment implements
        OnClickListener, DatePickerController {

    public enum Version {
        VERSION_1,
        VERSION_2
    }

    public enum ScrollOrientation {
        HORIZONTAL,
        VERTICAL
    }

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_THEME_DARK_CHANGED = "theme_dark_changed";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";
    private static final String KEY_AUTO_DISMISS = "auto_dismiss";
    private static final String KEY_DEFAULT_VIEW = "default_view";
    private static final String KEY_TITLE = "title";
    private static final String KEY_OK_RESID = "ok_resid";
    private static final String KEY_OK_STRING = "ok_string";
    private static final String KEY_OK_COLOR = "ok_color";
    private static final String KEY_CANCEL_RESID = "cancel_resid";
    private static final String KEY_CANCEL_STRING = "cancel_string";
    private static final String KEY_CANCEL_COLOR = "cancel_color";
    private static final String KEY_VERSION = "version";
    private static final String KEY_TIMEZONE = "timezone";
    private static final String KEY_DATERANGELIMITER = "daterangelimiter";
    private static final String KEY_SCROLL_ORIENTATION = "scrollorientation";
    private static final String KEY_LOCALE = "locale";

    private static final int ANIMATION_DURATION = 300;

    private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private static SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM", Locale.getDefault());
    private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());

    private Calendar mCalendar = Utils.trimToMidnight(Calendar.getInstance(getTimeZone()));
    private OnDateSetListener mCallBack;
    private final HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;

    private AccessibleDateAnimator mAnimator;

    private DayPickerGroup mDayPickerView;
    private YearPickerView mYearPickerView;

    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private String mTitle;
    private HashSet<Calendar> highlightedDays = new HashSet<>();
    private boolean mThemeDark = false;
    private boolean mThemeDarkChanged = false;
    private Integer mAccentColor = null;
    private boolean mVibrate = true;
    private boolean mDismissOnPause = false;
    private boolean mAutoDismiss = false;
    private int mDefaultView = MONTH_AND_DAY_VIEW;
    private String mOkString;
    private Integer mOkColor = null;
    private String mCancelString;
    private Integer mCancelColor = null;
    private Version mVersion;
    private ScrollOrientation mScrollOrientation;
    private TimeZone mTimezone;
    private Locale mLocale = Locale.getDefault();
    private DefaultDateRangeLimiter mDefaultLimiter = new DefaultDateRangeLimiter();
    private DateRangeLimiter mDateRangeLimiter = mDefaultLimiter;

    public interface OnDateSetListener {
        void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth);
    }

    protected interface OnDateChangedListener {
        void onDateChanged();
    }

    public DatePickerDialog() {
        // Empty constructor required for dialog fragment.
    }

    public static DatePickerDialog newInstance(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        DatePickerDialog ret = new DatePickerDialog();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth);
        return ret;
    }

    public void initialize(OnDateSetListener callBack, Calendar initialSelection) {
        mCallBack = callBack;
        mCalendar = Utils.trimToMidnight((Calendar) initialSelection.clone());
        mScrollOrientation = null;
        //noinspection deprecation
        setTimeZone(mCalendar.getTimeZone());

        mVersion = Version.VERSION_2;
    }

    public void initialize(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        this.initialize(callBack, cal);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = requireActivity();
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setStyle(AppCompatDialogFragment.STYLE_NO_TITLE, 0);
        mCurrentView = UNINITIALIZED;
        if (savedInstanceState != null) {
            mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
            mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
            mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));
            mDefaultView = savedInstanceState.getInt(KEY_DEFAULT_VIEW);
        }
        SimpleDateFormat VERSION_2_FORMAT = new SimpleDateFormat(DateFormat.getBestDateTimePattern(mLocale, "EEEMMMdd"), mLocale);
        VERSION_2_FORMAT.setTimeZone(getTimeZone());

        // Následně upravit označení dne podle aktuálního výběru a zobrazit ho
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
        outState.putBoolean(KEY_THEME_DARK_CHANGED, mThemeDarkChanged);
        if (mAccentColor != null) outState.putInt(KEY_ACCENT, mAccentColor);
        outState.putBoolean(KEY_VIBRATE, mVibrate);
        outState.putBoolean(KEY_DISMISS, mDismissOnPause);
        outState.putBoolean(KEY_AUTO_DISMISS, mAutoDismiss);
        outState.putInt(KEY_DEFAULT_VIEW, mDefaultView);
        outState.putString(KEY_TITLE, mTitle);
        outState.putString(KEY_OK_STRING, mOkString);
        if (mOkColor != null) outState.putInt(KEY_OK_COLOR, mOkColor);
        outState.putString(KEY_CANCEL_STRING, mCancelString);
        if (mCancelColor != null) outState.putInt(KEY_CANCEL_COLOR, mCancelColor);
        outState.putSerializable(KEY_VERSION, mVersion);
        outState.putSerializable(KEY_SCROLL_ORIENTATION, mScrollOrientation);
        outState.putSerializable(KEY_TIMEZONE, mTimezone);
        outState.putParcelable(KEY_DATERANGELIMITER, mDateRangeLimiter);
        outState.putSerializable(KEY_LOCALE, mLocale);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int listPosition = -1;
        int listPositionOffset = 0;
        int currentView = mDefaultView;
        if (mScrollOrientation == null) {
            mScrollOrientation = mVersion == Version.VERSION_1
                    ? ScrollOrientation.VERTICAL
                    : ScrollOrientation.HORIZONTAL;
        }
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
            //noinspection unchecked
            highlightedDays = (HashSet<Calendar>) savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mThemeDarkChanged = savedInstanceState.getBoolean(KEY_THEME_DARK_CHANGED);
            if (savedInstanceState.containsKey(KEY_ACCENT)) mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
            mAutoDismiss = savedInstanceState.getBoolean(KEY_AUTO_DISMISS);
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mOkString = savedInstanceState.getString(KEY_OK_STRING);
            if (savedInstanceState.containsKey(KEY_OK_COLOR)) mOkColor = savedInstanceState.getInt(KEY_OK_COLOR);
            mCancelString = savedInstanceState.getString(KEY_CANCEL_STRING);
            if (savedInstanceState.containsKey(KEY_CANCEL_COLOR)) mCancelColor = savedInstanceState.getInt(KEY_CANCEL_COLOR);
            mVersion = (Version) savedInstanceState.getSerializable(KEY_VERSION);
            mScrollOrientation = (ScrollOrientation) savedInstanceState.getSerializable(KEY_SCROLL_ORIENTATION);
            mTimezone = (TimeZone) savedInstanceState.getSerializable(KEY_TIMEZONE);
            mDateRangeLimiter = savedInstanceState.getParcelable(KEY_DATERANGELIMITER);

            setLocale((Locale) savedInstanceState.getSerializable(KEY_LOCALE));

            if (mDateRangeLimiter instanceof DefaultDateRangeLimiter) {
                mDefaultLimiter = (DefaultDateRangeLimiter) mDateRangeLimiter;
            } else {
                mDefaultLimiter = new DefaultDateRangeLimiter();
            }
        }

        // Inicializace mCalendar na aktuální datum
        mCalendar = Utils.trimToMidnight(Calendar.getInstance(getTimeZone()));

        mDefaultLimiter.setController(this);

        int viewRes = R.layout.mdtp_date_picker_view_animator_v2;
        View view = inflater.inflate(viewRes, container, false);
        // All options have been set at this point: round the initial selection if necessary
        mCalendar = mDateRangeLimiter.setToNearestDate(mCalendar);

        final Activity activity = requireActivity();
        mDayPickerView = new DayPickerGroup(activity, this);
        mYearPickerView = new YearPickerView(activity, this);


        int bgColorResource = R.color.mdtp_date_picker_view_animator;
        int bgColor = ContextCompat.getColor(activity, bgColorResource);
        view.setBackgroundColor(bgColor);

        // Nastavení pozadí na window_shape
        view.setBackground(ContextCompat.getDrawable(activity, R.drawable.window_shape));

        mAnimator = view.findViewById(R.id.mdtp_animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());
        // TODO: Replace with animation decided upon by the design team.
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        // TODO: Replace with animation decided upon by the design team.
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        /// Aktualizace označení aktuálního dne
        updatePickers();

        // Poslat aktuálně vybraný den ven bez nutnosti stisknout tlačítko OK
        notifyOnDateListener();

        if (listPosition != -1) {
            // Pokud je uložená pozice, obnoví ji
            if (currentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(listPosition);
            } else if (currentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
            }
        }

        return view;
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewGroup viewGroup = (ViewGroup) getView();
        if (viewGroup != null) {
            viewGroup.removeAllViewsInLayout();
            View view = onCreateView(requireActivity().getLayoutInflater(), viewGroup, null);
            viewGroup.addView(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDismissOnPause) dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    private void updateDisplay() {
        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        int flags;

        flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        String fullDateText = DateUtils.formatDateTime(getActivity(), millis, flags);
        Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
    }

    public void setThemeDark(boolean themeDark) {
        mThemeDark = themeDark;
        mThemeDarkChanged = true;
    }

    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    public void setAccentColor(@ColorInt int color) {
        mAccentColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public int getAccentColor() {
        return mAccentColor;
    }

    @Override
    public boolean isHighlighted(int year, int month, int day) {
        Calendar date = Calendar.getInstance(getTimeZone());
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        Utils.trimToMidnight(date);
        return highlightedDays.contains(date);
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    public void setSelectableDays(Calendar[] selectableDays) {
        mDefaultLimiter.setSelectableDays(selectableDays);
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    public void setVersion(Version version) {
        mVersion = version;
    }

    public Version getVersion() {
        return mVersion;
    }

    public ScrollOrientation getScrollOrientation() {
        return mScrollOrientation;
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public void setTimeZone(TimeZone timeZone) {
        mTimezone = timeZone;
        mCalendar.setTimeZone(timeZone);
        YEAR_FORMAT.setTimeZone(timeZone);
        MONTH_FORMAT.setTimeZone(timeZone);
        DAY_FORMAT.setTimeZone(timeZone);
    }

    @SuppressWarnings("WeakerAccess")
    public void setLocale(Locale locale) {
        mLocale = locale;
        mWeekStart = Calendar.getInstance(mTimezone, mLocale).getFirstDayOfWeek();
        YEAR_FORMAT = new SimpleDateFormat("yyyy", locale);
        MONTH_FORMAT = new SimpleDateFormat("MMM", locale);
        DAY_FORMAT = new SimpleDateFormat("dd", locale);
    }

    @Override
    public Locale getLocale() {
        return mLocale;
    }

    private Calendar adjustDayInMonthIfNeeded(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (day > daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
        return mDateRangeLimiter.setToNearestDate(calendar);
    }

    @Override
    public void onClick(View v) {
        notifyOnDateListener();
    }

    @Override
    public void onYearSelected(int year) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar = adjustDayInMonthIfNeeded(mCalendar);
        updatePickers();
        updateDisplay();
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updatePickers();
        updateDisplay();
        notifyOnDateListener();
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }


    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        return new MonthAdapter.CalendarDay(mCalendar, getTimeZone());
    }

    @Override
    public Calendar getStartDate() {
        return mDateRangeLimiter.getStartDate();
    }

    @Override
    public Calendar getEndDate() {
        return mDateRangeLimiter.getEndDate();
    }

    @Override
    public int getMinYear() {
        return mDateRangeLimiter.getMinYear();
    }

    @Override
    public int getMaxYear() {
        return mDateRangeLimiter.getMaxYear();
    }


    @Override
    public boolean isOutOfRange(int year, int month, int day) {
        return mDateRangeLimiter.isOutOfRange(year, month, day);
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void tryVibrate() {
    }

    @Override public TimeZone getTimeZone() {
        return mTimezone == null ? TimeZone.getDefault() : mTimezone;
    }

    public void notifyOnDateListener() {
        if (mCallBack != null) {
            mCallBack.onDateSet(DatePickerDialog.this, mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        }
    }
}
