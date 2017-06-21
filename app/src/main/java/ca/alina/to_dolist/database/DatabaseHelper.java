package ca.alina.to_dolist.database;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.async.AsyncSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ca.alina.to_dolist.database.schema.DaoMaster;
import ca.alina.to_dolist.database.schema.DaoSession;
import ca.alina.to_dolist.database.schema.Task;
import ca.alina.to_dolist.database.schema.TaskDao;

/**
 * Created by Alina on 2017-06-20.
 */

public class DatabaseHelper implements AsyncOperationListener {

    private static DatabaseHelper instance; // singleton

    private DaoMaster.DevOpenHelper mHelper;
    private DaoSession daoSession;
    private AsyncSession asyncSession;
    private TaskDao taskDao;

    private DatabaseHelper(final Context context) {
        final DaoMaster daoMaster;

        mHelper = new DaoMaster.DevOpenHelper(context, "tasks.db", null);
        daoMaster = new DaoMaster(mHelper.getReadableDatabase());
        daoSession = daoMaster.newSession();
        asyncSession = daoSession.startAsyncSession();
        asyncSession.setListener(this);

        taskDao = daoSession.getTaskDao();
    }

    // TODO doublecheck Singleton pattern
    public synchronized static DatabaseHelper getInstance(final Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    // Unneeded - open DB once for the application scope
//    public void openDatabaseForReading() throws SQLiteException {
//        final DaoMaster daoMaster = new DaoMaster(mHelper.getReadableDatabase());
//        daoSession = daoMaster.newSession();
//        asyncSession = daoSession.startAsyncSession();
//        asyncSession.setListener(this);
//        taskDao = daoSession.getTaskDao();
//    }
//
//    public void openDatabaseForWriting() throws SQLiteException {
//        final DaoMaster daoMaster = new DaoMaster(mHelper.getWritableDatabase());
//        daoSession = daoMaster.newSession();
//        asyncSession = daoSession.startAsyncSession();
//        asyncSession.setListener(this);
//        taskDao = daoSession.getTaskDao();
//    }

    @Override
    public void onAsyncOperationCompleted(AsyncOperation operation) { }

    public void toggleChecked(Task task, boolean isChecked) {
        if (isChecked) {  // uncheck
            task.setMarkedDoneTime(null);
            task.setIsDone(false);
        }
        else {  // check
            task.setMarkedDoneTime(new Date());
            task.setIsDone(true);
        }

        // commit changes
        updateTask(task);
    }

    // TODO make private & use a Builder class
    public void insertTask(Task task) {
        try {
            taskDao.insert(task);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTask(Task task) {
        try {
            taskDao.update(task);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Task> getOneDayTasks(final Date day) {
        final Date startDate;  // inclusive
        final Date endDate;  // exclusive

        startDate = getBeginningOfDay(day);
        endDate = getEndofDay(day);

        // TODO query

        return null;
    }

    // ridiculous helper methods

    private Date getBeginningOfDay(final Date day) {
        final Date result;
        final Calendar calendar;

        calendar = new GregorianCalendar();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        result = calendar.getTime();

        return result;
    }

    private Date getEndofDay(final Date day) {
        final Date result;
        final Calendar calendar;

        calendar = new GregorianCalendar();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        result = calendar.getTime();

        return result;
    }
}
