package ca.alina.to_dolist;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;

/** Helper class for scheduling notifications.
 * Create it within a method, not an Activity (?)
 */
class NotificationHelper {
    private static final int smallIcon = R.drawable.ic_check_box_black_24dp;
    private static final long[] vibratePattern = {0, 500};

    private Context mContext;

    NotificationHelper(final Context context) {
        mContext = context;
    }

    void scheduleNotification(Params params, int delay) {
        final Notification notification = makeNotif(params.name);

        Intent notificationIntent = new Intent(mContext, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification makeNotif(String content) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String ringtoneString = sharedPref.getString(SettingsActivity.KEY_PREF_RINGTONE, "");
        boolean vibrate = sharedPref.getBoolean(SettingsActivity.KEY_PREF_VIBRATE, false);

        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle(content)
                .setContentText("3 seconds have passed")
                .setSmallIcon(smallIcon);
        if (!ringtoneString.isEmpty()) {
            builder.setSound(Uri.parse(ringtoneString));
        }
        if (vibrate) {
            builder.setVibrate(vibratePattern);
        }

        Intent openAppIntent = new Intent(mContext, MainActivity.class);
        // open to default list?
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 11011, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }

    class Params {
        private String name;

        public void setName(final String name) {
            this.name = name;
        }
    }
}
