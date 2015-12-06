package org.rpereira.swut;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.rpereira.swut.wallpaper.WallpaperManager;

public class MainActivity extends AppCompatActivity
{
	/** app instance */
	private static MainActivity _instance;

	private static MainThread _thrd;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(org.rpereira.swut.R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(org.rpereira.swut.R.id.toolbar);
		setSupportActionBar(toolbar);
		this.initialize();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		_thrd.stopRequest();
		WallpaperManager.destroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(org.rpereira.swut.R.menu.menu_main, menu);
		return (true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == org.rpereira.swut.R.id.action_settings)
		{
			return (true);
		}

		return (super.onOptionsItemSelected(item));
	}

	/**
	 * initialize the application
	 */
	private void initialize()
	{
		_instance = this;
		_thrd = new MainThread();

		ResourceManager.start(this);

		final ToggleButton updatebutton = (ToggleButton) findViewById(R.id.toggleButtonWallpaperUpdate);
		updatebutton.setChecked(false);
		updatebutton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				WallpaperManager.setUpdate(updatebutton.isChecked());
			}
		});


		final ToggleButton downloadbutton = (ToggleButton) findViewById(R.id.toggleButtonWallpaperDownload);
		downloadbutton.setChecked(false);
		downloadbutton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				WallpaperManager.setDownload(downloadbutton.isChecked());
			}
		});

		_thrd.startRequest();

	}

	/**
	 * show a toast message
	 *
	 * @param text     : string to toast
	 * @param duration : true if long length should be used, false if short length want to be use
	 */
	public static void toast(final String text, final boolean duration)
	{
		Toast.makeText(_instance.getApplicationContext(), text,
				duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	public static void runOnUIThread(Runnable runnable)
	{
		_instance.runOnUiThread(runnable);
	}

	public static MainActivity instance()
	{
		return (_instance);
	}
}
