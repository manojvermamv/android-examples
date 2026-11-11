package com.anubhav.codereverse.uiapp.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import com.anubhav.codereverse.R;
import com.anubhav.codereverse.uiapp.common.IConstants;
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository;
import com.anubhav.codereverse.uiapp.database.preferences.SharedPreferenceManager;
import com.anubhav.codereverse.uiapp.ui.ApplicationLifecycleManager;
import com.anubhav.codereverse.uiapp.ui.notifications.NotificationsActivity;
import com.anubhav.codereverse.uiapp.utils.JSONUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager notificationManager;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        SharedPreferenceManager preferences = SharedPreferenceManager.get(this);
        preferences.fcmToken(s);
        new FirebaseDatabaseRepository().uploadFcmToken(preferences.loggedUID(), s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            createNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void createNotification(String title, String content) {
        int notificationId = (int) System.currentTimeMillis();
        Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, intent, getPendingIntentFlag());

        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());

        createNotificationChannel(getApplicationContext(), notificationManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(getApplicationContext(), getString(R.string.default_notification_channel_id));
        }
        notificationBuilder
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent);

        if (ApplicationLifecycleManager.isAppVisible()) {
            notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(content));
        }
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public static void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(context, notificationManager);
    }

    public static void createNotificationChannel(Context context, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = context.getString(R.string.default_notification_channel_id);
            String channelName = context.getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            //channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static int getPendingIntentFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_MUTABLE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    /**
     * Creates a {@link NotificationData} object from @{@link RemoteMessage} by extracting parameters from data.
     * setTimeStamp() must be called for using timeStamp as notificationID
     *
     * @param remoteMessage {@link RemoteMessage} received from firebase cloud messaging
     * @return A {@link NotificationData} representing the notification received
     */
    private NotificationData createNotificationModel(@NonNull RemoteMessage remoteMessage) {
        try {
            final String receivedStrData = remoteMessage.getData().get(IConstants.FCM_DATA);
            final NotificationData notificationData = JSONUtils.objectFromJson(receivedStrData, NotificationData.class);
            //notificationData.setTimeStamp((int) System.currentTimeMillis());
            return notificationData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}