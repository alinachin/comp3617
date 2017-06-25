package ca.alina.to_dolist;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

/**
 * Created by Alina on 2017-06-22.
 */

public class TaskAdapter extends ArrayAdapter<Task> {
    // for multi-select
    // private SparseBooleanArray mSelectedItems;

    public TaskAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        //mSelectedItems = new SparseBooleanArray();
    }

    public TaskAdapter(Context context, int resource, List<Task> tasks) {
        super(context, resource, tasks);
        //mSelectedItems = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.list_item_2line, null);

            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) convertView.findViewById(R.id.dateDisplay);
            viewHolder.time = (TextView) convertView.findViewById(R.id.timeDisplay);
            viewHolder.taskName = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Task p = getItem(position);

        // TODO create separate layout files for checked/unchecked list_items?
        if (p != null) {
            // set TextViews etc.
            Date d = p.getStartTime();
            viewHolder.taskName.setText(p.getName());
            viewHolder.time.setText(DateHelper.formatTime(d));
            viewHolder.date.setText(DateHelper.formatDateOneLine(d));

            // set bg as selected/unselected
//            if (mSelectedItems.get(position)) {
//                viewHolder.taskName.setBackgroundColor(Color.RED); // todo use state drawable
//            }

            // todo set CHECKBOX for done/not done state
        }


        return convertView;
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
    }
}
