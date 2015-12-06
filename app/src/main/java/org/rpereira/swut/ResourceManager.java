package org.rpereira.swut;

import android.app.Activity;
import android.graphics.Point;
import android.os.Environment;

import org.rpereira.swut.wallpaper.WallpaperManager;

import java.io.File;

/**
 * Created by rpereira on 05/12/15.
 */
public class ResourceManager
{
	private static final Point SIZE = new Point();

	private static String _dirpath = null;
	private static File _savedir = null;

	public static void start(Activity activity)
	{
		activity.getWindowManager().getDefaultDisplay().getSize(SIZE);
		_dirpath = getFilepath(Environment.getExternalStorageDirectory().toString(), "SWUT");
		_savedir = new File(_dirpath);
		if (_savedir.exists() == false)
		{
			_savedir.mkdir();
			MainActivity.toast("Created savedir! " + _savedir.exists(), false);
		}
		WallpaperManager.initialize(activity, getFilepath(_dirpath, "images"));
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
}
