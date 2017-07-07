package ca.alina.to_dolist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Time;
import java.util.Date;

import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

public class BasicEditor extends Fragment {

    private Task task;
    private View rootView;
    private ViewHolder fields;

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
        rootView = inflater.inflate(R.layout.fragment_basic_editor, container, false);

        fields = new ViewHolder();
        fields.bigDate = (BigDatePopupButton) rootView.findViewById(R.id.bigDate);
        fields.name = (EditText) rootView.findViewById(R.id.taskName);
        fields.startTime = (TimeButtonEditText) rootView.findViewById(R.id.startTimeCompound);
        fields.endTime = (TimeButtonEditText) rootView.findViewById(R.id.endTimeCompound);

        final TimeButtonEditText endTime = fields.endTime;
        CompoundButton toggleEndTime = (CompoundButton) rootView.findViewById(R.id.endTimeSwitch);

        toggleEndTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                endTime.setEnabled(isChecked);
            }
        });

        
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
            // todo if switch is on task.setEndTime() otherwise task.setEndTime(null)
        }

        return task;
    }

    private void getTaskName() {
        // todo validate
        task.setName(fields.name.getText().toString());
    }

    private void getTaskStartTime() {
        Date date = task.getStartTime();
        date = DateHelper.changeDate(date, fields.bigDate.getDate());
        date = DateHelper.changeTime(date, fields.startTime.getTime());

        task.setStartTime(date);
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
            fields.bigDate.setDate(task.getStartTime());

            fields.name.setText(task.getName());
            fields.startTime.setTime(task.getStartTime());

            // TODO handle endtime
            // todo set switch depending on task having an endTime
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


    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    private static class ViewHolder {
        EditText name;
        TimeButtonEditText startTime;
        TimeButtonEditText endTime;
        BigDatePopupButton bigDate;
    }

}
