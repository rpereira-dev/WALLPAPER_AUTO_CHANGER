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
		Logger.get().log(Logger.Level.FINE, "Downloader thread started!");
		MainActivity.runOnUIThread(new Runnable()
		{
			@Override
			public void run()
			{
				MainActivity.toast("Downloader thread started.", false);
			}
		});

		ArrayList<WallpaperType> types = WallpaperManager.getTypes();
		while (this._run)
		{
			for (WallpaperType type : types)
			{
				if (type.using())
				{
					for (String url : type.getUrls())
					{
						_downloader.bindUrl(url, 0, Integer.MAX_VALUE); //grep every image possible in the URL without redirection
						while (!_downloader.done())
						{
							_downloader.processImage();
							if (this._run == false)
							{
								break ;
							}
						}

						if (this._run == false)
						{
							break ;
						}

						try
						{
							Thread.sleep(250);
						}
						catch (InterruptedException exception)
						{
							break ;
						}

					}
				}
			}
		}

		Logger.get().log(Logger.Level.FINE, "Downloader thread stopped!");
		MainActivity.runOnUIThread(new Runnable()
		{
			@Override
			public void run()
			{
				MainActivity.toast("Downloader thread stopped.", false);
			}
		});
	}

	public void startRequest()
	{
		if (this.isAlive() || this._run)
		{
			return ;
		}
		this.start();
		this._run = true;
	}

	public void stopRequest()
	{
		this._run = false;
	}
}
