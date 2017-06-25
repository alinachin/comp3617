package ca.alina.to_dolist.database;

import android.content.Context;
import android.util.Log;

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.async.AsyncSession;

import java.util.Date;
import java.util.List;

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

    // TODO move to builder class
    private int taskLengthLastUsed;  // initialize to "default task length" from settings

    // TODO public static final queries

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

    // TODO implement
    @Override
    public void onAsyncOperationCompleted(AsyncOperation operation) {
        // check AsyncOperation.isFailed() and/or AsyncOperation.getThrowable()

        // switch
    }

    public void toggleDone(Task task, boolean isChecked) {
        if (isChecked) {
            task.setMarkedDoneTime(null);
            task.setIsDone(true);
        }
        else {  // check
            task.setMarkedDoneTime(DateHelper.now());
            task.setIsDone(false);
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
            taskDao.update(task);  // TODO use asyncSession
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO test: not working?
    public List<Task> getOneDayList(final Date day) {
        final List<Task> results;
        final Date startDate;  // inclusive
        final Date endDate;  // inclusive

        startDate = DateHelper.getBeginningOfDay(day);
        endDate = DateHelper.getEndOfDay(day);

        // build query
        results = taskDao.queryBuilder()
                .where(TaskDao.Properties.StartTime.between(startDate, endDate))
                .orderAsc(TaskDao.Properties.StartTime)
                .list();

        return results;
    }

    public void deleteSelectedTasks(final List<Task> tasks) {
        //Log.e("DatabaseHelper", "deleting selected tasks");
        taskDao.deleteInTx(tasks);  // blocking version
        //asyncSession.deleteInTx(Task.class, tasks);
    }

    // TODO remove
    public List<Task> debugGetAllTasks() {
        return taskDao.loadAll();
    }

    public void debugDeleteAll() {
        taskDao.deleteAll();
    }

    public static class Query {
        // TODO hold a prebuilt GreenDAO query
    }
}
