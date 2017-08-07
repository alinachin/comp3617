package ca.alina.to_dolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.schema.Task;

/** Helper class for scheduling notifications.
 * Create it within a method, not an Activity (?)
 */
class NotificationHelper {
    private Context mContext;

    NotificationHelper(final Context context) {
        mContext = context;
    }

    void scheduleNotification(Task task) {
//        Log.wtf("NotificationHelper", "scheduleNotification started");
        boolean taskHasEndTime = (task.getEndTime() != null);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        long startTime = task.getStartTime().getTime();
        long endTime = (taskHasEndTime ? task.getEndTime().getTime() : 0L);
        long taskId = task.getId();

        DatabaseHelper helper = DatabaseHelper.getInstance(mContext);

        Intent notificationIntent = new Intent(mContext, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.LONG_TASK_ID, taskId);
        notificationIntent.putExtra(NotificationPublisher.BOOL_IS_END_TIME, false);

        // save to notif database & get requestCode
        int startNotifId = helper.makeStartNotif(task);
        if (startNotifId < 0) {
//            Log.e("NotificationHelper", "error writing to notif db");
            return;
        }

        PendingIntent pendingIntentStartTime = PendingIntent.getBroadcast(
                mContext, startNotifId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntentStartTime);

        if (taskHasEndTime) {
            Intent intent = new Intent(mContext, NotificationPublisher.class);
            intent.putExtra(NotificationPublisher.LONG_TASK_ID, taskId);
            intent.putExtra(NotificationPublisher.BOOL_IS_END_TIME, true);

            // save to notif database & get requestCode
            int endNotifId = helper.makeEndNotif(task);
            if (endNotifId < 0) {
//                Log.e("NotificationHelper", "error writing to notif db");
                return;
            }

            PendingIntent pendingIntentEndTime = PendingIntent.getBroadcast(
                    mContext, endNotifId, intent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            alarmManager.set(AlarmManager.RTC_WAKEUP, endTime, pendingIntentEndTime);
        }
    }

}
