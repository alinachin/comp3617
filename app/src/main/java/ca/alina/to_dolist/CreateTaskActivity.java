package ca.alina.to_dolist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import ca.alina.to_dolist.database.schema.DaoSession;
import ca.alina.to_dolist.database.schema.TaskDao;

public class CreateTaskActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // TODO generate suggested values


        // populate EditorFragment fields w/ values
        // use Bundle? https://stackoverflow.com/a/10798580
    }

    public void cancelButtonPressed(final View view) {
        // do NOT save changes to DB

        setResult(RESULT_CANCELED);
        finish();
    }

    public void saveButtonPressed(final View view) {
        // request values from editor fragment (?)

        // create row in DB & commit

        setResult(RESULT_OK);
        finish();
    }
}
