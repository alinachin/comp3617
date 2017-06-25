package ca.alina.to_dolist;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

public class MainActivity extends AppCompatActivity {

    // request codes for activities e.g. CreateTask
    static final int CREATE_TASK_REQUEST = 1;
    static final int EDIT_TASK_REQUEST = 2;

    private DatabaseHelper helper;
    private ListView listView;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        helper = DatabaseHelper.getInstance(this);
        listView = (ListView) findViewById(R.id.smartList);
        adapter = new TaskAdapter(
                MainActivity.this,
                R.layout.list_item_2line,
                helper.debugGetAllTasks());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox =  ((TaskAdapter.ViewHolder) view.getTag()).done;
                checkBox.toggle();
                adapter.toggleDone(position, checkBox.isChecked());
            }
        });


        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                mode.setTitle(Integer.valueOf(listView.getCheckedItemCount()).toString());
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelectedItems();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });
    }

    /** Opens the task editor.
     * No task is added unless the user explicitly clicks Save. When the task editor is closed,
     * IF a new task was successfully added, this screen should refresh the list of tasks.
     * @param view The View attached to this handler
     */
    public void createTask(final View view) {
        final Intent intent;

        intent = new Intent(this, CreateTaskActivity.class);
        startActivityForResult(intent, CREATE_TASK_REQUEST);
    }

    public void testNotification(final View view) {
        scheduleNotification(getNotification("Task name here"), 3000);
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(content);
        builder.setContentText("3 seconds have passed");
        builder.setSmallIcon(R.drawable.ic_check_box_black_24dp);
        builder.setDefaults(Notification.DEFAULT_ALL);
        return builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                // task was created
                //Toast.makeText(this, "Refreshing task list", Toast.LENGTH_SHORT).show();
                refreshView();
            }
        }
        else if (requestCode == EDIT_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                // task may have been edited, deleted etc.
                refreshView();
            }
        }
    }

    // TODO move into TaskAdapter, make it extend BaseAdapter, pass it the helper instance
    /** Refreshes the list of tasks displayed by this activity. */
    protected void refreshView() {
        List<Task> tasks;
        tasks = helper.debugGetAllTasks();  // TODO rerun actual (pre-built) query

        // update adapter
        adapter.clear();
        adapter.addAll(tasks);
    }

    // TODO move into TaskAdapter
    protected void deleteSelectedItems() {
        //Log.e("MainActivity", "deleting items");
        // get selected items from list
        SparseBooleanArray checkedPositions = listView.getCheckedItemPositions();
        // https://stackoverflow.com/a/6931618
        if (checkedPositions != null) {
            List<Task> tasks = new LinkedList<Task>();
            for (int i=0; i<checkedPositions.size(); i++) {
                if (checkedPositions.valueAt(i)) {
                    Task item = (Task) adapter.getItem(checkedPositions.keyAt(i));
                    tasks.add(item);
                }
            }

            // tell helper to delete these items
            helper.deleteSelectedTasks(tasks);

            // refresh list
            // TODO do this as a callback from deleteSelectedTasks instead
            refreshView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
