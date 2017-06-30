package ca.alina.to_dolist.database;

import android.content.Context;
import android.util.Log;

import org.greenrobot.greendao.async.AsyncOperation;
import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.async.AsyncSession;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.joda.time.LocalDate;

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

    private static final int LIMIT_SMART_LIST = 20;

    // TODO move to builder class
    private int taskLengthLastUsed;  // initialize to "default task length" from settings

    private DatabaseHelper(final Context context) {
        final DaoMaster daoMaster;

        mHelper = new DaoMaster.DevOpenHelper(context, "tasks.db", null);

        daoMaster = new DaoMaster(mHelper.getReadableDatabase());
        daoSession = daoMaster.newSession();
        // TODO remove
        asyncSession = daoSession.startAsyncSession();
        asyncSession.setListener(this);

        taskDao = daoSession.getTaskDao();

        // debug
        QueryBuilder.LOG_SQL = true;
    }

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

    // TODO remove
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

    public TaskQuery getOneDayList(final LocalDate day) {
        final Date startDate;  // inclusive
        final Date endDate;  // inclusive

        startDate = DateHelper.getBeginningOfDay(day);
        endDate = DateHelper.getEndOfDay(day);

        // build query
        return new TaskQuery(taskDao.queryBuilder()
                .where(TaskDao.Properties.StartTime.between(startDate, endDate))
                .orderAsc(TaskDao.Properties.StartTime)
                .build());
    }

    public TaskQuery getSmartList() {
        final Date startToday;
        final Date endToday;

        startToday = DateHelper.getBeginningOfDay(new LocalDate());
        endToday = DateHelper.getEndOfDay(new LocalDate());

        QueryBuilder<Task> qb = taskDao.queryBuilder();
        WhereCondition notDone = TaskDao.Properties.IsDone.eq(false);
        WhereCondition notHidden = TaskDao.Properties.IsHidden.eq(false);
        WhereCondition expired = TaskDao.Properties.StartTime.lt(startToday);
        WhereCondition isToday = TaskDao.Properties.StartTime.between(startToday, endToday);
        WhereCondition todayOrAfter = TaskDao.Properties.StartTime.ge(startToday);

        qb.whereOr(qb.and(notHidden, expired), todayOrAfter).limit(LIMIT_SMART_LIST);
        qb.orderAsc(TaskDao.Properties.StartTime);

        return new TaskQuery(qb.build());
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

    public Task getTask(long taskId) {
        return taskDao.load(taskId);
    }

    public static class TaskQuery {
        // TODO hold a prebuilt GreenDAO query
        final Query<Task> query;

        TaskQuery(Query<Task> query) {
            this.query = query;
        }

        public List<Task> run() {
            return query.list();
        }
    }
}
