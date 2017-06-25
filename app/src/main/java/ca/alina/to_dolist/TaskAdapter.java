package ca.alina.to_dolist;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

/**
 * Created by Alina on 2017-06-22.
 */

public class TaskAdapter extends ArrayAdapter<Task> {
    private DatabaseHelper helper;

    public TaskAdapter(Context context, int resource, List<Task> tasks) {
        super(context, resource, tasks);

        helper = DatabaseHelper.getInstance(context);
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
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Task p = getItem(position);

        if (p != null) {
            // set TextViews etc.
            viewHolder.taskName.setText(p.getName());
            Date d = p.getStartTime();
            viewHolder.time.setText(DateHelper.formatTime(d));
            viewHolder.date.setText(DateHelper.formatDateOneLine(d));

            // set CHECKBOX for done/not done state
            viewHolder.done.setChecked(p.getIsDone());
            viewHolder.done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox me = (CheckBox) v;
                    toggleDone(position, me.isChecked());
                }
            });
            // TODO create separate layout files for checked/unchecked list_items?
            // set other appearance properties for done/not done state
            View bgView = convertView.findViewById(R.id.listItemBg);
            if (viewHolder.done.isChecked()) {
                bgView.setBackgroundResource(R.drawable.list_item_bg_done);
            }
            else {
                bgView.setBackgroundResource(R.drawable.list_item_bg);
            }
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
    }
}
