package com.anubhav.codereverse.utils;

import android.content.Context;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ApkUtils {

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static void uninstallApplication(Context context, String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void installApkInternally(Context context, Uri contentUri) throws Exception {
        String packageName = context.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent unKnownSourceIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", packageName)));
            unKnownSourceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                context.startActivity(unKnownSourceIntent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(contentUri, "application/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            context.startActivity(intent);

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        }
    }

    public static void launchApplication(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Utils.getLog(context, "launchApplication 1: " + e);
            try {
                // The URL should either launch directly in a non-browser app
                // (if it’s the default), or in the disambiguation dialog
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(packageName));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER);
                else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            } catch (Exception e2) {
                Utils.getLog(context, "launchApplication 2: " + e2);
            }
        }
    }

    /**
     * check if installed apk and uninstalled apk have the same Signatures.
     *
     * @param context         *
     * @param packageName     installed apk package's name
     * @param archiveFilePath uninstalled apk fill full path
     * @return *
     */

    public static boolean isSignatureSame(Context context, String packageName, String archiveFilePath) {
        return IsSignaturesSame(
                getInstalledPackageSignatures(context, packageName),
                getSignaturesFromApk(new File(archiveFilePath)));
    }

    private static boolean IsSignaturesSame(Signature[] s1, Signature[] s2) {
        if (s1 == null) {
            return false;
        }
        if (s2 == null) {
            return false;
        }
        HashSet<Signature> set1 = new HashSet<>();
        Collections.addAll(set1, s1);
        HashSet<Signature> set2 = new HashSet<>();
        Collections.addAll(set2, s2);
        return set1.equals(set2);
    }

    private static Signature[] getInstalledPackageSignatures(Context context, String packageName) {
        Signature[] signatures = null;
        try {
            signatures = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return signatures;
    }

    private static Signature[] getSignaturesFromApk(File file) {
        List<Signature> signatures = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(file);
            JarEntry je = jarFile.getJarEntry("AndroidManifest.xml");
            byte[] readBuffer = new byte[8192];
            Certificate[] certs = loadCertificates(jarFile, je, readBuffer);
            if (certs != null) {
                for (Certificate c : certs) {
                    String strSign = toCharsString(c.getEncoded());
                    Signature signature = new Signature(strSign);
                    signatures.add(signature);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return signatures.toArray(new Signature[0]);
    }

    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        try {
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException ignored) {
        }
        return null;
    }

    private static String toCharsString(byte[] sigBytes) {
        byte[] sig = sigBytes;
        final int N = sig.length;
        final int N2 = N * 2;
        char[] text = new char[N2];
        for (int j = 0; j < N; j++) {
            byte v = sig[j];
            int d = (v >> 4) & 0xf;
            text[j * 2] = (char) (d >= 10 ? ('a' + d - 10) : ('0' + d));
            d = v & 0xf;
            text[j * 2 + 1] = (char) (d >= 10 ? ('a' + d - 10) : ('0' + d));
        }
        return new String(text);
    }

    /**
     * This method only return compiled AndroidManifest code
     */
    public static String getAndroidManifest(String apkPath) {
        try {
            StringBuilder xml = new StringBuilder();
            JarFile jf = new JarFile(apkPath);
            try (InputStream input = jf.getInputStream(jf.getEntry("AndroidManifest.xml"))) {
                InputStreamReader isr = new InputStreamReader(input);
                BufferedReader reader = new BufferedReader(isr);
                int c;
                while ((c = reader.read()) != -1) {
                    xml.append((char) c);
                }
            }
            return xml.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

}