package ca.alina.to_dolist;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.schema.Task;

/**
 * Created by Alina on 2017-07-24.
 */

public abstract class AbstractEditorActivity extends AppCompatActivity {
    protected DatabaseHelper helper;
    protected BasicEditor editor;
    protected Task task;

    private static final String FRAGMENT_TAG = "Editor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMyContentView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        helper = DatabaseHelper.getInstance(this);

        // initial onCreate
        if (savedInstanceState == null) {
            // initialize task (e.g. from Intent)
            initTask();

            // initialize editor
            addEditor();
        }
        // not first onCreate, e.g. screen was rotated
        else {
            editor = (BasicEditor) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_TAG);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save fragment instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT_TAG, editor);
    }

    protected abstract void setMyContentView();

    protected abstract void initTask();

    protected void addEditor() {
        // set editor
        editor = new BasicEditor();
        editor.setTask(task);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, editor);
        transaction.commit();
    }

    protected abstract void save();
    // todo add validation method
}
