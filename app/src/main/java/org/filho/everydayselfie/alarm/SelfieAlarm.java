package org.filho.everydayselfie.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by Roberto Filho on 19/08/17.
 */

public class SelfieAlarm {

    private Context mActivityContext;
    private AlarmManager mAlarmManager;

    private static final long TWO_MINUTE_ALARM_DELAY = 2 * 60 * 1000L;
    private static final long THIRTY_SECONDS_ALARM_DELAY = 30 * 1000L;
    // 900000

    public SelfieAlarm(Context activityContext, AlarmManager alarmManager) {
        this.mActivityContext = activityContext;
        this.mAlarmManager = alarmManager;
    }


    public void start() {
        long alarmDelay = TWO_MINUTE_ALARM_DELAY;

        // Create an Intent to broadcast to the AlarmNotificationReceiver
        Intent mNotificationReceiverIntent =
                new Intent(mActivityContext, AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        PendingIntent mNotificationReceiverPendingIntent =
                PendingIntent.getBroadcast(mActivityContext, 0, mNotificationReceiverIntent, 0);

        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + alarmDelay,
                alarmDelay, // AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                mNotificationReceiverPendingIntent);
    }
}
