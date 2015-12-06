package org.rpereira.swut;

import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by rpereira on 06/12/15.
 */
public class WallpaperType
{
	private final boolean _default_value;
	private String _name;
	private ArrayList<String> _urls;
	private boolean _use;

	public WallpaperType(String name, boolean default_value, String ... urls)
	{
		this._name = name;
		this._default_value = default_value;
		this._urls = new ArrayList<>();
		for (String url : urls)
		{
			this._urls.add(url);
		}
	}

	public void addUrl(String url)
	{
		this._urls.add(url);
	}

	public ArrayList<String> getUrls()
	{
		return (this._urls);
	}

	public String getName()
	{
		return (this._name);
	}

	public void load()
	{
		this.use(ResourceManager.getPreferences(this._name + ":use", this._default_value));
	}

	public void save()
	{
		ResourceManager.putPreferences(this._name + ":use", this.using());
	}

	public void use(boolean use)
	{
		this._use = use;
	}

	public boolean using()
	{
		return (this._use);
	}
}
