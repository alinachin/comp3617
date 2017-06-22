package ca.alina.to_dolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

/**
 * Created by Alina on 2017-06-22.
 */

public class TaskAdapter extends ArrayAdapter<Task> {
    public TaskAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public TaskAdapter(Context context, int resource, List<Task> tasks) {
        super(context, resource, tasks);
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
            viewHolder.taskName = (TextView) convertView.findViewById(R.id.taskName);
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
        }

        return convertView;
    }

//    @Override
//    public void notifyDataSetChanged() {
//
//    }


    static class ViewHolder {
        TextView date;
        TextView time;
        TextView taskName;
    }
}
