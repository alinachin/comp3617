package ca.alina.to_dolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.Date;

import ca.alina.to_dolist.database.DatabaseHelper;
import io.fabric.sdk.android.Fabric;

import static ca.alina.to_dolist.DropboxWebActivity.DROPBOX_PREF_FILE;
import static ca.alina.to_dolist.DropboxWebActivity.PREF_SESSION_KEY;

public class MainActivity
        extends AppCompatActivity
        implements BigDatePopupButton.OnBigDateChangedListener,
        TaskAdapter.GoToDateListener {

    // request codes for activities e.g. CreateTask
    static final int CREATE_TASK_REQUEST = 1;
    static final int EDIT_TASK_REQUEST = 2;
    static final int BACKUP_WEB_LOGIN_REQUEST = 3;
    static final int RESTORE_WEB_LOGIN_REQUEST = 4;

    static final String LIST_TYPE_KEY = "listType";

    // action for IntentFilter for external receiver to refresh list
    public static final String REFRESH_ACTION = "refresh";

    private ListView listView;
    private TaskAdapter adapter;
    private BigDatePopupButton bigDate;

    private BroadcastReceiver refresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TaskAdapter.ListType listType;

        super.onCreate(savedInstanceState);

//        // Set up Crashlytics, disabled for debug builds
//        Crashlytics crashlyticsKit = new Crashlytics.Builder()
//                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
//                .build();
//
//        // Initialize Fabric with the debug-disabled crashlytics.
//        Fabric.with(this, crashlyticsKit);

        Crashlytics crashlyticsKit = new Crashlytics();
        Fabric.with(this, crashlyticsKit);

        // use savedInstanceState to get/save the type of list (smart or day)
        if (savedInstanceState != null) {
            //Log.e("MainActivity", "onCreate(): restoring from saved state");
            String string = savedInstanceState.getString(LIST_TYPE_KEY, "");
            try {
                listType = TaskAdapter.ListType.fromString(string);
            }
            catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e("MainActivity", "couldn't retrieve saved list config from activity state");
                }
                Crashlytics.logException(e);
                listType = TaskAdapter.ListType.SMART;
            }
        }
        else {
            listType = TaskAdapter.ListType.SMART;
        }
        //Log.e("MainActivity", "onCreate(): listType: " + listType.toString());

        // initialize preferences to default values if needed
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        // check for which initial setup tasks still need to be done
        StateMachine stateMachine = new StateMachine(this);
        int resultState = stateMachine.run();
        if (resultState != StateMachine.READY) {
            if (resultState == StateMachine.DB_RESTORE_NEEDED) {
                // perform db restore based on saved DB method
                Log.e("MainActivity", "performing database restore");
                if (stateMachine.getBackupType() == StateMachine.BACKUP_DROPBOX)
                    actionDownloadDb();
            }
        }

        // display-drawing stuff
        setContentView(R.layout.activity_main);
        setupToolbar();

        initContents(listType);
    }

    @Override
    protected void onPause() {
        // unregister local broadcastreceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refresher);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // register local broadcastreceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(refresher,
                new IntentFilter(REFRESH_ACTION));
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String listType = adapter.getListType().getString();
        //Log.e("MainActivity", "onSaveInstanceState(): listType: " + listType);
        outState.putString(LIST_TYPE_KEY, listType);

        super.onSaveInstanceState(outState);
    }

    /** Initialize the Support Action Bar
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    /** Initialize displayed contents: the task list and date display.
     *
     * @param listType The type of list to display
     */
    private void initContents(TaskAdapter.ListType listType) {
        adapter = new TaskAdapter(this, R.layout.list_item_2line, listType, this);

        // set BigDate
        bigDate = (BigDatePopupButton) findViewById(R.id.bigDate);
        if (listType == TaskAdapter.ListType.SMART) {
            bigDate.setDate(listType.getDate());
        }

        listView = (ListView) findViewById(R.id.smartList);
        // TODO add empty view to content_main.xml
        // link to download from backup?
        //listView.setEmptyView();
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

        refresher = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.e("MainActivity", "onReceive() - refresh visible items");
                adapter.refresh();
            }
        };
    }

    /** Opens the task editor.
     * No task is added unless the user explicitly clicks Save. When the task editor is closed,
     * IF a new task was successfully added, this screen should refresh the list of tasks.
     * @param view The View attached to this handler
     */
    public void createTask(final View view) {
        final Intent intent;

        intent = new Intent(this, CreateTaskActivity.class);
        // put the current list type (date) to use as suggested starting date
        Date startDate = adapter.getListType().getDate();
        intent.putExtra(CreateTaskActivity.START_DATE_LONG_EXTRA, startDate.getTime());
        startActivityForResult(intent, CREATE_TASK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_TASK_REQUEST || requestCode == EDIT_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                // task was created or edited/deleted
                adapter.refresh();
            }
        }
        else if (requestCode == BACKUP_WEB_LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                // login succeeded
                uploadBackup();
            }
        }
        else if (requestCode == RESTORE_WEB_LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                downloadBackup();
            }
        }
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
        if (id == R.id.action_backup) {
            actionBackup();
            return true;
        }
        if (id == R.id.action_smart_list) {
            bigDate.setDate(DateHelper.now());
            if (adapter.getListType() == TaskAdapter.ListType.SMART) {
                // jump to today
                adapter.setListType(TaskAdapter.ListType.fromDate(DateHelper.now()));
            }
            else {
                // jump to smart list
                adapter.setListType(TaskAdapter.ListType.SMART);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBigDateChanged(Date date) {
        //Log.e("MainActivity", "changing lists from BigDate");
        adapter.setListType(TaskAdapter.ListType.fromDate(date));
    }

    @Override
    public void onJumpToDate(Date date) {
        //Log.e("MainActivity", "changing lists from date-heading in smart list");
        bigDate.setDate(date);
        adapter.setListType(TaskAdapter.ListType.fromDate(date));
    }

    private void actionBackup() {
        String token = getSharedPreferences(DROPBOX_PREF_FILE, MODE_PRIVATE)
                .getString(PREF_SESSION_KEY, "");
        if (token.isEmpty()) {
            Intent intent = new Intent(this, DropboxWebActivity.class);
            startActivityForResult(intent, BACKUP_WEB_LOGIN_REQUEST);
        }
        else {
            uploadBackup();
        }
    }

    private void actionDownloadDb() {
        // assume this is on reinstall, we don't have a token
        Intent intent = new Intent(this, DropboxWebActivity.class);
        startActivityForResult(intent, RESTORE_WEB_LOGIN_REQUEST);
    }

    private void uploadBackup() {
        String filePath = "/" + DropboxWebActivity.DROPBOX_FILENAME;

        File dbFileHandle = getDatabasePath(DatabaseHelper.DB_NAME);

        String token = getSharedPreferences(DROPBOX_PREF_FILE, MODE_PRIVATE)
                .getString(PREF_SESSION_KEY, "");
        //Log.e("MainActivity", "token " + token);

        JsonObject obj = new JsonObject();
        obj.addProperty("path", filePath);
        obj.addProperty("mode", "overwrite");
        obj.addProperty("mute", true);
        String paramString = new Gson().toJson(obj);
        //Log.e("MainActivity", paramString);

        // add progress bar (hook up to Ion)
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(ProgressBar.VISIBLE);

        Ion.with(this)
                .load(DropboxWebActivity.API_UPLOAD)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Dropbox-API-Arg", paramString)
                .progressBar(pb)
                .setFileBody(dbFileHandle)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (e != null) {
                            if (BuildConfig.DEBUG) {
                                Log.e("MainActivity", e.getMessage());
                                e.printStackTrace();
                            }

                            if (e instanceof UnknownHostException) {
                                Toast.makeText(MainActivity.this, "Backup failed - no Internet connection", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Backup failed", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            if (result.getHeaders().code() != 200) {
                                if (BuildConfig.DEBUG) {
                                    Log.e("MainActivity",
                                            Integer.toString(result.getHeaders().code())
                                                    + " " + result.getHeaders().message());
                                    if (result.getResult() != null) {
                                        Log.e("MainActivity", result.getResult().toString());
                                    }
                                }

                                if (result.getHeaders().code() == 401) {
                                    Intent intent = new Intent(MainActivity.this, DropboxWebActivity.class);
                                    startActivityForResult(intent, BACKUP_WEB_LOGIN_REQUEST);
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Backup failed - Dropbox issue", Toast.LENGTH_LONG).show();
                                }
                            }
                            else if (result.getResult() != null) {
//                                Log.d("MainActivity", result.getResult().toString());

                                // do something w/ success result?
                                if (result.getResult().get("error") == null) {
                                    Toast.makeText(MainActivity.this, "Backup successful", Toast.LENGTH_LONG).show();

                                    // store info about backup in SharedPrefs
                                    new StateMachine(MainActivity.this).setBackupType(StateMachine.BACKUP_DROPBOX, new Date());
                                }
                            }
                        }

                        // hide progress bar
                        pb.setVisibility(ProgressBar.GONE);
                    }
                });
    }

    private void downloadBackup() {
        String filePath = "/" + DropboxWebActivity.DROPBOX_FILENAME;

        final File dbFileHandle = getDatabasePath(DatabaseHelper.DB_NAME);
        final File cacheHandle = new File(getCacheDir(), DatabaseHelper.DB_NAME);

        String token = getSharedPreferences(DROPBOX_PREF_FILE, MODE_PRIVATE)
                .getString(PREF_SESSION_KEY, "");

        JsonObject obj = new JsonObject();
        obj.addProperty("path", filePath);
        String paramString = new Gson().toJson(obj);
//        Log.e("MainActivity", paramString);

        // disable the FAB until backup is done (or failed)
        final ImageButton fab = (ImageButton) findViewById(R.id.fab);
        fab.setEnabled(false);

        // add progress bar (hook up to Ion)
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(ProgressBar.VISIBLE);

        Ion.with(this)
                .load(DropboxWebActivity.API_DOWNLOAD)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Dropbox-API-Arg", paramString)
                .progressBar(pb)
                .write(cacheHandle)
                .withResponse()
                .setCallback(new FutureCallback<Response<File>>() {
                    @Override
                    public void onCompleted(Exception e, Response<File> result) {
                        if (e != null) {
                            if (BuildConfig.DEBUG) {
                                Log.e("MainActivity", e.getMessage());
                                e.printStackTrace();
                            }

                            if (e instanceof UnknownHostException) {
                                Toast.makeText(MainActivity.this, "Sync failed - no Internet connection", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Sync failed", Toast.LENGTH_LONG).show();
                            }
                        }
                        else {
                            if (result.getHeaders().code() != 200) {
                                if (BuildConfig.DEBUG) {
                                    Log.e("MainActivity",
                                            Integer.toString(result.getHeaders().code())
                                                    + " " + result.getHeaders().message());
                                }

                                if (result.getHeaders().code() == 401) {
                                    Intent intent = new Intent(MainActivity.this, DropboxWebActivity.class);
                                    startActivityForResult(intent, RESTORE_WEB_LOGIN_REQUEST);
                                }
                                else if (result.getHeaders().code() == 409) {
                                    Toast.makeText(MainActivity.this, "Backup not found", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Sync failed - Dropbox issue", Toast.LENGTH_LONG).show();
                                }
                            }
                            else if (result.getResult() != null) {
                                String resultHeaders = result.getHeaders().getHeaders().get("Dropbox-API-Result");
                                JsonObject resultJson = new JsonParser().parse(resultHeaders).getAsJsonObject();

                                if (resultJson.get("error") == null) {
//                                    Log.d("MainActivity", "download successful");
                                    try {
                                        overwriteDb(cacheHandle, dbFileHandle);
                                        adapter.refresh();
                                        Toast.makeText(MainActivity.this, "Sync successful", Toast.LENGTH_LONG).show();
                                    }
                                    catch (IOException exc) {
                                        if (BuildConfig.DEBUG) {
                                            Log.e("MainActivity", "failed to copy downloaded backup to db folder");
                                            exc.printStackTrace();
                                        }

                                        Crashlytics.logException(exc);
                                        Toast.makeText(MainActivity.this, "Sync failed", Toast.LENGTH_LONG).show();
                                    }
                                }
//                                else {
//                                    String err = resultJson.get("error").getAsString();
//                                    Toast.makeText(MainActivity.this, "Sync failed - " + err, Toast.LENGTH_LONG).show();
//                                }
                            }
                        }

                        // hide progress bar again
                        pb.setVisibility(ProgressBar.GONE);

                        // re-enable floating action button
                        fab.setEnabled(true);
                    }
                });

    }

    // copy cached file to actual db
    private void overwriteDb(File source, File target) throws IOException {
        // check that target (actual DB) is empty!
        if (!DatabaseHelper.getInstance(this).isDatabaseEmpty()) {
//            Log.w("MainActivity", "trying to overwrite db from backup while there's something in it. Cancelling");
            // delete cached file (source)
            boolean cacheDeleted = source.delete();
//            if (!cacheDeleted) {
//                Log.d("MainActivity", "failed to delete cached downloaded db, let Android clean it");
//            }

            return;
        }

        FileInputStream inStream = new FileInputStream(source);
        try {
            FileOutputStream outStream = new FileOutputStream(target);
            try {
                FileChannel inChannel = inStream.getChannel();
                FileChannel outChannel = outStream.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            catch (IOException e) {
                throw new IOException("Copy failed", e);
            }
            finally {
                outStream.close();
            }
        }
        catch (IOException e) {
            throw new IOException("Copy failed", e);
        }
        finally {
            try {
                inStream.close();
            }
            catch (Exception e) {
                // we don't care
            }
        }

        // confirm copy succeeded
        if (target.length() != source.length()) {
            throw new IOException("Copy failed");
        }

        // delete cached file (source)
        boolean cacheDeleted = source.delete();
//        if (!cacheDeleted) {
//            Log.d("MainActivity", "failed to delete cached downloaded db, let Android clean it");
//        }
    }

}
