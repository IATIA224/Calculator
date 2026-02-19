package com.calmahahh.app.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.calmahahh.app.R;
import com.calmahahh.app.TodayActivity;

/**
 * Receives alarm broadcasts and shows notification for task reminders.
 */
public class TaskReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra(TaskNotificationManager.EXTRA_TASK_ID, -1);
        String taskName = intent.getStringExtra(TaskNotificationManager.EXTRA_TASK_NAME);

        if (taskName == null) taskName = "Task";

        // Create intent to open Today screen when notification is tapped
        Intent tapIntent = new Intent(context, TodayActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) taskId, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TaskNotificationManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ Task Reminder")
                .setContentText(taskName + " starts in 10 minutes!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) taskId, builder.build());
    }
}
