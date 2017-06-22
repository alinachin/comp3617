package ca.alina.to_dolist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.DateHelper;
import ca.alina.to_dolist.database.schema.Task;

public class CreateTaskActivity extends AppCompatActivity {

    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

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
        // request values from editor fragment (?)

        // create row in DB & commit

        // TODO hook up to UI
        // dummy task
        final Task task;
        task = new Task();
        task.setName("shopping");
        task.setNotes("");
        task.setStartTime(DateHelper.now());
        task.setIsAlarm(false);
        task.setIsDone(false);
        task.setIsRecurring(false);
        task.setIsHidden(false);
        helper.insertTask(task);

        setResult(RESULT_OK);
        finish();
    }
}
