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

import ca.alina.to_dolist.database.schema.Task;


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
        nameField = (EditText) rootView.findViewById(R.id.taskName);

        // set click handler for startTimeIconButton
        return rootView;
    }

    /** Retrieve values from editor fields and put in Task
     *
     * @return Task with user's changes
     */
    public Task getTask() {
        if (rootView != null) {
            task.setName(nameField.getText().toString());
        }

        return task;
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

            nameField.setText(task.getName());
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

    private boolean validate() {
        // nameField must not be empty


        return true;
    }

//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
