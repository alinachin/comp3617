package ca.alina.to_dolist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;

import ca.alina.to_dolist.database.schema.Task;

public class CreateTaskActivity extends AbstractEditorActivity {
    public static final String START_DATE_LONG_EXTRA = "startDate";
    private Date suggestedStartTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // get intent extra, if there is one
        Intent intent = getIntent();
        long date = intent.getLongExtra(START_DATE_LONG_EXTRA, -1);
        try {
            suggestedStartTime = DateHelper.autoStartTime(new Date(date));
        }
        catch (Exception e) {
            Log.e("CreateTaskActivity", "no start date passed to activity");
            suggestedStartTime = DateHelper.autoStartTime(); // suggest a time based on today
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_create_task);
    }

    @Override
    protected Task initTask() {
        Task task = new Task();

        task.setName(""); /* can edit */
        task.setStartTime(suggestedStartTime); /* can edit */
        // no need to set end time - editor UI fills it in

        task.setNotes("");
        task.setIsAlarm(false);
        task.setIsDone(false);
        task.setIsRecurring(false);
        task.setIsHidden(false);

        return task;
    }

    @Override
    protected void save() {
        final Task task;
        // grab values from editor fragment
        task = editor.getTask();

        helper.insertTask(task);

        // schedule notification
        NotificationHelper nHelper = new NotificationHelper(this);
        nHelper.scheduleNotification(task);

        setResult(RESULT_OK);
        finish();
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
