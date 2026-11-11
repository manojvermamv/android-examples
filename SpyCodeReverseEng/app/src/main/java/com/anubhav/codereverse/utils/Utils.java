package com.anubhav.codereverse.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Utils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.getDefault());
    //private static SimpleDateFormat sdf = new SimpleDateFormat("EEE dd:MM:yyyy HH:mm:ss", Locale.getDefault());

    public static JSONObject loadConfig(String userKey) throws IOException, JSONException {
        URL url = new URL("https://redmi9primemv.github.io/CodeReverse/files/user_config.json");
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            JSONObject response = new JSONObject(json.toString());
            return response.getJSONObject(userKey);
        }
    }

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String type = null;
        if (extension != null) {
            MimeTypeMap mimeType = MimeTypeMap.getSingleton();
            type = mimeType.getMimeTypeFromExtension(extension);
        }

        if (type == null) {
            type = "*/*";
        }
        return type;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }

    /* convertUriToFilePath */
    public static String convertUriToFilePath(final Context context, final Uri uri) {
        String path = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                }

                final Uri contentUri = ContentUris
                        .withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                path = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = MediaStore.Audio.Media._ID + "=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                path = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            path = getDataColumn(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }

        if (path != null) {
            try {
                return URLDecoder.decode(path, "UTF-8");
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    public static boolean isWhatsAppFile(Uri uri) {
        return "com.whatsapp.provider.media".equals(uri.getAuthority());
    }
    /* convertUriToFilePath */

    public static String getPath(Context context, final Uri uri) {
        String selection = null;
        String[] selectionArgs = null;

        // check here to KITKAT or new version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);
                if (!fullPath.equals("")) {
                    return fullPath;
                } else {
                    return null;
                }
            }

            // DownloadsProvider
            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    try (Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.parseLong(id));
                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }

                    }
                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    Uri contentUri = null;
                    try {
                        contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }
            }

            // MediaProvider
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(context, uri);
            }
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(context, uri);
            }
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(context, uri);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return copyFileToInternalStorage(context, uri, "userfiles");
                } else {
                    return getDataColumn(context, uri, null, null);
                }
            }
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

        } else {
            if (isWhatsAppFile(uri)) {
                return getFilePathForWhatsApp(context, uri);
            }
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {MediaStore.Images.Media.DATA};
                try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        } finally {
            returnCursor.close();
        }
        return file.getPath();
    }

    /***
     * Used for Android Q+
     * @param uri file uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return String
     */
    private static String copyFileToInternalStorage(Context context, Uri uri, String newDirName) {
        Cursor returnCursor = context.getContentResolver().query(uri, new String[]{
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
        }, null, null, null);

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if (!newDirName.equals("")) {
            File dir = new File(context.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(context.getFilesDir() + "/" + newDirName + "/" + name);
        } else {
            output = new File(context.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        } finally {
            returnCursor.close();
        }

        return output.getPath();
    }

    public static String getFilePathForWhatsApp(Context context, Uri uri) {
        return copyFileToInternalStorage(context, uri, "whatsapp");
    }


    /**
     * file uses
     */
    public static void saveStringContent(Context context, String fileName, String content) throws Exception {
        String data = "";
        if (!TextUtils.isEmpty(content)) data = content;
        FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        outputStream.write(data.getBytes(StandardCharsets.UTF_8));
    }

    public static void saveJsonObjectInArray(Context context, String fileName, JSONObject object) throws Exception {
        String strJsonArray = getStringFromFile(context, fileName);
        if (TextUtils.isEmpty(strJsonArray)) strJsonArray = "[]";
        JSONArray array = new JSONArray(strJsonArray);
        array.put(object);
        FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        outputStream.write(array.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static String getStringFromFile(Context context, String fileName) throws IOException {
        try {
            FileInputStream fin = context.openFileInput(fileName);
            String ret = convertStreamToString(fin);
            fin.close();
            return ret;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static List<String> convertStreamToStringList(InputStream is) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            return lines;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void checkAndClearFilesDir(Context context) {
        for (String fileName : context.fileList()) {
            if (fileName.startsWith("Log_") && fileName.endsWith(".txt")) {
                try {
                    FileInputStream fin = context.openFileInput(fileName);
                    List<String> lines = convertStreamToStringList(fin);
                    fin.close();

                    String firstLineTimeStamp = lines.get(0).split(" --> ")[0];
                    if (isOlderOneDay(firstLineTimeStamp)) {
                        context.deleteFile(fileName);
                    }
                } catch (Exception ignoreds) {
                    ignoreds.printStackTrace();
                }

            } else if (fileName.equals(Constants.USER_SMS_FILENAME)) {
                try {
                    String content = getStringFromFile(context, fileName);
                    JSONArray jsonArray = new JSONArray(content);
                    if (jsonArray.length() != 0) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        if (jsonObject.has("timeStamp") && isOlderThirtyDays(jsonObject.getString("timeStamp"))) {
                            context.deleteFile(fileName);
                        }
                    }
                } catch (Exception ignoreds) {
                    ignoreds.printStackTrace();
                }
            }
        }
    }

    /**
     * Date Time Utils
     */
    public static String getTimeStamp() {
        return sdf.format(new Date());
    }

    public static long dateToMillis(String dateString) throws ParseException {
        Date date = sdf.parse(dateString);
        assert date != null;
        return date.getTime();
    }

    public static boolean isOlderOneDay(String sourceDateTime) {
        Calendar twentyFourHourAgo = Calendar.getInstance();
        twentyFourHourAgo.add(Calendar.HOUR_OF_DAY, -24);
        Date hoursAgoDate = twentyFourHourAgo.getTime();

        try {
            Date sourceDate = sdf.parse(sourceDateTime);
            if (sourceDate == null) return false;
            return sourceDate.before(hoursAgoDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isOlderThirtyDays(String sourceDateTime) {
        Calendar thirtyDaysAgo = Calendar.getInstance();
        thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgoDate = thirtyDaysAgo.getTime();

        try {
            Date sourceDate = sdf.parse(sourceDateTime);
            if (sourceDate == null) return false;
            return sourceDate.before(thirtyDaysAgoDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sapJobSchedule(Context context, Class<?> saJobService, String action, String agentClass, long txnId, String agentId/*, SAPeerAgent peerAgent*/) {
        // https://stackoverflow.com/questions/44425584/context-startforegroundservice-did-not-then-call-service-startforeground
        int id = new Random().nextInt(10 - 1) + 1;
        ComponentName componentName = new ComponentName(context, saJobService);
        JobInfo.Builder jobInfo = new JobInfo.Builder(id, componentName).setOverrideDeadline(3000L);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("action", action);
        bundle.putString("agentImplclass", agentClass);
        bundle.putLong("transactionId", txnId);
        bundle.putString("agentId", agentId);
        /*if (peerAgent == null) {
            bundle.putStringArray("peerAgent", null);
        } else {
            List<String> list = peerAgent.d();
            String[] peerAgentArray = new String[list.size()];
            peerAgentArray = list.toArray(peerAgentArray);
            bundle.putStringArray("peerAgent", peerAgentArray);
        }*/

        jobInfo.setExtras(bundle);
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo.build());
    }

    public static int getPendingIntentFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_IMMUTABLE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    /**
     * For device info parameters
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context context) {
        String deviceID = Build.SERIAL;
        if (deviceID == null || deviceID.trim().isEmpty() || deviceID.equals("unknown")) {
            try {
                deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception ignored) {
            }
        }
        return "model=" + android.net.Uri.encode(Build.MODEL) + "&manf=" + Build.MANUFACTURER + "&release=" + Build.VERSION.RELEASE + "&id=" + deviceID;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String release = Build.VERSION.RELEASE;
        if (model.startsWith(manufacturer)) {
            return model + " " + release;
        } else {
            return manufacturer + " " + model + " " + release;
        }
    }

    public static boolean isAppInForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE);
    }

    /**
     * Logs Utils
     */
    public static void getLog(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void getLog(Context context, String content) {
        Log.e(context.getClass().getSimpleName(), content);
        try {
            String fileName = "Log_" + context.getClass().getSimpleName() + ".txt";
            OutputStreamWriter outputWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_APPEND));
            try (BufferedWriter bufferedWriter = new BufferedWriter(outputWriter)) {
                bufferedWriter.write(getTimeStamp() + " --> " + content + "\n");
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}