package ca.alina.to_dolist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
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

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.schema.Task;

/**
 * Created by Alina on 2017-06-22.
 */

class TaskAdapter extends ArrayAdapter<Task> implements AsyncOperationListener {
    private DatabaseHelper helper;
    private ListType listType;  // either SMART_LIST or a date
    private DatabaseHelper.TaskQuery query;
    private Formatter timeFormatter;
    private StringBuilder timeFormatterSB;
    private GoToDateListener goToDateListener;

    TaskAdapter(Context context, int resource, ListType listType, GoToDateListener listener) {
        super(context, resource, new ArrayList<Task>());

        goToDateListener = listener;

        timeFormatterSB = new StringBuilder(50);
        timeFormatter = new Formatter(timeFormatterSB, Locale.getDefault());

        helper = DatabaseHelper.getInstance(context);

        setListType(listType);
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
            viewHolder.taskName = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.done = (CheckBox) convertView.findViewById(R.id.listItemDoneCheckBox);
            viewHolder.editBtn = (ImageButton) convertView.findViewById(R.id.listItemEditBtn);
            viewHolder.bg = convertView.findViewById(R.id.listItemBg);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Task task = getItem(position);

        if (task != null) {
            // set TextViews etc.
            viewHolder.taskName.setText(task.getName());

            Date taskStartTime = task.getStartTime();
            Date taskEndTime = task.getEndTime() != null ? task.getEndTime() : taskStartTime;

            String taskTimeRange = DateUtils.formatDateRange(
                    getContext(),
                    timeFormatter,
                    taskStartTime.getTime(),
                    taskEndTime.getTime(),
                    DateUtils.FORMAT_SHOW_TIME).toString();
            timeFormatterSB.setLength(0);  // clear formatter output
            viewHolder.time.setText(taskTimeRange);

            // hide date depending on 1) list type 2) previous item = same day
            if (listType != ListType.SMART) {
                viewHolder.date.setVisibility(View.GONE);
            }
            else if (position > 0 && DateHelper.sameDay(
                    taskStartTime,
                    getItem(position-1).getStartTime()
            )) {
                viewHolder.date.setVisibility(View.GONE);
            }
            else {
                viewHolder.date.setVisibility(View.VISIBLE);
                viewHolder.date.setText(DateHelper.formatOneLineDate(getContext(), taskStartTime));
                viewHolder.date.setTag(taskStartTime);
                // set click handler
                viewHolder.date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (goToDateListener != null) {
                            //Log.e("TaskAdapter", "switching list types");
                            goToDateListener.onJumpToDate((Date) v.getTag());
                        }
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
            // todo pull out?
            if (viewHolder.done.isChecked()) {
                viewHolder.bg.setBackgroundResource(R.drawable.list_item_bg_done);
            }
            else {
                viewHolder.bg.setBackgroundResource(R.drawable.list_item_bg);
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

    ListType getListType() {
        return listType;
    }

    void setListType(ListType listType) {
        //Log.e("TaskAdapter", "new list type: " + listType);
        this.listType = listType;

        // change query & rerun it
        if (listType == ListType.SMART) {
            query = helper.getSmartList();
        }
        else {
            // parse listType into a date
            query = helper.getOneDayList(listType.getDate());
        }

        refresh();
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
            List<Task> tasks = new ArrayList<Task>(positions.size());
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

        try {
            List<Task> tasks = (List<Task>) operation.getResult();
            this.clear();
            this.addAll(tasks);
        }
        catch (ClassCastException e) {
            Log.e("TaskAdapter", "GreenDao AsyncOperation returned wrong result type");
        }
    }


    private static class ViewHolder {
        Button date;
        TextView time;
        TextView taskName;
        CheckBox done;
        ImageButton editBtn;
        View bg;
    }

    public interface GoToDateListener {
        void onJumpToDate(Date date);
    }

    public static class ListType {
        private Date mDate;
        private String mString;

        private static final String SMART_NAME = "smart";
        public static final ListType SMART = new ListType(null, SMART_NAME) {
            public Date getDate() {
                // always return current date
                return new Date();
            }
        };

        protected ListType(Date date, String string) {
            mDate = date;
            mString = string;
        }

        public static ListType fromDate(Date date) {
            LocalDate day = new LocalDate(date);
            String string = day.toString();
            return new ListType(date, string);
        }

        public static ListType fromString(String string) throws IllegalArgumentException {
            if (string.equals(SMART_NAME)) {
                // default behaviour: smartlist
                return SMART;
            }

            try {
                LocalDate day = LocalDate.parse(string);
                Date date = day.toDate();
                return new ListType(date, string);
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Date getDate() {
            return mDate;
        }

        public String getString() {
            return mString;
        }

        public String toString() {
            return mString;
        }
    }
}
