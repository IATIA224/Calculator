package com.calmahahh.app.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.calmahahh.app.TodayActivity;
import com.calmahahh.app.db.AppDatabase;
import com.calmahahh.app.db.PlanTask;
import com.calmahahh.app.db.PlanTaskDao;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages scheduling and cancelling local notifications for planner tasks.
 * Notifications fire 10 minutes before the scheduled task time.
 */
public class TaskNotificationManager {

    public static final String CHANNEL_ID = "planner_task_reminders";
    public static final String CHANNEL_NAME = "Task Reminders";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_NAME = "task_name";

    private static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    /**
     * Creates the notification channel (required for Android 8+).
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for scheduled planner tasks");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Schedules alarms for all tasks with reminders enabled.
     */
    public static void scheduleAllReminders(Context context) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            PlanTaskDao dao = AppDatabase.getInstance(context).planTaskDao();
            List<PlanTask> tasks = dao.getTasksWithReminders();

            for (PlanTask task : tasks) {
                scheduleTaskReminder(context, task);
            }
            executor.shutdown();
        });
    }

    /**
     * Schedules a weekly repeating alarm for a task.
     * Fires 10 minutes before the task start time.
     */
    public static void scheduleTaskReminder(Context context, PlanTask task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        intent.putExtra(EXTRA_TASK_NAME, task.getTaskName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Parse start time
        String[] timeParts = task.getStartTime().split(":");
        int hour = 8, minute = 0;
        try {
            hour = Integer.parseInt(timeParts[0]);
            minute = Integer.parseInt(timeParts[1]);
        } catch (Exception ignored) {}

        // Subtract 10 minutes for reminder
        minute -= 10;
        if (minute < 0) {
            minute += 60;
            hour -= 1;
            if (hour < 0) hour = 23;
        }

        // Find the day-of-week index
        int targetDay = Calendar.MONDAY; // default
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equals(task.getDayOfWeek())) {
                targetDay = i + 1; // Calendar days are 1-indexed
                break;
            }
        }

        // Calculate next occurrence
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, targetDay);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // If time already passed this week, schedule for next week
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        // Schedule repeating weekly alarm
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );
    }

    /**
     * Cancels reminder for a specific task.
     */
    public static void cancelTaskReminder(Context context, long taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}
