package ca.alina.to_dolist;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;

import java.util.Date;

import ca.alina.to_dolist.database.DateHelper;

/** Displays a Date. When clicked, displays a DatePicker dialog to edit the date.
 */

class BigDatePopupButton extends FrameLayout {
    // DatePickerDialog

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
                // show DatePicker dialog (eventually as custom fragment)
                Toast.makeText(getContext(), "Date picker here", Toast.LENGTH_SHORT).show();
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
            viewHolder.relativeDate.setText("(" + relativeDate + ")");
        }
        else {
            viewHolder.relativeDate.setText(relativeDate);
        }
    }

    public Date getDate() {
        return mDate.toDate();
    }


    private static class ViewHolder {
        TextView dayOfWeek;
        TextView date;
        TextView relativeDate;
    }

}
