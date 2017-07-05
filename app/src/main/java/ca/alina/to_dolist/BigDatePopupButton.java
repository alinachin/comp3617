package ca.alina.to_dolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Date;

import ca.alina.to_dolist.database.DateHelper;

/** Displays a Date. When clicked, displays a DatePicker dialog to edit the date.
 */

class BigDatePopupButton extends FrameLayout implements DatePickerDialog.OnDateSetListener {

    private LocalDate mDate;
    private ViewHolder viewHolder;

    public BigDatePopupButton(Context context) {
        super(context);
        inflateLayout();
    }

    public BigDatePopupButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigDatePopupButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflateLayout();
    }

    private void inflateLayout() {
        LayoutInflater mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                //Toast.makeText(getContext(), "Date picker here", Toast.LENGTH_SHORT).show();
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
    }

    public void setDate(Date date) {
        mDate = new LocalDate(date);

        // set display fields
        viewHolder.dayOfWeek.setText(mDate.dayOfWeek().getAsText());

        viewHolder.date.setText(DateHelper.formatDayMonth(getContext(), date));

        String relativeDate = DateHelper.formatDateRelativeToNow(mDate);
        if (!relativeDate.isEmpty()) {
            relativeDate = "(" + relativeDate + ")";
        }
        viewHolder.relativeDate.setText(relativeDate);
    }

    public Date getDate() {
        return mDate.toDate();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        setDate(calendar.getTime());
    }


    private static class ViewHolder {
        TextView dayOfWeek;
        TextView date;
        TextView relativeDate;
    }

}
