package com.anubhav.commonutility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.snackbar.Snackbar;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperMethod {

    public static void backPressAction(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.finishAndRemoveTask();
        } else {
            activity.finish();
        }
    }

    public static String generateRandomUUID() {
        UUID uniqueKey = UUID.randomUUID();
        return uniqueKey.toString();
    }

    public static long generateRandomNumber() {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return number;
    }

    public static void showMsgDialog(Context context, String title, String msg) {
        //AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public static final Pattern VALID_EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_MOBILE_REGEX = Pattern.compile("/^(\\+\\d{1,3}[- ]?)?\\d{10}$/");
    public static final Pattern VALID_MOBILE_TEN_REGEX = Pattern.compile("^\\d{10}$");

    public static boolean validateEmail(CharSequence email) {
        Matcher matcher = VALID_EMAIL_REGEX.matcher(email);
        return matcher.find();
    }

    public static boolean validatePassword(CharSequence password) {
        // password must be not contains password
        return Pattern.matches("[^ ]*", password);
    }

    public static boolean validateMobile(CharSequence mobile) {
        Matcher matcher = VALID_MOBILE_TEN_REGEX.matcher(mobile);
        return matcher.find();
    }

    /**
     * You can show popup menu with icons
     */
    @SuppressLint("RestrictedApi")
    public static void showPopupMenuWithIcons(PopupMenu popupMenu, View view) {
        MenuPopupHelper menuPopupHelper = new MenuPopupHelper(view.getContext(), (MenuBuilder) popupMenu.getMenu(), view);
        menuPopupHelper.setForceShowIcon(true);
        menuPopupHelper.show();
    }

    public static void restartApp(Context context) {
        Intent restartIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = restartIntent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    public static boolean isVisibleView(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public static String getFileExtension(String filePath) {
        String fileExtension = null;
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0) {
            fileExtension = filePath.substring(strLength + 1).toLowerCase();
        }
        return fileExtension;
    }

    public static String getFileName(String filePath) {
        String fileName = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fileName = Paths.get(new URI(filePath).getPath()).getFileName().toString();
            } else {
                String[] path = new URI(filePath).getPath().split("/");
                fileName = path[path.length - 1];
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /***  Get Status Bar Height for current device in px ***/
    public static int getStatusBarHeight(Activity activity) {
        ContextWrapper contextWrapper = (ContextWrapper) activity;
        int result = 0;
        int resourceId = contextWrapper.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = contextWrapper.getResources().getDimensionPixelSize(resourceId);
        } else {
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            result = rect.top;
        }
        return result;
    }

    public static void showSoftKeyboard(View view) {
        Context context = view.getContext();
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void showSoftKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideSoftKeyboard(View view) {
        Context context = view.getContext();
        if (context == null) return;
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = activity.findViewById(android.R.id.content);
        }
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /***  Convert dp to pixel(px) ***/
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static String convertDurationInString(long milliseconds) {
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        String finalTimerStr = "";
        String secondsStr = "";

        // Add hours if there
        if (hours > 0) finalTimerStr = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsStr = "0" + seconds;
        } else {
            secondsStr = "" + seconds;
        }

        finalTimerStr = finalTimerStr + minutes + ":" + secondsStr;

        return finalTimerStr;
    }

    public static boolean hasImage(@NonNull ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        boolean hasImage = (drawable != null);
        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }
        return hasImage;
    }


    /***  Floating Action Button Animation - Start Here  ***/
    public static void init(final View v) {
        v.setVisibility(View.GONE);
        v.setTranslationY(v.getHeight());
        v.setAlpha(0f);
    }

    public static boolean rotateFab(final View v, boolean rotate) {
        v.animate().setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
                .rotation(rotate ? 135f : 0f);
        return rotate;
    }

    public static void showIn(final View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationY(v.getHeight());
        v.animate()
                .setDuration(200)
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
                .alpha(1f)
                .start();
    }

    public static void showOut(final View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(1f);
        v.setTranslationY(0);
        v.animate()
                .setDuration(200)
                .translationY(v.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                }).alpha(0f)
                .start();
    }

    /***  Floating Action Button Animation - End Here  ***/


    // remove duplicate View from activity ( don't use on first View )
    public static void removeView(View view) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
    }

    // custom snackbar ( firstString & thirdString is a normal string) and ( secondString red color string)
    public static void snackbarLong(View view, String firstString, String secondString) {
        SpannableStringBuilder snackbarText = new SpannableStringBuilder();
        snackbarText.append(firstString);
        int boldStart = snackbarText.length();
        snackbarText.append(secondString);
        snackbarText.setSpan(new ForegroundColorSpan(0xFFFF0000), boldStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), boldStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Snackbar.make(view, snackbarText, Snackbar.LENGTH_SHORT).setAction("NoAction", null).show();
    }

    // convert drawable to bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        //int width = drawable.getIntrinsicWidth();
        //width = width > 0 ? width : 1;
        //int height = drawable.getIntrinsicHeight();
        //height = height > 0 ? height : 1;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static boolean checkImageResource(Context context, ImageView imageView, int imageResource) {
        boolean result = false;
        if (context != null && imageView != null && imageView.getDrawable() != null) {
            Drawable.ConstantState constantState;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                constantState = context.getResources().getDrawable(imageResource, context.getTheme()).getConstantState();
            } else {
                constantState = context.getResources().getDrawable(imageResource).getConstantState();
            }
            if (imageView.getDrawable().getConstantState() == constantState) {
                result = true;
            }
        }
        return result;
    }

    //check if a string contains only numbers
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //format a file size in Bytes, KB, MB, GB, TB
    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }
        return hrSize;
    }


    public static Bitmap fastBlur(Bitmap sentBitmap, float scale, int radius) {
        // scale must be in range ( 0.1 to 0.9 )

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);

        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public static void aboutDialog(final Activity activity) {
        String versionName = "";
        try {
            PackageManager packageManager = activity.getPackageManager();
            String packageName = activity.getPackageName();
            versionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "unknown";
        }
        /**
         new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
         .setTitle(R.string.about_title)
         .setMessage(activity.getString(R.string.about_text, versionName))
         .setPositiveButton(R.string.ok_btn, null)
         .setCancelable(false)
         .show();
         */
    }

    public static void forceCloseApp() {
        //Intent intent = new Intent(Intent.ACTION_MAIN);
        //intent.addCategory(Intent.CATEGORY_HOME);
        //MyApp.getContext().startActivity(intent);
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }

}
