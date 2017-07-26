package ca.alina.to_dolist;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ca.alina.to_dolist.database.DatabaseHelper;
import ca.alina.to_dolist.database.schema.Task;

public class MarkDoneReceiver extends BroadcastReceiver {
    public static final String ACTION_MARK = "ca.alina.to_dolist.action.MARK";

    public static final String EXTRA_TASK_ID = "ca.alina.to_dolist.extra.TASK_ID";
    public static final String EXTRA_NOTIF_ID = "ca.alina.to_dolist.extra.NOTIF_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("MarkDoneReceiver", "starting mark task as done");
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
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
    }
}
