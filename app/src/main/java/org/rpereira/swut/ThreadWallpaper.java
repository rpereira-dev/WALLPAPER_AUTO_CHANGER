package org.rpereira.swut;

import java.util.ArrayList;

/**
 * Created by rpereira on 05/12/15.
 */
public class ThreadWallpaper extends Thread implements Runnable
{
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
		ArrayList<WallpaperType> types = WallpaperManager.getTypes();
		while (this._run)
		{
			for (WallpaperType type : types)
			{
				if (type.using())
				{
					for (String url : type.getUrls())
					{
						_downloader.searchWallpapers(url, 0, 5); //search 100 images in the page at most, and follow 0 redirection

						try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException exception)
						{
							break ;
						}
					}
				}
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
