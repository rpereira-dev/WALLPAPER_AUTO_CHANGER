package org.rpereira.swut.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.rpereira.swut.MainActivity;
import org.rpereira.swut.ResourceManager;

import java.io.IOException;

/**
 * Created by rpereira on 05/12/15.
 */
public class WallpaperManager
{
	private static android.app.WallpaperManager _manager;
	private static WallpaperDownloader _downloader;
	private static ThreadWallpaper _thrd;

	public static void initialize(Context context, String dirpath)
	{
		_manager = android.app.WallpaperManager.getInstance(context);
		_downloader = new WallpaperDownloader(dirpath);
		_downloader.start();
		_thrd = new ThreadWallpaper(_downloader);
	}

	/**
	 * start the wallpaper services
	 */
	public static void start()
	{
		_thrd.startRequest();
	}

	/**
	 * stop the wallpaper services
	 */
	public static void stop()
	{
		_thrd.stopRequest();
	}

	/**
	 * called when the application is stopped
	 */
	public static void destroy()
	{
		stop();
		_downloader.stop();
	}

	/**
	 * Set the current wallpaper
	 *
	 * @param filepath : wallpaper to set
	 */
	public static void setWallpaper(final String filepath)
	{
		try
		{
			Bitmap bitmap = BitmapFactory.decodeFile(filepath);
			if (bitmap == null)
			{
				MainActivity.toast("Error while setting wallpaper! " + filepath, false);
			}
			else
			{
				_manager.setBitmap(bitmap);
				MainActivity.toast("Wallpaper was set! " + filepath, false);
			}
		}
		catch (IOException e)
		{
			MainActivity.toast("An error occurred while setting wallpaper: " + e.getLocalizedMessage(), true);
		}
	}
}
