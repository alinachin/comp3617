package ca.alina.to_dolist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import ca.alina.to_dolist.database.schema.Task;

public class BasicEditor extends Fragment {

    private Task task;
    private View rootView;
    private ViewHolder fields;

    public BasicEditor() {
        // Required empty public constructor
    }

    // do non-graphic initializations here
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // required to save Task object
    }

    // do graphical initializations here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_basic_editor, container, false);

        fields = new ViewHolder();
        fields.bigDate = (BigDatePopupButton) rootView.findViewById(R.id.bigDate);
        fields.name = (EditText) rootView.findViewById(R.id.taskName);
        fields.startTime = (TimeButton) rootView.findViewById(R.id.startTimeCompound);
        fields.endTime = (TimeButton) rootView.findViewById(R.id.endTimeCompound);

        final TimeButton endTime = fields.endTime;
        fields.endTimeSwitch = (CompoundButton) rootView.findViewById(R.id.endTimeSwitch);

        fields.endTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                endTime.setEnabled(isChecked);
            }
        });
        endTime.setAssocCheckable(fields.endTimeSwitch);

        fields.startTime.setLinkedTimeListener(fields.endTime);
        
        return rootView;
    }

    // do final initializations here (e.g. modifying Views)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // if created for the first time, populate fields from task
        // otherwise fields should have been restored by Android
        if (savedInstanceState == null) {
            if (task == null) {
                Toast.makeText(getActivity(), "Could not open task", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }

            populate();
        }
    }

    /** Retrieve values from editor fields and put in Task
     *
     * @return Task with user's changes
     */
    public Task getTask() {
        if (rootView != null) {
            task.setName(getTaskName());
            task.setStartTime(getTaskStartTime());
            task.setEndTime(getTaskEndTime());
        }

        return task;
    }

    private String getTaskName() {
        // todo validate
        return fields.name.getText().toString();
    }

    private Date getTaskStartTime() {
        Date date = task.getStartTime();
        date = DateHelper.changeDate(date, fields.bigDate.getDate());
        date = DateHelper.changeTime(date, fields.startTime.getTime());

        return date;
    }

    private Date getTaskEndTime() {
        if (fields.endTimeSwitch.isChecked()) {
            // use (mandatory) starting time as a reference
            Date startTime = task.getStartTime();
            Date endTime = DateHelper.changeTime(startTime, fields.endTime.getTime());

            // if end time is before start time, it means the next day (confirmation dialog?)
            if (endTime.before(startTime)) {
                endTime = DateHelper.addOneDay(endTime);

                // todo check if plausible?
            }

            return endTime;
        }
        else {
            return null;
        }
    }

    public void setTask(final Task task) {
//        Log.e("BasicEditor", "begin setTask()");
        this.task = task;
        //populate();
    }

    /** Populate editor fields with Task fields */
    protected void populate() {
        // mirror getTask() - populate fields
        if (rootView != null) {
            fields.bigDate.setDate(task.getStartTime());

            fields.name.setText(task.getName());
            fields.startTime.setTime(task.getStartTime());

            if (task.getEndTime() != null) {
                fields.endTime.setTime(task.getEndTime());
                fields.endTime.setEnabled(true);
                fields.endTimeSwitch.setChecked(true);
            }
            else {
                fields.endTime.setEnabled(false);
                fields.endTimeSwitch.setChecked(false);
            }
        }
    }

    private static class ViewHolder {
        EditText name;
        TimeButton startTime;
        TimeButton endTime;
        BigDatePopupButton bigDate;
        CompoundButton endTimeSwitch;
    }

}
