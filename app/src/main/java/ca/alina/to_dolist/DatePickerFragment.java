package ca.alina.to_dolist;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DatePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String ARG_DATE = "date";

    private long mDate;


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
            mDate = getArguments().getLong(ARG_DATE);
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
        // TODO get arguments & convert into year-month-day integers

        return new DatePickerDialog(getActivity(), this, 0, 0, 0);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }
}
