package org.rpereira.swut;

import android.app.ActionBar;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
	/** app instance */
	private static MainActivity _instance;

	private static WallpaperUpdateThread _thrd;

	private static Dialog _dialog = null;

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
		ResourceManager.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("Settings");

		return (true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		this._dialog.show();
		return (super.onOptionsItemSelected(item));
	}

	/**
	 * initialize the application
	 */
	private void initialize()
	{
		_instance = this;
		_thrd = new WallpaperUpdateThread();

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
				boolean b = false;
				for (WallpaperType type : WallpaperManager.getTypes())
				{
					if (type.using())
					{
						b = true;
						break ;
					}
				}
				if (b)
				{
					WallpaperManager.setDownload(downloadbutton.isChecked());
				}
				else
				{
					MainActivity.toast("No Wallpaper types are choosen, nothing to be downloaded.", false);
					downloadbutton.setChecked(false);
				}
			}
		});

		_thrd.startRequest();
		this.initializeDialog();
	}

	private void initializeDialog()
	{
		this._dialog = new Dialog(this);
		this._dialog.setTitle("Wallpaper settings");
		LinearLayout layout = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(new FrameLayout.LayoutParams(params));
		layout.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		layout.setBackgroundColor(Color.TRANSPARENT);
		layout.setOrientation(LinearLayout.VERTICAL);

		final Spinner spinner = new Spinner(this);
		ArrayList<Integer> choices = new ArrayList<>();
		choices.add(5);
		choices.add(10);
		choices.add(15);
		choices.add(30);
		choices.add(60 * 1);
		choices.add(60 * 2);
		choices.add(60 * 5);
		choices.add(60 * 15);
		choices.add(60 * 30);
		choices.add(60 * 60);
		ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, choices);
		spinner.setAdapter(spinnerArrayAdapter);
		layout.addView(spinner);
		int value = ResourceManager.getPreferences("timer", 0);
		WallpaperUpdateThread.SLEEP_TIME = (long)(value * 1000);
		spinner.setSelection(value);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				int valueset = (Integer)spinner.getSelectedItem();
				ResourceManager.putPreferences("timer", position);
				Logger.get().log(Logger.Level.DEBUG, position);
				ResourceManager.commitPreferences();
				WallpaperUpdateThread.SLEEP_TIME = (long)(valueset * 1000);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}

		});

		StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

		for (final WallpaperType type : WallpaperManager.getTypes())
		{
			SpannableStringBuilder sb = new SpannableStringBuilder(type.getName());
			sb.setSpan(bss, 0, type.getName().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

			final CheckBox checkbox = new CheckBox(this);
			checkbox.setText(sb);
			checkbox.setChecked(type.using());
			checkbox.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					type.use(checkbox.isChecked());
				}
			});
			layout.addView(checkbox);
		}


		String text = "Reset";
		StyleSpan iss = new StyleSpan(android.graphics.Typeface.BOLD);
		SpannableStringBuilder sb = new SpannableStringBuilder(text);
		sb.setSpan(iss, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		Button reset = new Button(this);
		reset.setText(sb);
		reset.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				WallpaperManager.reset();
			}
		});
		layout.addView(reset);

		this._dialog.addContentView(layout, params);
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
