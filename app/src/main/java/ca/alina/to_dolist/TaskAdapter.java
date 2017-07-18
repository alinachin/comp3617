package ca.alina.to_dolist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

/**
 * Created by Alina on 2017-06-22.
 */

class TaskAdapter extends ArrayAdapter<Task> implements AsyncOperationListener {
    static final String SMART_LIST = "smart";
    private DatabaseHelper helper;
    private DateFormat timeFormat;
    private String listType;  // either SMART_LIST or a date
    private DatabaseHelper.TaskQuery query;

    TaskAdapter(Context context, int resource, String listType) {
        super(context, resource, new ArrayList<Task>());

        helper = DatabaseHelper.getInstance(context);
        timeFormat = android.text.format.DateFormat.getTimeFormat(context.getApplicationContext());
        //dateFormat = DateHelper.getOneLineFormat(context);

        this.listType = listType;
        if (listType.equals(SMART_LIST)) {
            query = helper.getSmartList();
        }
        else {
            // parse listType into LocalDate
            try {
                query = helper.getOneDayList(LocalDate.parse(listType));
            }
            catch (IllegalArgumentException e) {
                Log.e("TaskAdapter", "Invalid listType (must be date yyyy-MM-DD)");
                query = helper.getSmartList();
            }
        }

        refresh();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.list_item_2line, null);

            viewHolder = new ViewHolder();
            viewHolder.date = (Button) convertView.findViewById(R.id.listItemDate);
            viewHolder.time = (TextView) convertView.findViewById(R.id.listItemTime);
            viewHolder.taskName = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.done = (CheckBox) convertView.findViewById(R.id.listItemDoneCheckBox);
            viewHolder.editBtn = (ImageButton) convertView.findViewById(R.id.listItemEditBtn);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Task task = getItem(position);

        if (task != null) {
            View bgView = convertView.findViewById(R.id.listItemBg);

            // set TextViews etc.
            viewHolder.taskName.setText(task.getName());

            Date taskStartTime = task.getStartTime();
            viewHolder.time.setText(timeFormat.format(taskStartTime));

            // hide date depending on 1) list type 2) previous item = same day
            if (!listType.equals(SMART_LIST) || (position > 0 && DateHelper.sameDay(
                    taskStartTime,
                    getItem(position-1).getStartTime()
            ))) {
                viewHolder.date.setVisibility(View.GONE);
            }
            else {
                viewHolder.date.setVisibility(View.VISIBLE);
                //viewHolder.date.setText(dateFormat.format(taskStartTime));
                viewHolder.date.setText(DateHelper.formatOneLineDate(getContext(), taskStartTime));
                // set click handler
                viewHolder.date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "Go to list of tasks for [date]", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // set CHECKBOX for done/not done state
            viewHolder.done.setChecked(task.getIsDone());
            viewHolder.done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox me = (CheckBox) v;
                    toggleDone(position, me.isChecked());
                }
            });

            // set other appearance properties for done/not done state
            if (viewHolder.done.isChecked()) {
                bgView.setBackgroundResource(R.drawable.list_item_bg_done);
            }
            else {
                bgView.setBackgroundResource(R.drawable.list_item_bg);
            }

            viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentEdit = new Intent(getContext(), EditTaskActivity.class);
                    intentEdit.putExtra(EditTaskActivity.TAG_INTENT, task.getId());
                    ((Activity) getContext()).startActivityForResult(
                            intentEdit,
                            MainActivity.EDIT_TASK_REQUEST);
                }
            });
        }

        return convertView;
    }

    CheckBox getCheckBoxAt(View itemView) {
        Object tag = itemView.getTag();
        if (tag != null) {
            return ((ViewHolder) tag).done;
        }

        return null;
    }

    String getListType() {
        return listType;
    }

    /** Refreshes the this adapter's contents */
    void refresh() {
        // rerun query
        query.runAsync(this);
    }

    void toggleDone(int position, boolean checked) {
        // tell helper to mark done/not done
        Task task = getItem(position);
        helper.toggleDone(task, checked);

        notifyDataSetChanged();
    }

    void deleteSelectedItems(SparseBooleanArray positions) {
        if (positions != null) {
            List<Task> tasks = new LinkedList<Task>();
            for (int i=0; i<positions.size(); i++) {
                if (positions.valueAt(i)) {
                    Task item = getItem(positions.keyAt(i));
                    tasks.add(item);
                }
            }

            // tell helper to delete these items
            //helper.deleteSelectedTasks(tasks);
            helper.deleteSelectedTasks(tasks, new AsyncOperationListener() {
                @Override
                public void onAsyncOperationCompleted(AsyncOperation operation) {
                    if (operation.isFailed()) {
                        Log.e("TaskAdapter", "GreenDao: deleting selected tasks failed");
                        return;
                    }
                    refresh();
                }
            });
        }
    }

    @Override
    public void onAsyncOperationCompleted(AsyncOperation operation) {
        // get the results of a TaskQuery
        if (operation.isFailed()) {
            Log.e("TaskAdapter", "GreenDao tried to run query - failed");
            // todo show user error message?
            return;
        }
        List<Task> tasks = (List<Task>) operation.getResult();
        this.clear();
        this.addAll(tasks);
    }


    private static class ViewHolder {
        Button date;
        TextView time;
        TextView taskName;
        CheckBox done;
        ImageButton editBtn;
    }
}
