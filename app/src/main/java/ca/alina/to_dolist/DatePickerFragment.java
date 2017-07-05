package ca.alina.to_dolist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DatePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatePickerFragment extends DialogFragment {
    private static final String ARG_DATE = "date";

    private Calendar mCalendar;

    public DatePickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param date The date to initially display in the date picker.
     * @return A new instance of fragment DatePickerFragment.
     */
    public static DatePickerFragment newInstance(long date) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            long date = getArguments().getLong(ARG_DATE);
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(date);
        }
    }

    // TODO delete xml layout?
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_date_picker, container, false);
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;

        year = mCalendar.get(Calendar.YEAR);
        month = mCalendar.get(Calendar.MONTH);
        day = mCalendar.get(Calendar.DAY_OF_MONTH);

        try {
            DatePickerDialog.OnDateSetListener listener;
            listener = (DatePickerDialog.OnDateSetListener) getActivity().findViewById(R.id.bigDate);

            return new DatePickerDialog(getActivity(), listener, year, month, day);
        }
        catch (Exception e) {
            Log.e("DatePickerFragment", "couldnt attach listener (needs to be R.id.bigDate)");
            e.printStackTrace();
            return null;
        }
    }

}
