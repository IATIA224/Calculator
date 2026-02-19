package com.calmahahh.app.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Re-schedules all task reminders after device reboot.
 * Registered in AndroidManifest with BOOT_COMPLETED action.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TaskNotificationManager.createNotificationChannel(context);
            TaskNotificationManager.scheduleAllReminders(context);
        }
    }
}
