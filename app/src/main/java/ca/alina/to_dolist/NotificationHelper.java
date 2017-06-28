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

/**
 * Created by Alina on 2017-06-27.
 */

class NotificationHelper {
    private static final int smallIcon = R.drawable.ic_check_box_black_24dp;
    private static final long[] vibratePattern = {0, 500};

    static void scheduleNotification(Context context, Notification notification, int delay) {

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    static Notification getNotification(Context context, String content) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtoneString = sharedPref.getString(SettingsActivity.KEY_PREF_RINGTONE, "");
        boolean vibrate = sharedPref.getBoolean(SettingsActivity.KEY_PREF_VIBRATE, false);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(content)
                .setContentText("3 seconds have passed")
                .setSmallIcon(smallIcon);
        if (!ringtoneString.isEmpty()) {
            builder.setSound(Uri.parse(ringtoneString));
        }
        else {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }

        if (vibrate) {
            builder.setVibrate(vibratePattern);
        }

        Intent openAppIntent = new Intent(context, MainActivity.class);
        // open to default list?
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 11011, openAppIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }
}
