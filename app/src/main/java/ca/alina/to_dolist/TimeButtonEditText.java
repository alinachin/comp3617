package ca.alina.to_dolist;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
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

public class TimeButtonEditText
        extends FrameLayout
        implements TimePickerDialog.OnTimeSetListener,
        View.OnClickListener {
    private ViewHolder viewHolder;
    private LocalTime mTime;
    private DateFormat timeFormat;
    private boolean myEnabled;
    private Checkable checkable = null;
    private TimeButtonEditText linkedTimeListener = null;
    private int defaultTaskLength;

    public TimeButtonEditText(@NonNull Context context) {
        super(context);
        init();
    }

    public TimeButtonEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeButtonEditText(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
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

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultTaskLength = Integer.parseInt(sharedPrefs.getString(SettingsActivity.KEY_PREF_TASK_LENGTH, "-1"));
    }

    public Date getTime() {
        // TODO parse editText
        return mTime.toDateTimeToday().toDate();
    }

    public void setTime(Date date) {
        if (date == null) {
            Log.w("TimeButtonEditText", "setTime(null)");
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
        //Log.e("TimeButtonEditText", "I'm clicked");

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

    public void setLinkedTimeListener(TimeButtonEditText listener) {
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
