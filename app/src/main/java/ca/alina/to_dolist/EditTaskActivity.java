package ca.alina.to_dolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Date;

public class EditTaskActivity extends AbstractEditorActivity {
    static final String TAG_INTENT = "taskId";

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_edit_task);
    }

    @Override
    protected void setTask() {
        // get task to edit from intent
        Intent callingIntent = getIntent();
        long taskId = callingIntent.getLongExtra(TAG_INTENT, -1L);
        task = helper.getTask(taskId);
        if (task == null) {
            Toast.makeText(this, "Could not open task", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
//      Log.e("EditTaskActivity", "Editing task: " + task.getName());
    }

    // onStart - refresh values from database?

    protected void save() {
        // save old start & end times
        Date startTime, endTime;
        startTime = task.getStartTime();
        endTime = task.getEndTime();

        // grab values from editor fragment
        task = editor.getTask();

        helper.updateTask(task);

        // check if start date & end date changed
        if (startTime != task.getStartTime() || endTime != task.getEndTime()) {
            NotificationHelper nHelper = new NotificationHelper(this);
            nHelper.scheduleNotification(task);
        }

        setResult(RESULT_OK);
        finish();
    }

    protected void delete() {
        // should have the task already
        helper.deleteTask(task);

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
            //Log.e("EditTaskActivity", "Delete menubutton pressed");
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
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
