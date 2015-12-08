package org.rpereira.swut;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by rpereira on 05/12/15.
 */
public class WallpaperManager
{
	private static android.app.WallpaperManager _manager;
	private static WallpaperDownloader _downloader;
	private static ThreadWallpaper _thrd_download;
	private static boolean _update;

	private static ArrayList<WallpaperType> _types = null;


	public static void initialize(Context context, String dirpath)
	{
		_manager = android.app.WallpaperManager.getInstance(context);
		_downloader = new WallpaperDownloader(dirpath);
		_downloader.start();
		MainActivity.toast(_downloader.getImages().size() + " images were loaded.", false);
		_types = new ArrayList<>();
		loadTypes();
	}

	private static void loadTypes()
	{
		_types.add(new WallpaperType("Nature", true, "http://idesigniphone.com/category/nature"));
		_types.add(new WallpaperType("Space", true, "http://idesigniphone.com/category/space"));
		_types.add(new WallpaperType("Abstract", true, "http://idesigniphone.com/category/abstract"));
		_types.add(new WallpaperType("Animals", true, "http://idesigniphone.com/category/animals"));
		_types.add(new WallpaperType("Comics", true, "http://idesigniphone.com/category/comics"));
		_types.add(new WallpaperType("Food", true, "http://idesigniphone.com/category/food-drinks"));
		_types.add(new WallpaperType("Games", true, "http://idesigniphone.com/category/games"));
		_types.add(new WallpaperType("Paintings", true, "http://idesigniphone.com/category/paintings"));
		_types.add(new WallpaperType("City", true, "http://idesigniphone.com/category/city"));
		_types.add(new WallpaperType("Cultures", true, "http://idesigniphone.com/category/cultures"));
		_types.add(new WallpaperType("Plains", true, "http://idesigniphone.com/category/plains"));

		Collections.sort(_types, new Comparator<WallpaperType>()
		{
			@Override
			public int compare(WallpaperType lhs, WallpaperType rhs)
			{
				return (lhs.getName().compareTo(rhs.getName()));
			}
		});

		for (WallpaperType type : _types)
		{
			String url = type.getUrls().get(0) + "/page/";
			for (int i = 0 ; i < 40 ; i++)
			{
				type.addUrl(url + i);
			}
		}

		for (WallpaperType type : _types)
		{
			type.load();
		}
	}

	/**
	 * start the wallpaper download thread
	 */
	public static void setDownload(boolean download)
	{
		if (download)
		{
			if (_thrd_download != null)
			{
				_thrd_download.stopRequest();
			}
			_thrd_download = new ThreadWallpaper(_downloader);
			_thrd_download.startRequest();
		}
		else
		{
			if (_thrd_download != null)
			{
				_thrd_download.stopRequest();
			}
		}
	}

	/**
	 * remove every downloaded photos and the .images file
	 */
	public static void reset()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance());
		alert.setTitle("Warning");
		alert.setMessage("Are you sure to delete every downloaded images?");
		alert.setPositiveButton("YES", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				_downloader.reset();
				MainActivity.toast("Every images has been removed!", false);
			}
		});

		alert.setNegativeButton("NO", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});

		alert.show();
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

		for (WallpaperType type : _types)
		{
			type.save();
		}
	}

	/** update the wallpaper manager */
	public static boolean update()
	{
		if (!_update)
		{
			return (false);
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
				else
				{
					Logger.get().log(Logger.Level.ERROR, "Couldnt get any random images!");
					MainActivity.toast("Not wallpaper where found!", false);
				}
			}
		});

		return (true);
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
				Logger.get().log(Logger.Level.FINE, "Wallpaper set: " + filepath);
			}
		}
		catch (IOException e)
		{
			MainActivity.toast("An error occurred while setting wallpaper: " + e.getLocalizedMessage(), true);
		}
	}

	public static ArrayList<WallpaperType> getTypes()
	{
		return (_types);
	}


}
