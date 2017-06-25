package ca.alina.to_dolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.schema.Task;

public class EditTaskActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private BasicEditor editor;
    private Task task;

    static final String TAG_INTENT = "taskId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        helper = DatabaseHelper.getInstance(this);

        editor = new BasicEditor();
        // set editor

        // tell editor we want to edit existing task
        Bundle bundle = new Bundle();
        bundle.putBoolean(BasicEditor.EXISTING_TASK_KEY, true);
        editor.setArguments(bundle);

        // get task to edit from intent
        Intent callingIntent = getIntent();
        long taskId = callingIntent.getLongExtra(TAG_INTENT, -1L);
        task = helper.getTask(taskId);
        if (task == null) {
            Toast.makeText(this, "Could not open task", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        Log.e("EditTaskActivity", "Editing task: " + task.getName());
        // pass task to editor fragment
        editor.setTask(task);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.edit_task_container, editor, "editTask");
        transaction.commit();
    }

    // onStart - refresh values from database?

    private void save() {
        // grab values from editor fragment
        task = editor.getTask();

        // TODO use builder/factory class
        helper.updateTask(task);

        setResult(RESULT_OK);
        finish();
    }

    private void delete() {
        // should have the task already
        List<Task> t = new ArrayList<>();
        t.add(task);
        helper.deleteSelectedTasks(t);

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            save();
            return true;
        }
        if (id == R.id.action_delete) {
            Log.e("EditTaskActivity", "Delete menubutton pressed");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_confirm_delete)
                    .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            delete();
                        }
                    })
                    .setNegativeButton(R.string.alert_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.create();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
