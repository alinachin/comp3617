package ca.alina.to_dolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Date;

/** Displays a Date. When clicked, displays a DatePicker dialog to edit the date.
 */

public class BigDatePopupButton extends FrameLayout implements DatePickerDialog.OnDateSetListener {
    private static final String STATE_SUPER_KEY = "super";
    private static final String STATE_DATE_LONG_KEY = "mDate";

    private LocalDate mDate;
    private ViewHolder viewHolder;
    private OnBigDateChangedListener listener;

    public BigDatePopupButton(Context context) {
        super(context);
        init();
    }

    public BigDatePopupButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigDatePopupButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.big_date, this, true);

        // fill in viewholder
        viewHolder = new ViewHolder();
        viewHolder.date = (TextView) findViewById(R.id.date);
        viewHolder.dayOfWeek = (TextView) findViewById(R.id.dayOfWeek);
        viewHolder.relativeDate = (TextView) findViewById(R.id.relativeDay);

        Typeface weekFont = Typeface.createFromAsset(
                getContext().getAssets(),
                "font/Roboto-Light.ttf");
        viewHolder.dayOfWeek.setTypeface(weekFont);

        // set click handler
        this.setFocusable(true);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // show DatePicker dialog

                try {
                    Activity parentActivity = (Activity) getContext();
                    FragmentManager fragmentManager = parentActivity.getFragmentManager();

                    DatePickerFragment datePicker = DatePickerFragment.newInstance(
                            mDate.toDate().getTime());
                    datePicker.show(fragmentManager, "datePicker");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // check for listener in parent
        if (getContext() instanceof OnBigDateChangedListener) {
            listener = (OnBigDateChangedListener) getContext();
        }
    }

    // handle saving state manually

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER_KEY, super.onSaveInstanceState());
        bundle.putLong(STATE_DATE_LONG_KEY, mDate.toDateTimeAtStartOfDay().getMillis());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER_KEY));
            long date = bundle.getLong(STATE_DATE_LONG_KEY);
            setDate(new Date(date));
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

    public void setDate(Date date) {
        LocalDate newDate = new LocalDate(date);
        if (!newDate.equals(mDate)) {
            mDate = newDate;

            // set display fields
            viewHolder.dayOfWeek.setText(mDate.dayOfWeek().getAsText());

            viewHolder.date.setText(DateHelper.formatDayMonth(getContext(), date));

            String relativeDate = DateHelper.formatDateRelativeToNow(mDate);
            if (!relativeDate.isEmpty()) {
                relativeDate = "(" + relativeDate + ")";
            }
            viewHolder.relativeDate.setText(relativeDate);
        }
    }

    public Date getDate() {
        return mDate.toDate();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        Date date = calendar.getTime();
        setDate(date);

        // notify listener
        if (listener != null) {
            listener.onBigDateChanged(date);
        }
    }


    private static class ViewHolder {
        TextView dayOfWeek;
        TextView date;
        TextView relativeDate;
    }

    interface OnBigDateChangedListener {
        void onBigDateChanged(Date date);
    }

}
