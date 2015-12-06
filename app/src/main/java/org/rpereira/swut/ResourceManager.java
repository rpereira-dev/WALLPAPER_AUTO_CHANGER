package org.rpereira.swut;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by rpereira on 05/12/15.
 */
public class ResourceManager
{
	private static final Point SIZE = new Point();

	private static String _dirpath = null;
	private static File _savedir = null;
	private static SharedPreferences _shared_preferences = null;
	private static SharedPreferences.Editor _shared_preferences_editor = null;

	public static void start(Activity activity)
	{
		activity.getWindowManager().getDefaultDisplay().getSize(SIZE);
		_dirpath = getFilepath(Environment.getExternalStorageDirectory().toString(), "SWUT");
		_savedir = new File(_dirpath);
		initializeLogger();

		if (!_savedir.exists())
		{
			_savedir.mkdir();
			MainActivity.toast("Created savedir! " + _savedir.exists(), false);
		}
		_shared_preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		_shared_preferences_editor = _shared_preferences.edit();
		WallpaperManager.initialize(activity, getFilepath(_dirpath, "images"));
	}

	public static void initializeLogger()
	{
		try
		{
			File logfile = new File(getFilepath(_dirpath, ".log.txt"));
			if (!logfile.exists())
			{
				logfile.createNewFile();
			}
			Logger.get().setPrintStream(new PrintStream(logfile));
			Logger.get().use(true);
		}
		catch (IOException exception)
		{
			System.err.println("Couldnt iniatialize logger: " + exception.getLocalizedMessage());
			Logger.get().use(false);
		}
	}

	public static void stop()
	{
		commitPreferences();
	}

	/**
	 * return the path for the given file relative to the app resource folder
	 */
	public static String getFilepath(String dirpath, String file)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(dirpath);
		if (!dirpath.endsWith("/") && !file.startsWith("/"))
		{
			builder.append("/");
		}

		builder.append(file);

		return (builder.toString());
	}

	public static void putPreferences(String key, boolean value)
	{
		_shared_preferences_editor.putBoolean(key, value);
	}

	public static void putPreferences(String key, String value)
	{
		_shared_preferences_editor.putString(key, value);
	}

	public static void putPreferences(String key, int value)
	{
		_shared_preferences_editor.putInt(key, value);
	}

	public static void putPreferences(String key, float value)
	{
		_shared_preferences_editor.putFloat(key, value);
	}

	public static void putPreferences(String key, long value)
	{
		_shared_preferences_editor.putLong(key, value);
	}

	public static boolean getPreferences(String key, boolean defvalue)
	{
		return (_shared_preferences.getBoolean(key, defvalue));
	}

	public static int getPreferences(String key, int defvalue)
	{
		return (_shared_preferences.getInt(key, defvalue));
	}

	public static float getPreferences(String key, float defvalue)
	{
		return (_shared_preferences.getFloat(key, defvalue));
	}

	public static long getPreferences(String key, long defvalue)
	{
		return (_shared_preferences.getLong(key, defvalue));
	}

	public static String getPreferences(String key, String defvalue)
	{
		return (_shared_preferences.getString(key, defvalue));
	}

	public static void commitPreferences()
	{
		_shared_preferences_editor.commit();
	}

	public static int getScreenWidth()
	{
		return (SIZE.x);
	}

	public static int getScreenHeight()
	{
		return (SIZE.y);
	}
}
