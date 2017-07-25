package ca.alina.to_dolist;

import android.view.Menu;
import android.view.MenuItem;

import ca.alina.to_dolist.database.schema.Task;

public class CreateTaskActivity extends AbstractEditorActivity {

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_create_task);
    }

    @Override
    protected void setTask() {
        task = new Task();

        task.setName(""); /* can edit */
        task.setStartTime(DateHelper.autoStartTime()); /* can edit */
        // no need to set end time - editor UI fills it in

        task.setNotes("");
        task.setIsAlarm(false);
        task.setIsDone(false);
        task.setIsRecurring(false);
        task.setIsHidden(false);
    }

    @Override
    protected void save() {
        final Task task;
        // grab values from editor fragment
        task = editor.getTask();

        helper.insertTask(task);

        // use helper class
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
