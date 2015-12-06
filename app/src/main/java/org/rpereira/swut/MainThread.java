package org.rpereira.swut;

import org.rpereira.swut.wallpaper.WallpaperManager;

/**
 * Created by rpereira on 06/12/15.
 */
public class MainThread extends Thread implements Runnable
{
	private static final long WALLPAPER_AUTO_UPDATE_TIMER = 1000 * 30; //30 sec

	private boolean _run = false;

	@Override
	public void run()
	{
		while (this._run)
		{
			WallpaperManager.update();

			try
			{
				Thread.sleep(WALLPAPER_AUTO_UPDATE_TIMER);
			}
			catch (InterruptedException e)
			{
				this.stopRequest();
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
