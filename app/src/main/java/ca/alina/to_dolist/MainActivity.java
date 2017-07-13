package ca.alina.to_dolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.Date;

import ca.alina.to_dolist.database.DateHelper;

public class MainActivity extends AppCompatActivity implements BigDatePopupButton.OnBigDateChangedListener {

    // request codes for activities e.g. CreateTask
    static final int CREATE_TASK_REQUEST = 1;
    static final int EDIT_TASK_REQUEST = 2;

    static final String LIST_TYPE_KEY = "listType";

    private ListView listView;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // use savedInstanceState to save the type of list (smart or day)
        String listType;
        if (savedInstanceState != null) {
            listType = savedInstanceState.getString(LIST_TYPE_KEY, TaskAdapter.SMART_LIST);
        }
        else {
            listType = TaskAdapter.SMART_LIST;
        }
        adapter = new TaskAdapter(MainActivity.this, R.layout.list_item_2line, listType);

        // set BigDate
        BigDatePopupButton bigDate = (BigDatePopupButton) findViewById(R.id.bigDate);
        bigDate.setDate(DateHelper.now());

        listView = (ListView) findViewById(R.id.smartList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox =  adapter.getCheckBoxAt(view);
                checkBox.toggle();
                adapter.toggleDone(position, checkBox.isChecked());
            }
        });


        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int itemCount() { return listView.getCheckedItemCount(); }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                mode.setTitle(Integer.toString(itemCount()));
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        //Log.e("MainActivity", "Delete in multi-select mode pressed");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        String message = getResources().getQuantityString(
                                R.plurals.alert_confirm_multi_delete,
                                itemCount(),
                                itemCount());
                        builder.setMessage(message)
                                .setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        //deleteSelectedItems();
                                        adapter.deleteSelectedItems(listView.getCheckedItemPositions());
                                        mode.finish(); // Action picked, so close the CAB
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST_TYPE_KEY, adapter.getListType());

        super.onSaveInstanceState(outState);
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
                // task was created
                //Toast.makeText(this, "Refreshing task list", Toast.LENGTH_SHORT).show();
                //refreshView();
                adapter.refresh();
            }
        }
        else if (requestCode == EDIT_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                // task may have been edited, deleted etc.
                //Toast.makeText(this, "Refreshing task list", Toast.LENGTH_SHORT).show();
                //refreshView();
                adapter.refresh();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // TODO disable/hide "Backup" item based on settings
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
        if (id == R.id.action_backup) {
            Intent intent = new Intent(this, DropboxWebActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBigDateChanged(Date date) {
        Toast.makeText(this, "BigDate changed", Toast.LENGTH_SHORT).show();

        // TODO change listType (swap out adapter)
    }
}
