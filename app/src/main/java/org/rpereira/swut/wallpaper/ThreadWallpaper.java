package org.rpereira.swut.wallpaper;

import org.rpereira.swut.MainActivity;

/**
 * Created by rpereira on 05/12/15.
 */
public class ThreadWallpaper extends Thread implements Runnable
{
	private static final int UPDATE_TIME = 1000 * 10;

	private WallpaperDownloader _downloader;
	private boolean _run;

	public ThreadWallpaper(WallpaperDownloader downloader)
	{
		this._downloader = downloader;
		this._run = false;
	}

	@Override
	public void run()
	{
		Runnable toastrun = new Runnable()
		{
			@Override
			public void run()
			{
				MainActivity.toast("Update thread!", false);
				WallpaperImage img = _downloader.getRandomImage();
				if (img != null)
				{
					WallpaperManager.setWallpaper(img.getFilepath());
				}
			}
		};
		while (true)
		{
			MainActivity.runOnUIThread(toastrun);

			this._downloader.searchWallpapers("http://www.mobileswall.com/", 1, 5);

			try
			{
				Thread.sleep(UPDATE_TIME);
			}
			catch (InterruptedException exception)
			{
				break ;
			}

			if (this._run == false)
			{
				break ;
			}
		}
		this._run = false;
	}

	public void startRequest()
	{
		this._run = true;

		if (!this.isAlive())
		{
			this.start();
		}
	}

	public void stopRequest()
	{
		this._run = false;
	}
}
