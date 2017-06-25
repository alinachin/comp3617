package ca.alina.to_dolist;

import android.content.Context;
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
    private SparseBooleanArray itemsDone;
    // for multi-select
    // private SparseBooleanArray mSelectedItems;

    public TaskAdapter(Context context, int resource, List<Task> tasks) {
        super(context, resource, tasks);
        //mSelectedItems = new SparseBooleanArray();

        helper = DatabaseHelper.getInstance(context);
        itemsDone = new SparseBooleanArray();
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

        // TODO create separate layout files for checked/unchecked list_items?
        if (p != null) {
            // set TextViews etc.
            viewHolder.taskName.setText(p.getName());
            Date d = p.getStartTime();
            viewHolder.time.setText(DateHelper.formatTime(d));
            viewHolder.date.setText(DateHelper.formatDateOneLine(d));

            // set bg as selected/unselected
//            if (mSelectedItems.get(position)) {
//                viewHolder.taskName.setBackgroundColor(Color.RED); // todo use state drawable
//            }

            // set CHECKBOX for done/not done state
            viewHolder.done.setChecked(p.getIsDone());
            viewHolder.done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox me = (CheckBox) v;
                    toggleDone(position, me.isChecked());
                }
            });
            // set other appearance properties for done/not done state

        }


        return convertView;
    }

    public void toggleDone(int position, boolean checked) {
        // tell helper to mark done/not done
        Task task = getItem(position);
        helper.toggleDone(task, checked);

        notifyDataSetChanged();
    }

    // multi-select methods
    // https://www.mindstick.com/Articles/1577/android-delete-multiple-selected-items-in-listview

//    public void toggleSelection(int position) {
//        selectView(position, !mSelectedItems.get(position));
//    }
//
//    // Remove selection after unchecked
//    public void removeSelection() {
//        mSelectedItems = new  SparseBooleanArray();
//        notifyDataSetChanged();
//    }
//
//    // Item checked on selection
//    public void selectView(int position, boolean value) {
//        if (value)
//            mSelectedItems.put(position, value);
//        else
//            mSelectedItems.delete(position);
//
//        notifyDataSetChanged();
//    }
//
//    // Get number of selected items
//    public int getSelectedCount() {
//        return mSelectedItems.size();
//    }
//
//
//    public SparseBooleanArray getSelectedIds() {
//        return mSelectedItems;
//    }


    static class ViewHolder {
        TextView date;
        TextView time;
        TextView taskName;
        CheckBox done;
    }
}
