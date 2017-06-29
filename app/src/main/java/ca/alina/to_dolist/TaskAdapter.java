package ca.alina.to_dolist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

import static ca.alina.to_dolist.R.id.date;

/**
 * Created by Alina on 2017-06-22.
 */

class TaskAdapter extends ArrayAdapter<Task> {
    private DatabaseHelper helper;
    private DateFormat timeFormat;
    private DateFormat dateFormat;

    TaskAdapter(Context context, int resource, List<Task> tasks) {
        super(context, resource, tasks);

        helper = DatabaseHelper.getInstance(context);
        timeFormat = android.text.format.DateFormat.getTimeFormat(context.getApplicationContext());
        dateFormat = android.text.format.DateFormat.getMediumDateFormat(context.getApplicationContext());
    }

    //public TaskAdapter(Context context, int resource, DatabaseHelper helper, DatabaseHelper.Query dataset)

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.list_item_2line, null);

            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) convertView.findViewById(R.id.listItemDate);
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
            Date d = task.getStartTime();

            viewHolder.time.setText(timeFormat.format(d));
            viewHolder.date.setText(dateFormat.format(d));

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

    public void toggleDone(int position, boolean checked) {
        // tell helper to mark done/not done
        Task task = getItem(position);
        helper.toggleDone(task, checked);

        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView date;
        TextView time;
        TextView taskName;
        CheckBox done;
        ImageButton editBtn;
    }
}
