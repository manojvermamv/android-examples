package com.anubhav.codereverse.uiapp.utils;

import static android.os.Build.VERSION.SDK_INT;

import static com.anubhav.codereverse.uiapp.common.IConstants.SLASH;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.anubhav.codereverse.uiapp.data.model.LocationAddress;
import com.anubhav.codereverse.uiapp.ui.SplashActivity;
import com.anubhav.commonutility.FileUtils;
import com.anubhav.codereverse.BuildConfig;
import com.anubhav.codereverse.R;
import com.anubhav.codereverse.uiapp.common.IConstants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final String defaultAppPath = Environment.getExternalStorageDirectory() + SLASH + BuildConfig.APP_NAME + SLASH;

    public static String getThreadInfo() {
        Thread thread = Thread.currentThread();
        return "@[name=" + thread.getName() + ", id=" + thread.getId() + "]";
    }

    public static void printIntentExtras(Intent intent) {
        if (intent.getData() != null) {
            Log.e("Intent Data --> ", "" + intent.getData());
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object obj = extras.get(key);
                if (obj == null) {
                    Log.e("Intent Extras --> ", key + " - Null");
                } else if (obj instanceof String) {
                    Log.e("Intent Extras --> ", key + " - " + (String) obj);
                } else if (obj instanceof Boolean) {
                    Log.e("Intent Extras --> ", key + " - " + (Boolean) obj);
                } else if (obj instanceof Integer) {
                    Log.e("Intent Extras --> ", key + " - " + (Integer) obj);
                } else {
                    Log.e("Intent Extras --> ", key + " - " + obj);
                }
            }
        }
    }

    public static boolean hasCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasWritePermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity, int request_code) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, request_code);
    }

    public static void requestReadPermission(Activity activity, int request_code) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, request_code);
    }

    public static void requestWritePermission(Activity activity, int request_code) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request_code);
    }

    public static boolean allGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String getApplicationVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String[] split(String str) {
        return str.split("\\s*,\\s*");
    }

    private static Pattern NAME_PATTERN = Pattern.compile("[\\w][\\w _-]{1,22}[\\w]");

    // check for a name that has no funny unicode characters to not let them look to much like other names
    public static boolean isValidName(String name) {
        if (name == null || name.length() == 0) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isEmpty(final Object s) {
        if (s == null) {
            return true;
        }
        if ((s instanceof String) && (((String) s).trim().length() == 0)) {
            return true;
        }
        if (s instanceof Map) {
            return ((Map<?, ?>) s).isEmpty();
        }
        if (s instanceof List) {
            return ((List<?>) s).isEmpty();
        }
        if (s instanceof Object[]) {
            return (((Object[]) s).length == 0);
        }
        return false;
    }

    private static void sout(String msg) {
        if (BuildConfig.DEBUG) {
            System.out.println("Pra :: " + msg);
        }
    }

    private static void getErrors(final Exception e) {
        if (BuildConfig.DEBUG) {
            final String stackTrace = "Pra ::" + Log.getStackTraceString(e);
            System.out.println(stackTrace);
        }
    }

    public static void RTLSupport(Window window) {
        try {
            window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int calPercentage(int value, float percentage) {
        return (int) (value * (percentage / 100.0f));
    }

    public static void shareApp(final Activity mActivity, final String title) {
        try {
//            final String app_name = Html.fromHtml(title).toString();
//            final String share_text = Html.fromHtml(mActivity.getResources().getString(R.string.strShareContent)).toString();
//            final Intent sendIntent = new Intent();
//            sendIntent.setAction(Intent.ACTION_SEND);
//            sendIntent.putExtra(Intent.EXTRA_TEXT, app_name + "\n\n" + share_text + "\n\n" + "https://play.google.com/store/apps/details?id=" + mActivity.getPackageName());
//            sendIntent.setType("text/plain");
//            mActivity.startActivity(sendIntent);
        } catch (Resources.NotFoundException e) {
            Utils.getErrors(e);
        }
    }

    public static void shareApp(final Activity mActivity) {
//        shareApp(mActivity, mActivity.getResources().getString(R.string.strShareTitle));
    }

    public static void rateApp(final Activity mActivity) {
        final String appName = mActivity.getPackageName();
        try {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(IConstants.DEFAULT_UPDATE_URL_2 + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(IConstants.DEFAULT_UPDATE_URL + appName)));
        }
    }

    public static String getDateTime() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date date = new Date();

        return dateFormat.format(date);
    }

    public static String getDateTimeStampName() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getCapsWord(String name) {
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    public static String formatDateTime(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    public static String formatTime(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    public static String formatLocalTime(long timeInMillis) {
        SimpleDateFormat dateFormatUTC = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = dateFormatUTC.parse(formatTime(timeInMillis));
        } catch (Exception ignored) {
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        if (date == null) {
            return dateFormat.format(timeInMillis);
        }
        return dateFormat.format(date);
    }

    public static String formatLocalFullTime(long timeInMillis) {
        SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = dateFormatUTC.parse(formatDateTime(timeInMillis));
        } catch (Exception e) {
            Utils.getErrors(e);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        if (date == null) {
            return dateFormat.format(timeInMillis);
        }
        return dateFormat.format(date);
    }

    public static String formatDateTime(final Context context, final String timeInMillis) {
        long localTime = 0L;
        try {
            localTime = dateToMillis(formatLocalFullTime(dateToMillis(timeInMillis)));
        } catch (Exception e) {
            Utils.getErrors(e);
        }
        if (isToday(localTime)) {
            return formatTime(context, localTime);
        } else {
            return formatDateNew(localTime);
        }
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.getDefault());

    public static long dateToMillis(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = sdf.parse(dateString);
        assert date != null;
        return date.getTime();
    }

    public static String formatFullDate(String timeString) {
        long timeInMillis = 0;
        try {
            timeInMillis = dateToMillis(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(timeInMillis).toUpperCase();
    }

    /**
     * Formats timestamp to 'date month' format (e.g. 'February 3').
     */
    public static String formatDateNew(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yy HH:mm", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    /**
     * Returns whether the given date is today, based on the user's current locale.
     */
    public static boolean isToday(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        String date = dateFormat.format(timeInMillis);
        return date.equals(dateFormat.format(Calendar.getInstance().getTimeInMillis()));
    }

    /**
     * Checks if two dates are of the same day.
     *
     * @param millisFirst  The time in milliseconds of the first date.
     * @param millisSecond The time in milliseconds of the second date.
     * @return Whether {@param millisFirst} and {@param millisSecond} are off the same day.
     */
    public static boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }

    public static String formatLocalTime(Context context, long when) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        int flags = DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_NO_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL;

        if (then.year != now.year) {
            flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        return DateUtils.formatDateTime(context, when, flags);
    }

    public static String formatTime(Context context, long when) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        int flags = DateUtils.FORMAT_NO_NOON | DateUtils.FORMAT_NO_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL;

        if (then.year != now.year) {
            flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        return DateUtils.formatDateTime(context, when, flags);
    }

    public static void openCallIntent(final Context context, final String number) {
        try {
            final Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    public static void setHTMLMessage(final TextView lblName, final String strMsg) {
        if (SDK_INT >= Build.VERSION_CODES.N) {
            lblName.setText(Html.fromHtml(strMsg, Html.FROM_HTML_MODE_LEGACY));
        } else {
            lblName.setText(Html.fromHtml(strMsg));
        }
    }

//    public static File getReceiveDirectory(Context context, String type) {
//        final String directoryName = AttachmentTypes.getTypeName(type);
//        final String mainPath = SLASH + context.getString(R.string.app_name) + SLASH + directoryName;
//        File file;
//        if (isAboveQ()) {
//            file = new File(IConstants.SDPATH + AttachmentTypes.getDirectoryByType(type), mainPath);
//        } else {
//            file = new File(Environment.getExternalStorageDirectory(), SLASH + AttachmentTypes.getDirectoryByType(type) + mainPath);
//        }
//        return file;
//    }
//
//    public static File getSentDirectory(Context context, String type) {
//        final String directoryName = AttachmentTypes.getTypeName(type);
//        final String systemFolder = AttachmentTypes.getDirectoryByType(directoryName);// Audio, Movie or Download(System Folders)
//
//        File file;
//        if (isAboveQ()) {
//            file = new File(IConstants.SDPATH + systemFolder, SLASH + context.getString(R.string.app_name) + SLASH + directoryName + IConstants.SENT_FILE);
//        } else {
//            file = new File(Environment.getExternalStorageDirectory(), SLASH + systemFolder + SLASH + context.getString(R.string.app_name) + SLASH + directoryName + IConstants.SENT_FILE);
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//        }
//        return file;
//    }

    public static File getSentFile(final File directory, final String extension) {
        //final String ext = "_" + Utils.getDateTimeName() + extension;
        if (extension.equalsIgnoreCase(IConstants.EXT_MP3)) {
            return new File(directory, "REC" + extension);
        } else if (extension.equalsIgnoreCase(IConstants.EXT_VCF)) {
            return new File(directory, "CONT" + extension);
        }
        return new File(directory, Utils.getDateTimeStampName() + extension);
    }

//    public static File getDownloadDirectory(Context context, String type) {
//        String directoryName = type;
//        if (type.equalsIgnoreCase(IConstants.TYPE_RECORDING)) {
//            directoryName = AttachmentTypes.getTypeName(AttachmentTypes.RECORDING);
//        }
//        Utils.sout("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Download Directory::: " + type + " == " + directoryName);
//        return new File(AttachmentTypes.getDirectoryByType(type), SLASH + context.getString(R.string.app_name) + SLASH + directoryName);
//    }
//
//    public static String getUniqueFileName(final File fileToUpload, int attachmentType) {
//        String pathSegment = Uri.fromFile(fileToUpload).getLastPathSegment();
//        String fileExtension = FileUtils.getFileExtension(pathSegment);
//        if (!Utils.isEmpty(fileExtension)) {
//            pathSegment = pathSegment.replaceAll(fileExtension, AttachmentTypes.getExtension(fileExtension, attachmentType));
//        }
//
//        String end = "_" + Utils.getDateTimeStampName() + fileExtension;
//        Utils.sout("----New File Name:: " + fileToUpload.getAbsolutePath() + " >>> " + end);
//        return pathSegment.replaceAll(fileExtension, end);
//    }

    public static String getExtension(Context context, final Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();
        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public static String getMusicFolder() {
        return Environment.DIRECTORY_MUSIC;
    }

    public static String getMoviesFolder() {
        return Environment.DIRECTORY_MOVIES;
    }

    public static String getDownloadFolder() {
        return Environment.DIRECTORY_DOWNLOADS;
    }

    /**
     * SAF = Storage Access Framework (Scoped Storage)
     * It is only work for Android SDK >= 29 (Android ver >= 10 -> Android Q)
     * This devices didn't use WRITE_EXT_STORAGE Persmission and use new SAF
     */
    public static boolean isAboveQ() {
        return SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * isStoreFile = True -> It is storing the file via InputStream, False means do not store the file, just put the entry in contentResolver
     */
    public static File moveFileToFolder(final Context mContext, boolean isStoreFile, String newFileName, File sourceFile, int attachmentType) {
//        try {
//            final ContentResolver resolver = mContext.getContentResolver();
//            final ContentValues contentValues = new ContentValues();
//            final String type = AttachmentTypes.getTypeName(attachmentType);
//            final File dest = isStoreFile ? Utils.getSentDirectory(mContext, type) : Utils.getDownloadDirectory(mContext, type);
//            Uri target;
//
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName);
//            try {
//                final String mimeType = FileUtils.getMimeType(new File(newFileName));
//                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
//                //Utils.sout("OKNew File to Folder::: " + isStoreFile + " >> " + newFileName + " >>> " + type + " >dest> " + dest + " ::mimeType:: " + mimeType);
//            } catch (Exception ignored) {
//
//            }
//            if (Utils.isAboveQ()) {
//                String tempDest = dest.toString().replaceAll(IConstants.SDPATH, "");
//                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, tempDest);
//                target = AttachmentTypes.getTargetUri(type); //MediaStore.Downloads.EXTERNAL_CONTENT_URI;
//            } else {
//                contentValues.put(MediaStore.MediaColumns.DATA, dest.toString());
//                target = MediaStore.Files.getContentUri("external");
//            }
//            Uri uri = resolver.insert(target, contentValues);
//            if (isStoreFile) {
//                try {
//                    if (Utils.isAboveQ()) {
//                        try {
//                            InputStream is = new FileInputStream(sourceFile);
//                            OutputStream os = resolver.openOutputStream(uri, "rwt");
//                            byte[] buffer = new byte[1024];
//                            for (int r; (r = is.read(buffer)) != -1; ) {
//                                os.write(buffer, 0, r);
//                            }
//                            os.flush();
//                            os.close();
//                            is.close();
//                        } catch (Exception e) {
//                            Utils.getErrors(e);
//                        }
//                    } else {
//                        final File newFile = new File(dest, newFileName);
//                        FileUtils.copyFileOrDir(sourceFile, newFile);
//                    }
//                } catch (Exception e) {
//                    Utils.getErrors(e);
//                }
//            }
//            //Utils.sout("moved File successfully:: " + dest.toString());
//            return dest;
//        } catch (Exception e) {
//            Utils.getErrors(e);
//        }
        return null;
    }

    //Extract file extension from full path
    public static String getFileExtensionFromPath(String string) {
        int index = string.lastIndexOf(".");
        return string.substring(index + 1);
    }

    //Used to open the file by system
    public static Intent getOpenFileIntent(final Context context, final String path) {
        final String fileExtension = getFileExtensionFromPath(path);
        final File toInstall = new File(path);

        //if it's apk make the system open apk installer
        if (fileExtension.equalsIgnoreCase("apk")) {
            if (SDK_INT >= Build.VERSION_CODES.N) {
                final Uri apkUri = getUriForFileProvider(context, toInstall);
                final Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return intent;
            } else {
                final Uri apkUri = Uri.fromFile(toInstall);
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return intent;
            }

        } else { //else make the system open an app that can handle given type
            if (SDK_INT >= Build.VERSION_CODES.N) {
                final Intent newIntent = new Intent(Intent.ACTION_VIEW);
                newIntent.setDataAndType(getUriForFileProvider(context, toInstall), Utils.getMimeType(context, Uri.fromFile(toInstall)));
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return newIntent;
            } else {
                final Intent newIntent = new Intent(Intent.ACTION_VIEW);
                newIntent.setDataAndType(getUriForFileProvider(context, toInstall), Utils.getMimeType(context, Uri.fromFile(toInstall)));
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return newIntent;
            }
        }
    }

    public static void openPlayingVideo(final Context context, final File file) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(getUriForFileProvider(context, file), Utils.getMimeType(context, Uri.fromFile(file)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    public static Uri getUriForFileProvider(Context context, File file) {
        return FileProvider.getUriForFile(context, BuildConfig.FILE_PROVIDER, file);
    }

    private static long getVideoDurationValidation(Context context, File file) {
        try {
            final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(file));
            final String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final long durationMs = Long.parseLong(time);
            retriever.release();
            return durationMs;
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static String getVideoDuration(Context context, File file) {
        return convertSecondsToHMmSs(getVideoDurationValidation(context, file));
    }

    public static String getFileNameFromUrl(String url) {
        String filename;
        if (SDK_INT >= Build.VERSION_CODES.O) {
            filename = Paths.get(url).getFileName().toString();
        } else {
            filename = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
        }
        return filename;
    }

    public static String getMimeType(final Context context, final Uri uri) {
        String mimeType = "";
        try {
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                final ContentResolver cr = context.getContentResolver();
                mimeType = cr.getType(uri);
            } else {
                final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        } catch (Exception e) {
            Utils.getErrors(e);
        }
        return mimeType;
    }

    public static void setChatImage(Context mContext, String imgUrl, ImageView mImageView) {
        try {
            final int roundedCorner = 16;
            final GranularRoundedCorners gCorner = new GranularRoundedCorners(roundedCorner, roundedCorner, roundedCorner, roundedCorner);
//            Glide.with(mContext).load(imgUrl).placeholder(R.drawable.image_load)
//                    .thumbnail(0.5f)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .centerCrop()
//                    .fallback(R.drawable.image_load)
//                    .transform(new CenterCrop(), gCorner)
//                    .into(mImageView);
        } catch (Exception ignored) {
        }
    }

    public static void chatSendSound(Context context) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context.getAssets().openFd("chat_tone.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }


    public static String getFileSize(final long size) {
        try {
            final int BYTES_IN_KILOBYTES = 1024;
            final String KILOBYTES = " KB";
            final String MEGABYTES = " MB";
            final String GIGABYTES = " GB";
            float fileSize = 0;
            String suffix = KILOBYTES;

            if (size > BYTES_IN_KILOBYTES) {
                fileSize = size / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    if (fileSize > BYTES_IN_KILOBYTES) {
                        fileSize = fileSize / BYTES_IN_KILOBYTES;
                        suffix = GIGABYTES;
                    } else {
                        suffix = MEGABYTES;
                    }
                }
            }
            return new DecimalFormat("###.#").format(fileSize) + suffix;
        } catch (Exception e) {
            return "";
        }
    }

    public static String convertSecondsToHMmSs(final long mySec) {
        final long seconds = mySec / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }

    public static File getCacheFolder(Context context) {
        return context.getExternalFilesDir(null);
    }

    public static void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory != null) {
                if (fileOrDirectory.isDirectory()) {
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteRecursive(child);
                    }
                }
                fileOrDirectory.delete();
            }
        } catch (Exception ignored) {
        }
    }

    public static void restartApp(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 123321, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);
        System.exit(0);
    }

    /*
     * Locations
     * */
    public static boolean isGPSEnabled(Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String getCountry(String countryName) {
        String[] isoCountryCodes = Locale.getISOCountries();
        for (String code : isoCountryCodes) {
            Locale locale = new Locale("", code);
            if (countryName.equalsIgnoreCase(locale.getDisplayCountry())) {
                return code;
            }
        }
        return "";
    }

    public static String getCountryName(String country) {
        String[] isoCountryCodes = Locale.getISOCountries();
        for (String code : isoCountryCodes) {
            Locale locale = new Locale("", code);
            if (country.equalsIgnoreCase(locale.getCountry())) {
                return code;
            }
        }
        return "";
    }

    public static String getCurrentCountry(Context context) {
        String country;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            country = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            country = context.getResources().getConfiguration().locale.getCountry();
        }
        return country;
    }

    public static String getCurrentCountryName(Context context) {
        String countryName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryName = context.getResources().getConfiguration().getLocales().get(0).getDisplayCountry();
        } else {
            countryName = context.getResources().getConfiguration().locale.getDisplayCountry();
        }
        return countryName;
    }

    private static final String staticMap = "https://maps.googleapis.com/maps/api/staticmap?key=%s&center=%s,%s&zoom=18&size=280x160&scale=2&format=jpg&markers=color:red|%s,%s|scale:4";

    public static void showStaticMap(final Context mContext, final LocationAddress locationAddress, int topLeft, int topRight, ImageView imgLocation) {
        Glide.with(mContext).load(String.format(staticMap, BuildConfig.MAPS_KEY, locationAddress.getLatitude(), locationAddress.getLongitude(), locationAddress.getLatitude(), locationAddress.getLongitude()))
                .transform(new CenterInside(), new GranularRoundedCorners(topLeft, topRight, 4, 4))
                .into(imgLocation);
    }

    public static void openMapWithAddress(final Context mContext, final LocationAddress locationAddress) {
        try {
            final Uri gmmIntentUri = Uri.parse("geo:" + locationAddress.getLatitude() + "," + locationAddress.getLongitude() + "?q=" + Uri.encode(locationAddress.getAddress()));
            final Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(mapIntent);
            }
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

}