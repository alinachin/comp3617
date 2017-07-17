package ca.alina.to_dolist;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import ca.alina.to_dolist.database.DateHelper;

/**
 * Composite View for inputting a time, either by typing into an EditText or using a TimePickerDialog.
 */

public class TimeButtonEditText extends FrameLayout implements TimePickerDialog.OnTimeSetListener, View.OnClickListener {
    private ViewHolder viewHolder;
    private LocalTime mTime;
    private DateFormat timeFormat;
    private boolean myEnabled;
    private Checkable checkable = null;

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
        viewHolder.editText = (EditText) findViewById(R.id.timeEditText);

//        viewHolder.editText.setClickable(true);
//        viewHolder.editText.setOnClickListener(this);
        viewHolder.button.setOnClickListener(this);
        this.setOnClickListener(this);
    }

    public Date getTime() {
        // TODO validation

        return mTime.toDateTimeToday().toDate();
    }

    public void setTime(Date date) {
        mTime = new LocalTime(date);

        String timeString = timeFormat.format(date);
        viewHolder.editText.setText(timeString);
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
        Log.e("TimeButtonEditText", "I'm clicked");

        // do custom onclick stuff
        if (!myEnabled) {
            setEnabled(true);
            if (checkable != null)
                checkable.setChecked(true);
        }

        // pass click to child views
        if (v == this || v == viewHolder.button) {
            // parse editText (do not throw error)
            // if it has a valid time, set
            long time;

            try {
                Date date = timeFormat.parse(viewHolder.editText.getText().toString());
                time = date.getTime();
            }
            catch (ParseException e) {
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
    }

    private static class ViewHolder {
        EditText editText;
        ImageButton button;
    }
}
