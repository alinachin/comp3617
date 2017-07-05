package ca.alina.to_dolist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

import static ca.alina.to_dolist.R.id.bigDate;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link BasicEditor.OnFragmentInteractionListener} interface
// * to handle interaction events.
// */
public class BasicEditor extends Fragment {

//    static final String TASK_PID_KEY = "taskPid";
//    static final String EXISTING_TASK_KEY = "editorType";

//    private OnFragmentInteractionListener mListener;

    private Task task;
    private View rootView;
    private EditText nameField;
    private EditText startTimeField;
    private DateFormat timeFormat;
    private BigDatePopupButton bigDate;

    public BasicEditor() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.e("BasicEditor", "begin onCreateView()");

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_basic_editor, container, false);

        bigDate = (BigDatePopupButton) rootView.findViewById(R.id.bigDate);
        nameField = (EditText) rootView.findViewById(R.id.taskName);
        startTimeField = (EditText) rootView.findViewById(R.id.startTime);

        // set click handler for startTimeIconButton
        return rootView;
    }

    /** Retrieve values from editor fields and put in Task
     *
     * @return Task with user's changes
     */
    public Task getTask() {
        if (rootView != null) {
            getTaskName();
            getTaskStartTime();
            // if switch is on task.setEndTime() otherwise task.setEndTime(null)
        }

        return task;
    }

    private void getTaskName() {
        // todo validate
        task.setName(nameField.getText().toString());
    }

    private void getTaskStartTime() {
        try {
            Date date = task.getStartTime();
            date = DateHelper.changeDate(date, bigDate.getDate());
            date = DateHelper.changeTime(date,
                    timeFormat.parse(startTimeField.getText().toString()));

            task.setStartTime(date);
        }
        catch (java.text.ParseException e) {
            // todo throw new error?
            Log.e("BasicEditor", "invalid start time in startTimeField");
            task.setStartTime(DateHelper.autoStartTime());
        }
    }

    public void setTask(final Task task) {
//        Log.e("BasicEditor", "begin setTask()");
        this.task = task;
        populate();
    }

    /** Populate editor fields with Task fields */
    protected void populate() {
        // mirror getTask() - populate fields
        if (rootView != null) {
//            Log.e("BasicEditor", "begin populate()");
            bigDate.setDate(task.getStartTime());

            nameField.setText(task.getName());
            startTimeField.setText(timeFormat.format(task.getStartTime()));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (task == null) {
            Toast.makeText(getActivity(), "Could not open task", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        populate();
    }

    public void updateEndTimeFromStartTime(final View view) {
        // when startTime loses focus/gets set from TimePicker,
        // update endTime to be [default time period] after startTime
    }

//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        timeFormat = android.text.format.DateFormat.getTimeFormat(context.getApplicationContext());
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }


//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
