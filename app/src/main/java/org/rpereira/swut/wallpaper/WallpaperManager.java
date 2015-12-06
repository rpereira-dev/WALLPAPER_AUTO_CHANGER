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
	private static ThreadWallpaper _thrd_download;
	private static boolean _update;

	public static void initialize(Context context, String dirpath)
	{
		_manager = android.app.WallpaperManager.getInstance(context);
		_downloader = new WallpaperDownloader(dirpath);
		_downloader.start();
		_thrd_download = new ThreadWallpaper(_downloader);
	}

	/**
	 * start the wallpaper download thread
	 */
	public static void setDownload(boolean download)
	{
		if (download)
		{
			_thrd_download.startRequest();
		}
		else
		{
			_thrd_download.stopRequest();
		}
	}

	/**
	 * start the wallpaper update
	 */
	public static void setUpdate(boolean update)
	{
		_update = update;
	}

	/**
	 * called when the application is stopped
	 */
	public static void destroy()
	{
		setDownload(false);
		setUpdate(false);
		_downloader.stop();
	}

	/** update the wallpaper manager */
	public static void update()
	{
		if (_update == false)
		{
			return ;
		}

		MainActivity.runOnUIThread(new Runnable()
		{
			@Override
			public void run()
			{
				WallpaperImage img = _downloader.getRandomImage();
				if (img != null)
				{
					WallpaperManager.setWallpaper(img.getFilepath());
				}
			}
		});
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
