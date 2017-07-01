package ca.alina.to_dolist;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Displays a Date. When clicked, displays a DatePicker dialog to edit the date.
 */

class BigDatePopupButton extends FrameLayout {
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

    }

}
