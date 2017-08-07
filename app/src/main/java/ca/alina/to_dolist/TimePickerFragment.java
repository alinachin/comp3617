package ca.alina.to_dolist;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment {
    private static final String ARG_ID = "id";
    private static final String ARG_TIME = "time";

    private int viewId;
    private Calendar mCalendar;

    /**
     * Required empty constructor.
     */
    public TimePickerFragment() { }

    /**
     * Factory method for creating a fragment with arguments.
     * @param time Initially displayed time, should get by calling getTime() from a Java Date
     * @return a new instance of the fragment
     */
    public static TimePickerFragment newInstance(int id, long time) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putLong(ARG_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            long time = getArguments().getLong(ARG_TIME);

            viewId = getArguments().getInt(ARG_ID);
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(time);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get listener (the compound view)
        try {
            TimePickerDialog.OnTimeSetListener listener;
            listener = (TimePickerDialog.OnTimeSetListener) getActivity().findViewById(viewId);

            return new TimePickerDialog(
                    getActivity(),
                    listener,
                    mCalendar.get(Calendar.HOUR_OF_DAY),
                    mCalendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity())
            );
        }
        catch (Exception e) {
            Crashlytics.logException(e);
//            Log.e("TimePickerFragment", "couldnt attach listener (provided in args bundle)");
//            e.printStackTrace();
            return null;
        }
    }
}
