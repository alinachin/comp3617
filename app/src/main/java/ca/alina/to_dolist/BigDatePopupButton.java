package ca.alina.to_dolist;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Displays a Date. When clicked, displays a DatePicker dialog to edit the date.
 */

class BigDatePopupButton extends FrameLayout {
    // DatePickerDialog

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

        Typeface weekFont = Typeface.createFromAsset(
                getContext().getAssets(),
                "font/Roboto-Light.ttf");
        TextView weekField = (TextView) findViewById(R.id.dayOfWeek);
        weekField.setTypeface(weekFont);

        // set focusable needed?
        this.setFocusable(true);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // show DatePicker dialog (eventually as custom fragment)
                Toast.makeText(getContext(), "Date picker here", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
