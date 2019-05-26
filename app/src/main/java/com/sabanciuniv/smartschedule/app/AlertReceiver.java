package com.sabanciuniv.smartschedule.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class AlertReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        String content_text = intent.getStringExtra("text");
        nb.setContentTitle("You have an upcoming event! - SmartScheduler");
        nb.setContentText(content_text);
        notificationHelper.getManager().notify(1, nb.build());
    }
}
