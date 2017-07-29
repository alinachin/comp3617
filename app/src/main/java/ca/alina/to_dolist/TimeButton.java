package ca.alina.to_dolist;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Composite View for inputting a time, either by typing into an EditText or using a TimePickerDialog.
 */

public class TimeButton
        extends FrameLayout
        implements TimePickerDialog.OnTimeSetListener,
        View.OnClickListener {
    private static final String STATE_SUPER_KEY = "super";
    private static final String STATE_TIME_LONG_KEY = "mTime";
    private static final String STATE_MY_ENABLED_KEY = "mEnabled";

    private ViewHolder viewHolder;
    private LocalTime mTime;
    private DateFormat timeFormat;
    private boolean myEnabled;
    private Checkable checkable = null;
    private TimeButton linkedTimeListener = null;
    private int defaultTaskLength;

    public TimeButton(@NonNull Context context) {
        super(context);
        init();
    }

    public TimeButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeButton(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.time_button_edittext, this, true);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        myEnabled = true;

        timeFormat = android.text.format.DateFormat.getTimeFormat(getContext().getApplicationContext());

        viewHolder = new ViewHolder();
        viewHolder.button = (ImageButton) findViewById(R.id.timeIconButton);
        viewHolder.editText = (TextView) findViewById(R.id.timeEditText);

        viewHolder.editText.setClickable(true);
        viewHolder.editText.setOnClickListener(this);

        viewHolder.button.setOnClickListener(this);

        this.setOnClickListener(this);

        if (!isInEditMode()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            defaultTaskLength = Integer.parseInt(sharedPrefs.getString(SettingsActivity.KEY_PREF_TASK_LENGTH, "-1"));
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER_KEY, super.onSaveInstanceState());
        bundle.putLong(STATE_TIME_LONG_KEY, mTime.toDateTimeToday().getMillis());
        bundle.putBoolean(STATE_MY_ENABLED_KEY, myEnabled);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER_KEY));

            long time = bundle.getLong(STATE_TIME_LONG_KEY);
            setTime(new Date(time));

            boolean enabled = bundle.getBoolean(STATE_MY_ENABLED_KEY, true);
            setEnabled(enabled);
        }
        else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    public Date getTime() {
        return mTime.toDateTimeToday().toDate();
    }

    public void setTime(Date date) {
        if (date == null) {
            Log.w("TimeButton", "setTime(null)");
            return;
        }

        mTime = new LocalTime(date);

        String timeString = timeFormat.format(date);
        viewHolder.editText.setText(timeString);

        if (linkedTimeListener != null) {
            linkedTimeListener.onLinkedTimeSet(date);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        myEnabled = enabled;
        //viewHolder.button.setEnabled(enabled);
        viewHolder.button.setImageAlpha(enabled ? 255 : 66);  // fake enabling/disabling the button
        viewHolder.editText.setEnabled(enabled);
    }

    public void setAssocCheckable(Checkable view) {
        checkable = view;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        setTime(calendar.getTime());
    }

    /**
     * Modify child views' onclick events
     * @param v The view that received the click event
     */
    @Override
    public void onClick(View v) {
        //Log.e("TimeButton", "I'm clicked");

        // do custom onclick stuff
        if (!myEnabled) {
            setEnabled(true);
            if (checkable != null)
                checkable.setChecked(true);
        }

        // pass click to child views

        // parse editText (do not throw error)
        // if it has a valid time, set
        long time;

//        try {
//            Date date = timeFormat.parse(viewHolder.editText.getText().toString());
//            time = date.getTime();
//        }
//        catch (ParseException e) {
//            time = DateHelper.now().getTime();
//        }
        try {
            time = new DateTime().withTime(mTime).getMillis();
        }
        catch (Exception e) {
            time = DateHelper.now().getTime();
        }

        // show TimePicker dialog
        try {
            Activity parentActivity = (Activity) getContext();
            FragmentManager manager = parentActivity.getFragmentManager();

            TimePickerFragment dialog = TimePickerFragment.newInstance(
                    getId(),
                    time
            );
            dialog.show(manager, "timePicker");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setLinkedTimeListener(TimeButton listener) {
        linkedTimeListener = listener;
    }

    public void onLinkedTimeSet(Date time) {
        setTime(DateHelper.autoEndTime(time, defaultTaskLength));
    }

    private static class ViewHolder {
        TextView editText;
        ImageButton button;
    }

}
