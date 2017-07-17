package ca.alina.to_dolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Date;
import java.util.List;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.schema.Task;

public class NotificationPublisher extends BroadcastReceiver {
    public static final String LONG_TASK_ID = "associated task id";
    public static final String BOOL_IS_END_TIME = "task has end time (# of notifs)";

    private static final int smallIcon = R.drawable.ic_check_box_black_24dp;
    private static final long[] vibratePattern = {0, 500};

    private static final int OPEN_APP_ID = 11001;
    private static final int MARK_DONE_ID = 11002;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        long taskId = intent.getLongExtra(LONG_TASK_ID, -1);
        boolean isForEndTime = intent.getBooleanExtra(BOOL_IS_END_TIME, false);
        int notificationId = 0;
        // todo if endtime - generate random id
        // else reuse id

        Notification notification = makeNotif(context, taskId, isForEndTime);
        if (notification != null) {
            notificationManager.notify(notificationId, notification); // todo id
        }
    }

    private Notification makeNotif(Context context, long taskId, boolean isEndTime) {
        Log.wtf("NotificationPublisher", "makeNotif started");
        // todo separate method for end notif style?
        DatabaseHelper databaseHelper;
        final Task task;
        final List<Task> expiredTasks;
        Notification.Builder builder;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifOn = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFS_ENABLED, true);
        String ringtoneString = sharedPref.getString(SettingsActivity.KEY_PREF_RINGTONE, "");
        boolean vibrate = sharedPref.getBoolean(SettingsActivity.KEY_PREF_VIBRATE, false);

        String doneActionText = context.getResources().getString(R.string.notif_action_mark_done);

        if (!notifOn) {
            Log.d("NotificationPublisher", "notifications off");
            return null;
        }

        if (taskId < 0) {
            return null;
        }

        databaseHelper = DatabaseHelper.getInstance(context);

        // get task
        task = databaseHelper.getTask(taskId);
        if (task == null) {
            return null;
        }

        // check whether there are expired tasks waiting
        expiredTasks = databaseHelper.getExpiredTasks();
        if (expiredTasks.isEmpty()) {
            builder = buildSingle(context, task); // just in case this task needs to be shown?
        }
        else if (expiredTasks.size() == 1) {
            builder = buildSingle(context, task);
        }
        else {
            builder = buildSummary(context, task, expiredTasks);
        }

        builder.setSmallIcon(smallIcon);
        if (!ringtoneString.isEmpty()) {
            builder.setSound(Uri.parse(ringtoneString));
        }
        if (vibrate) {
            builder.setVibrate(vibratePattern);
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        // open to default list?
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, OPEN_APP_ID, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        Intent markDoneIntent = new Intent(context, MarkDoneReceiver.class);
        markDoneIntent.setAction(MarkDoneReceiver.ACTION_MARK);
        markDoneIntent.putExtra(MarkDoneReceiver.EXTRA_TASK_ID, taskId);
        markDoneIntent.putExtra(MarkDoneReceiver.EXTRA_NOTIF_ID, 0); // todo
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                context, MARK_DONE_ID, markDoneIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_check_black_32dp, doneActionText, pendingIntent1);
        return builder.build();
    }

    private Notification.Builder buildSingle(Context context, Task task) {
        String name;
        String timeInterval;

        // get task
        name = task.getName();  // todo truncate
        timeInterval = formatTimeRange(context, task);

        return new Notification.Builder(context)
                .setContentTitle(name)
                .setContentText(timeInterval);
    }

    private Notification.Builder buildSummary(Context context, Task titleTask, List<Task> otherTasks) {
        Log.d("NotificationPublisher", "starting buildSummary()");
        String name;
        String timeInterval;
        int nCollapsed;
        int nExpanded;
        Notification.InboxStyle style;

        name = titleTask.getName();  // todo truncate
        timeInterval = formatTimeRange(context, titleTask);
        nCollapsed = otherTasks.size() - 1;  // assume otherTasks already includes titleTask

        Log.d("NotificationPublisher", "titleTask: " + titleTask.getName() + "\notherTasks: " + otherTasks.toString());

        style = new Notification.InboxStyle();
        style.setBigContentTitle(name);

        int i;
        for (i = 0; i < 4 && i < otherTasks.size(); i++) {
            String name2 = otherTasks.get(i).getName();
            if (!name2.equals(name)) {
                style.addLine(name2 + " - " + formatTimeRange(context, otherTasks.get(i)));
            }
        }
        nExpanded = nCollapsed - i + 1;
        Log.d("NotificationPublisher", "nCollapsed: " + String.valueOf(nCollapsed) + " nExpanded: " + String.valueOf(nExpanded));

        if (nExpanded > 0) {
            style.setSummaryText("+" + Integer.toString(nExpanded) + " more");
        }

        return new Notification.Builder(context)
                .setContentTitle(name + " +" + Integer.toString(nCollapsed) + " more")
                .setContentText(timeInterval)
                .setStyle(style);
    }

    private String formatTimeRange(Context context, Task task) {
        int flags = DateUtils.FORMAT_SHOW_TIME;
        Date startTime = task.getStartTime();
        Date endTime = task.getEndTime();
        if (endTime != null) {
            return DateUtils.formatDateRange(context, startTime.getTime(), endTime.getTime(), flags);
        }
        else {
            return DateUtils.formatDateRange(context, startTime.getTime(), startTime.getTime(), flags);
        }
    }
}
