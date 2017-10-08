package org.rpereira.wallpaper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by rpereira on 05/12/15.
 */
public class ResourceManager {
    private static final Point SIZE = new Point();

    private static String dirpath = null;
    private static File savedir = null;
    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor sharedPreferencesEditor = null;

    public static void start(Activity activity) {
        activity.getWindowManager().getDefaultDisplay().getSize(SIZE);
        dirpath = getFilepath(activity.getFilesDir().getAbsolutePath(), "WALLPAPERS");
        savedir = new File(dirpath);
        initializeLogger();

        if (!savedir.exists()) {
            savedir.mkdirs();
            MainActivity.toast("Created savedir! " + savedir.exists() + " : " + dirpath, false);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferencesEditor = sharedPreferences.edit();
        WallpaperManager.initialize(activity, getFilepath(dirpath, "images"));
    }

    public static void initializeLogger() {
        try {
            File logfile = new File(getFilepath(dirpath, "log.txt"));
            if (!logfile.exists()) {
                logfile.createNewFile();
            }
            Logger.get().setPrintStream(new PrintStream(logfile));
            Logger.get().use(true);
        } catch (IOException exception) {
            System.err.println("Couldnt iniatialize logger: " + exception.getLocalizedMessage());
            Logger.get().use(false);
        }
    }

    public static void stop() {
        commitPreferences();
    }

    /**
     * return the path for the given file relative to the app resource folder
     */
    public static String getFilepath(String dirpath, String file) {
        StringBuilder builder = new StringBuilder();
        builder.append(dirpath);
        if (!dirpath.endsWith("/") && !file.startsWith("/")) {
            builder.append("/");
        }
        builder.append(file);
        return (builder.toString());
    }

    public static void putPreferences(String key, boolean value) {
        sharedPreferencesEditor.putBoolean(key, value);
    }

    public static void putPreferences(String key, String value) {
        sharedPreferencesEditor.putString(key, value);
    }

    public static void putPreferences(String key, int value) {
        sharedPreferencesEditor.putInt(key, value);
    }

    public static void putPreferences(String key, float value) {
        sharedPreferencesEditor.putFloat(key, value);
    }

    public static void putPreferences(String key, long value) {
        sharedPreferencesEditor.putLong(key, value);
    }

    public static boolean getPreferences(String key, boolean defvalue) {
        return (sharedPreferences.getBoolean(key, defvalue));
    }

    public static int getPreferences(String key, int defvalue) {
        return (sharedPreferences.getInt(key, defvalue));
    }

    public static float getPreferences(String key, float defvalue) {
        return (sharedPreferences.getFloat(key, defvalue));
    }

    public static long getPreferences(String key, long defvalue) {
        return (sharedPreferences.getLong(key, defvalue));
    }

    public static String getPreferences(String key, String defvalue) {
        return (sharedPreferences.getString(key, defvalue));
    }

    public static void commitPreferences() {
        sharedPreferencesEditor.commit();
    }

    public static int getScreenWidth() {
        return (SIZE.x);
    }

    public static int getScreenHeight() {
        return (SIZE.y);
    }
}
