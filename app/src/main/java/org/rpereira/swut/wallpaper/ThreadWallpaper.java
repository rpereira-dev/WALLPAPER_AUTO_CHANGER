package org.rpereira.swut.wallpaper;

import org.rpereira.swut.MainActivity;

/**
 * Created by rpereira on 05/12/15.
 */
public class ThreadWallpaper extends Thread implements Runnable
{
	private static final String[] URLS = {
			"http://idesigniphone.com/category/nature",
			"http://idesigniphone.com/category/funny",
			"http://idesigniphone.com/category/vector",
			"http://idesigniphone.com/category/3d",
			"http://www.mobileswall.com/"
	};

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
		while (this._run)
		{
			for (String url : URLS)
			{
				this._downloader.searchWallpapers(url, 1, 10);
			}

			try
			{
				Thread.sleep(10 * 1000);
			}
			catch (InterruptedException e)
			{
				this._run = false;
			}
		}
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
