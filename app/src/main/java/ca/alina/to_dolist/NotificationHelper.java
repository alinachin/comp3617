package ca.alina.to_dolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
        Log.wtf("NotificationHelper", "scheduleNotification started");
        boolean taskHasEndTime = (task.getEndTime() != null);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        long startTime = task.getStartTime().getTime();
        long endTime = (taskHasEndTime ? task.getEndTime().getTime() : 0L);
        long taskId = task.getId();

        DatabaseHelper helper = DatabaseHelper.getInstance(mContext);

//        Params params = new Params();
//        params.setName(task.getName());
//        params.setId(task.getId());
//        params.setStartTime(startTime);
//        params.setEndTime(endTime);

        Intent notificationIntent = new Intent(mContext, NotificationPublisher.class);
//        notificationIntent.putExtra(NotificationPublisher.PARCEL_TASK_PARAMS, params);
        notificationIntent.putExtra(NotificationPublisher.LONG_TASK_ID, taskId);
        notificationIntent.putExtra(NotificationPublisher.BOOL_IS_END_TIME, false);

        // save to notif database & get requestCode
        int startNotifId = helper.makeStartNotif(task);
        if (startNotifId < 0) {
            Log.e("NotificationHelper", "error writing to notif db");
            return;
        }

        PendingIntent pendingIntentStartTime = PendingIntent.getBroadcast(
                mContext, startNotifId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntentStartTime);

        if (taskHasEndTime) {
            Intent intent = new Intent(mContext, NotificationPublisher.class);
            //intent.putExtra(NotificationPublisher.PARCEL_TASK_PARAMS, params);
            intent.putExtra(NotificationPublisher.LONG_TASK_ID, taskId);
            intent.putExtra(NotificationPublisher.BOOL_IS_END_TIME, true);

            // save to notif database & get requestCode
            int endNotifId = helper.makeEndNotif(task);
            if (endNotifId < 0) {
                Log.e("NotificationHelper", "error writing to notif db");
                return;
            }

            PendingIntent pendingIntentEndTime = PendingIntent.getBroadcast(
                    mContext, endNotifId, intent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            alarmManager.set(AlarmManager.RTC_WAKEUP, endTime, pendingIntentEndTime);
        }
    }

//    static class Params implements Parcelable {
//        private String name;
//        private long id;
//        private long startTime;
//        private long endTime;
//
//        private Params(Parcel in) {
//            name = in.readString();
//            id = in.readLong();
//            startTime = in.readLong();
//            endTime = in.readLong();
//        }
//
//        Params() {
//
//        }
//
//        public void setName(final String name) {
//            this.name = name;
//        }
//
//        public void setId(long id) {
//            this.id = id;
//        }
//
//        public void setStartTime(long startTime) {
//            this.startTime = startTime;
//        }
//
//        public void setEndTime(long endTime) {
//            this.endTime = endTime;
//        }
//
//        public static final Creator<Params> CREATOR = new Creator<Params>() {
//            @Override
//            public Params createFromParcel(Parcel in) {
//                return new Params(in);
//            }
//
//            @Override
//            public Params[] newArray(int size) {
//                return new Params[size];
//            }
//        };
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeString(name);
//            dest.writeLong(id);
//            dest.writeLong(startTime);
//            dest.writeLong(endTime);
//        }
//    }
}
