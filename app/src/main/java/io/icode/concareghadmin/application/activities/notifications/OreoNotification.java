package io.icode.concareghadmin.application.activities.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import io.icode.concareghadmin.application.R;

public class OreoNotification extends ContextWrapper{

    private static final String CHANNEL_ID = "io.icode.concaregh.application";

    private static final String CHANNEL_NAME = "concareghapp";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        /*check build version of device sending the notification
         and the device to sent the notification to(target device)*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // method call to create notification channel
            createChannel();
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    // method to create notification channel
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        // creating notificationChannel with the help of the NotificationManager
        getManager().createNotificationChannel(channel);
    }

    // method to create notification manager
    public NotificationManager getManager(){
        // check if notificationManager is null
        if(notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // return notificationManager
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    // method to create notification channel
    public Notification.Builder getOreoNotification(String title, String body,
                                                    PendingIntent pendingIntent, Uri soundUri,String icon){
        // return new Notification.Builder object
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setSmallIcon(R.mipmap.app_logo_round)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
    }

}
