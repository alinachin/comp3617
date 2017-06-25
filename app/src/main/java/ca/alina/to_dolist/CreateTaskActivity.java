package ca.alina.to_dolist;

import android.content.Intent;
import android.net.Uri;
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

public class CreateTaskActivity extends AppCompatActivity implements BasicEditor.OnFragmentInteractionListener {

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

        editor = (BasicEditor) getSupportFragmentManager().findFragmentById(R.id.create_task_editor);
        // TODO generate suggested values


        // populate EditorFragment fields w/ values
        // use Bundle? https://stackoverflow.com/a/10798580

        helper = DatabaseHelper.getInstance(this);
    }

    public void cancelButtonPressed(final View view) {
        // do NOT save changes to DB
        setResult(RESULT_CANCELED);
        finish();
    }

    public void saveButtonPressed(final View view) {
        save();
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO
    }

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
