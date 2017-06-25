package ca.alina.to_dolist;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
}
