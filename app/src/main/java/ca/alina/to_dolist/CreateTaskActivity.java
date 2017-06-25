package ca.alina.to_dolist;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

public class CreateTaskActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private BasicEditor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editor = new BasicEditor();
        // set editor

        // tell editor we want to edit existing task
        Bundle bundle = new Bundle();
        bundle.putBoolean(BasicEditor.EXISTING_TASK_KEY, false);
        editor.setArguments(bundle);

        // TODO generate suggested values - use builder class?
        Task task = new Task();
        task.setName(""); /* can edit */
        task.setStartTime(DateHelper.now()); /* can edit */
        task.setNotes("");
        task.setIsAlarm(false);
        task.setIsDone(false);
        task.setIsRecurring(false);
        task.setIsHidden(false);

        editor.setTask(task);

        helper = DatabaseHelper.getInstance(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.create_task_container, editor, "createTask");
        transaction.commit();
    }

    private void save() {
        final Task task;
        // grab values from editor fragment
        task = editor.getTask();

        // TODO use builder/factory class
        helper.insertTask(task);

        setResult(RESULT_OK);
        finish();
    }

//    @Override
//    public void onFragmentInteraction(Uri uri) {
//        // TODO
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
