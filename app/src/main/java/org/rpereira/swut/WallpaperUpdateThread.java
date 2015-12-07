package org.rpereira.swut;

/**
 * Created by rpereira on 06/12/15.
 */
public class WallpaperUpdateThread extends Thread implements Runnable
{
	public static long SLEEP_TIME = 1000;

	private boolean _run = false;

	@Override
	public void run()
	{
		while (this._run)
		{
			WallpaperManager.update();

			try
			{
				Logger.get().log(Logger.Level.FINE, "Wallpaper update sleep for: " + SLEEP_TIME / 1000 + " sec");
				Thread.sleep(SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				this.stopRequest();
			}
		}
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
