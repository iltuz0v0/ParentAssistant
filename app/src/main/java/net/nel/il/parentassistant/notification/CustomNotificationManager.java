package net.nel.il.parentassistant.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.main.MainActivity;

public class CustomNotificationManager {

    private static final int notificationRequestCode = 15;

    private static final int notificationId = 1;

    public void createRequestNotification(NotificationManager manager, int id, int type, MainActivity context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(context.getResources().getString(R.string.notification_request_title));
        builder.setContentText(context.getResources().getString(R.string.notification_request_text));
        builder.setSmallIcon(R.drawable.face);
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(context.getResources().getString(R.string.intent_extra_id), id);
        intent.putExtra(context.getResources().getString(R.string.intent_extra_type), type);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                notificationRequestCode, intent,
                PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;
        manager.notify(notificationId, notification);
    }

}
