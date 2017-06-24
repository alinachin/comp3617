package ca.alina.to_dolist;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

public class MainActivity extends AppCompatActivity {

    // request codes for activities e.g. CreateTask
    private static final int CREATE_TASK_REQUEST = 1;

    private DatabaseHelper helper;
    //private ListView listView;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO set from which xml file?
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        helper = DatabaseHelper.getInstance(this);
        // TODO testing purposes only
        helper.debugDeleteAll();


        ListView listView = (ListView) findViewById(R.id.smartList);
        adapter = new TaskAdapter(this, R.layout.list_item_2line, helper.debugGetAllTasks());
        listView.setAdapter(adapter);
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Task was created
                Toast.makeText(this, "Refreshing task list", Toast.LENGTH_SHORT).show();

                // read from DB
                refreshView();
            }
        }
    }

    /** Refreshes the list of tasks displayed by this activity.
     *
     */
    protected void refreshView() {
        List<Task> tasks;
        tasks = helper.debugGetAllTasks();  // TODO rerun actual (pre-built) query

//        List<String> taskNames = new ArrayList<String>();
//        for (Task t : tasks) {
//            taskNames.add(t.getName());
//        }
//        Toast.makeText(this, taskNames.toString(), Toast.LENGTH_LONG).show();

        // update adapter
        adapter.clear();
        adapter.addAll(tasks);
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
