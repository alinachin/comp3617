package ca.alina.to_dolist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link BasicEditor.OnFragmentInteractionListener} interface
// * to handle interaction events.
// */
public class BasicEditor extends Fragment {

    static final String TASK_PID_KEY = "taskPid";
    static final String EXISTING_TASK_KEY = "editorType";

//    private OnFragmentInteractionListener mListener;

    private Task task;
    private View view;

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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_basic_editor, container, false);
        return view;
    }

    public Task getTask() {
        final EditText nameField;

        if (view != null) {
            nameField = (EditText) view.findViewById(R.id.taskName);
            task.setName(nameField.getText().toString());
        }

        return task;
    }

    public void setTask(final Task task) {
        this.task = task;
        populate();
    }

    protected void populate() {
        final EditText nameField;

        // mirror getTask() - populate fields
        if (view != null) {
            nameField = (EditText) view.findViewById(R.id.taskName);
            nameField.setText(task.getName());
        }
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean existingTask;
        if (getArguments() == null) {
            existingTask = false;
        }
        else {
            existingTask = getArguments().getBoolean(EXISTING_TASK_KEY);
        }

        if ((task == null) && (existingTask)) {
            Toast.makeText(getActivity(), "Could not open task", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        if (!existingTask) {
            // TODO use builder class?
            task = new Task();
            task.setName(""); /* can edit */
            task.setStartTime(DateHelper.now()); /* can edit */
            task.setNotes("");
            task.setIsAlarm(false);
            task.setIsDone(false);
            task.setIsRecurring(false);
            task.setIsHidden(false);
        }
        // otherwise assume task was set already

        populate();
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
