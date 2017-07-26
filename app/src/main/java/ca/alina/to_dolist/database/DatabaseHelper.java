package ca.alina.to_dolist.database;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.greendao.async.AsyncOperationListener;
import org.greenrobot.greendao.async.AsyncSession;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import ca.alina.to_dolist.DateHelper;
import ca.alina.to_dolist.MainActivity;
import ca.alina.to_dolist.database.schema.DaoMaster;
import ca.alina.to_dolist.database.schema.DaoSession;
import ca.alina.to_dolist.database.schema.Notif;
import ca.alina.to_dolist.database.schema.NotifDao;
import ca.alina.to_dolist.database.schema.Task;
import ca.alina.to_dolist.database.schema.TaskDao;


public class DatabaseHelper {
    public static final String DB_NAME = "tasks.db";

    private static DatabaseHelper instance; // singleton

    private DaoSession daoSession;
    private TaskDao taskDao;
    private NotifDao notifDao;

    private static final int LIMIT_SMART_LIST = 20;

    //private int taskLengthLastUsed;  // initialize to "default task length" from settings

    private DatabaseHelper(final Context context) {
        DaoMaster.DevOpenHelper mHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        DaoMaster daoMaster = new DaoMaster(mHelper.getWritableDatabase());

        daoSession = daoMaster.newSession();

        taskDao = daoSession.getTaskDao();
        notifDao = daoSession.getNotifDao();

        // debug
        //QueryBuilder.LOG_SQL = true;
    }

    public synchronized static DatabaseHelper getInstance(final Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
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
            taskDao.update(task);
        }
        catch (Exception e) {  // TODO check
            e.printStackTrace();
        }
    }

    public TaskQuery getOneDayList(String string) {
        final LocalDate day;
        final Date startDate;  // inclusive
        final Date endDate;  // inclusive

        try {
            day = LocalDate.parse(string);
        }
        catch (Exception e) {
            Log.e("DatabaseHelper", "invalid listType argument: \"" + string + "\"");
            return getSmartList();
        }

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

        startToday = DateHelper.getBeginningOfDay(new LocalDate());

        QueryBuilder<Task> qb = taskDao.queryBuilder();
//        WhereCondition notDone = TaskDao.Properties.IsDone.eq(false);
        WhereCondition notHidden = TaskDao.Properties.IsHidden.eq(false);
        WhereCondition expired = TaskDao.Properties.StartTime.lt(startToday);
        WhereCondition todayOrAfter = TaskDao.Properties.StartTime.ge(startToday);

        qb.whereOr(qb.and(notHidden, expired), todayOrAfter).limit(LIMIT_SMART_LIST);
        qb.orderAsc(TaskDao.Properties.StartTime);

        return new TaskQuery(qb.build());
    }

//    public void deleteSelectedTasks(final List<Task> tasks) {
//        //Log.e("DatabaseHelper", "deleting selected tasks");
//        taskDao.deleteInTx(tasks);  // blocking version
//    }

    public void deleteTask(Task task) {
        // delete notif rows
        notifDao.deleteInTx(task.getNotifs());
        taskDao.delete(task);
    }

    public void deleteSelectedTasks(final List<Task> tasks, AsyncOperationListener callback) {
        Log.d("DatabaseHelper", "async deleting selected tasks");
        // cancel notifs
        for (Task t : tasks) {
            notifDao.deleteInTx(t.getNotifs());
        }

        AsyncSession session = daoSession.startAsyncSession();
        session.setListenerMainThread(callback);
        session.deleteInTx(Task.class, tasks);
    }

//    public List<Task> debugGetAllTasks() {
//        return taskDao.loadAll();
//    }
//
//    public void debugDeleteAll() {
//        taskDao.deleteAll();
//    }

    public Task getTask(long taskId) {
        return taskDao.load(taskId);
    }

    public boolean isDatabaseEmpty() {
        return (taskDao.count() == 0);
    }

    // NotificationPublisher uses AsyncTask to run in bg
    public List<Task> getExpiredTasks() {
        final Date now = new Date();

        Query<Task> query = taskDao.queryBuilder()
                .where(TaskDao.Properties.StartTime.le(now), TaskDao.Properties.IsDone.eq(false))
                .orderAsc(TaskDao.Properties.StartTime)
                .build();
        return query.list();
    }



    public int makeStartNotif(Task task) {
        //Log.wtf("DatabaseHelper", "makeStartNotif started");
        Notif startNotif;
        long taskId = task.getId();
        long notifId;

        // see if already exists
        List<Notif> notifList = task.getNotifs();
        if (!notifList.isEmpty()) {
            // reuse
            startNotif = notifList.get(0);
            notifId = startNotif.getId();
            return (int) notifId;
        }
        else {
            startNotif = new Notif();
            startNotif.setTaskId(taskId);

            try {
                notifId = notifDao.insert(startNotif);
                notifList.add(startNotif);

                return (int) notifId;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    public int makeEndNotif(Task task) {
        //Log.wtf("DatabaseHelper", "makeEndNotif started");
        Notif endNotif;
        long taskId = task.getId();
        long notifId;

        if (task.getEndTime() == null) {
            return -1;
        }
        // see if already exists
        List<Notif> notifList = task.getNotifs();
        if (notifList.size() >= 2) {
            endNotif = notifList.get(1);
            notifId = endNotif.getId();
            return (int) notifId;
        }
        else {
            endNotif = new Notif();
            endNotif.setTaskId(taskId);

            try {
                notifId = notifDao.insert(endNotif);
                notifList.add(endNotif);

                return (int) notifId;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    /**
     * Holds a prebuilt GreenDAO query
     */
    public static class TaskQuery {
        private final Query<Task> query;

        TaskQuery(Query<Task> query) {
            this.query = query;
        }

//        public List<Task> run() {
//            return query.list();
//        }

        public void runAsync(AsyncOperationListener callback) {
            AsyncSession session = getInstance(null).daoSession.startAsyncSession();
            session.setListenerMainThread(callback);
            session.queryList(query);
        }
    }

    public static class MarkDoneReceiver extends BroadcastReceiver {
        public static final String ACTION_MARK = "ca.alina.to_dolist.action.MARK";

        public static final String EXTRA_TASK_ID = "ca.alina.to_dolist.extra.TASK_ID";
        public static final String EXTRA_NOTIF_ID = "ca.alina.to_dolist.extra.NOTIF_ID";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("MarkDoneReceiver", "starting mark task as done");
            DatabaseHelper helper = getInstance(context);
            long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
            int notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 0);

            // cancel notification
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notifId);

            // mark task as done
            Task task = helper.getTask(taskId);
            if (task == null) {
                Log.e("MarkDoneReceiver", "param " + String.valueOf(taskId) + ": task not found");
                return;
            }
            helper.toggleDone(task, true);

            // todo notify any running MainActivity to refresh its visible listView items
            Intent refreshIntent = new Intent(MainActivity.REFRESH_ACTION);
            context.sendBroadcast(refreshIntent);
        }
    }
}
