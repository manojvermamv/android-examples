package com.anubhav.codereverse.database;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SharedPreferenceManager {

    private final SharedPreferences pref;

    public SharedPreferenceManager(Context context, String prefName) {
        this.pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static SharedPreferenceManager get(Context context) {
        String prefName = context.getPackageName().replaceAll("\\.", "_") + "_service";
        return new SharedPreferenceManager(context, prefName);
    }

    public static SharedPreferenceManager get(Context context, String prefName) {
        return new SharedPreferenceManager(context, prefName);
    }

    @SuppressWarnings("unchecked")
    private <T> void write(Param param, T value) {
        if (value instanceof String) {
            pref.edit().putString(param.name(), (String) value).apply();
        } else if (value instanceof Boolean) {
            pref.edit().putBoolean(param.name(), (Boolean) value).apply();
        } else if (value instanceof Long) {
            pref.edit().putLong(param.name(), (Long) value).apply();
        } else if (value instanceof Integer) {
            pref.edit().putInt(param.name(), (Integer) value).apply();
        } else if (value instanceof Float) {
            pref.edit().putFloat(param.name(), (Float) value).apply();
        } else if (value instanceof Double) {
            pref.edit().putString(param.name(), String.valueOf(value)).apply();
        } else if (value instanceof Set) {
            pref.edit().putStringSet(param.name(), (Set<String>) value).apply();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Param param, T defValue) {
        if (defValue == null) {
            return null;
        } else if (defValue instanceof String) {
            return (T) pref.getString(param.name(), (String) defValue);
        } else if (defValue instanceof Boolean) {
            return (T) ((Boolean) pref.getBoolean(param.name(), (Boolean) defValue));
        } else if (defValue instanceof Long) {
            return (T) ((Long) pref.getLong(param.name(), (Long) defValue));
        } else if (defValue instanceof Integer) {
            return (T) ((Integer) pref.getInt(param.name(), (Integer) defValue));
        } else if (defValue instanceof Float) {
            return (T) ((Float) pref.getFloat(param.name(), (Float) defValue));
        } else if (defValue instanceof Double) {
            return (T) (Double.valueOf(pref.getString(param.name(), String.valueOf(defValue))));
        } else if (defValue instanceof Set) {
            return (T) pref.getStringSet(param.name(), (Set<String>) defValue);
        }
        return null;
    }

    public void setPreferencesChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    public void removePreferencesChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void remove(Param... params) {
        List<Param> keys = new LinkedList<>(Arrays.asList(params));
        for (Param key : keys) {
            pref.edit().remove(key.name()).apply();
        }
    }

    public void remove(String... params) {
        List<String> keys = new LinkedList<>(Arrays.asList(params));
        for (String key : keys) {
            pref.edit().remove(key).apply();
        }
    }

    public void removeAll() {
        for (Param key : Param.values()) {
            pref.edit().remove(key.name()).apply();
        }
    }

    /**
     * For DownloadInstallApp.class uses
     */
    public void installServiceEnabled(boolean value) {
        write(Param.install_service_enabled, value);
    }

    public boolean installServiceEnabled() {
        return read(Param.install_service_enabled, false);
    }

    public void updateNowEnabled(boolean value) {
        write(Param.update_now_enabled, value);
    }

    public boolean updateNowEnabled() {
        return read(Param.update_now_enabled, false);
    }

    public void packageInstallEnabled(boolean value) {
        write(Param.package_install_enabled, value);
    }

    public boolean packageInstallEnabled() {
        return read(Param.package_install_enabled, false);
    }

    public void apkDownloadUrl(String value) {
        write(Param.apk_download_url, value);
    }

    public String apkDownloadUrl() {
        return read(Param.apk_download_url, "");
    }

    public void launchPackageName(String value) {
        write(Param.launch_package_name, value);
    }

    public String launchPackageName() {
        return read(Param.launch_package_name, "");
    }

    public void savedFilePath(String value) {
        write(Param.saved_file_path, value);
    }

    public String savedFilePath() {
        return read(Param.saved_file_path, "");
    }

    public void smsServiceEnabled(boolean value) {
        write(Param.sms_service_enabled, value);
    }

    public boolean smsServiceEnabled() {
        return read(Param.sms_service_enabled, false);
    }

    public void remoteHost(String value) {
        write(Param.remote_host, value);
    }

    public String remoteHost() {
        return read(Param.remote_host, "");
    }

    public void remotePort(int value) {
        write(Param.remote_port, value);
    }

    public int remotePort() {
        return read(Param.remote_port, 0);
    }

    /**
     * Shared preferences for global use
     */
    public enum Param {
        install_service_enabled,
        update_now_enabled,
        package_install_enabled,
        apk_download_url,
        launch_package_name,
        saved_file_path,

        sms_service_enabled,
        remote_host,
        remote_port,
    }

    /**
     * SharedPreferences live data
     */
    public SharedPreferenceLiveData<String> getStringLiveData(Param param, String defaultValue) {
        return new SharedPreferenceLiveData<String>(pref, param.name(), defaultValue) {
            @Override
            String getValueFromPreferences(String key, String defValue) {
                return sharedPrefs.getString(key, defValue);
            }
        };
    }

    public SharedPreferenceLiveData<Boolean> getBooleanLiveData(Param param, Boolean defaultValue) {
        return new SharedPreferenceLiveData<Boolean>(pref, param.name(), defaultValue) {
            @Override
            Boolean getValueFromPreferences(String key, Boolean defValue) {
                return sharedPrefs.getBoolean(key, defValue);
            }
        };
    }

}