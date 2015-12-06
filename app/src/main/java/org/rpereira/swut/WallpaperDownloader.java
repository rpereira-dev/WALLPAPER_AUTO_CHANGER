package org.rpereira.swut;

import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WallpaperDownloader
{
	private static final String IMAGES_FILE = ".images";
	private static final String DEFAULT_DST = "./images";

	/**
	 * blacklist of images url with wrong dimension
	 */
	private HashMap<String, WallpaperImage> _images;
	private ArrayList<WallpaperImage> _images_valid;
	private File _dstdir;
	private String _dst;

	private Stack<String> _urls_to_search;

	public WallpaperDownloader(String dst)
	{
		this._images = new HashMap<>();
		this._images_valid = new ArrayList<>();
		this._dst = dst;
		this._dstdir = new File(dst);
		this._urls_to_search = new Stack<>();
	}

	/**
	 * start the downloader (load the blacklist)
	 */
	public void start()
	{
		if (this._dstdir.exists() == false)
		{
			this._dstdir.mkdir();
		}

		File images = new File(this.getLocalPathFor(IMAGES_FILE));
		if (images.exists() == false)
		{
			try
			{
				images.createNewFile();
			}
			catch (IOException e)
			{
				Logger.get().log(Logger.Level.ERROR, "Couldnt create blacklist!", e.getLocalizedMessage());
			}
		}
		else if (images.canRead())
		{
			this.loadImagesSaveFile();
		}
	}

	private void loadImagesSaveFile()
	{
		try
		{
			InputStream fis = new FileInputStream(this.getLocalPathFor(IMAGES_FILE));
			InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(isr);
			String line;
			Logger.get().log(Logger.Level.FINE, "Loading Wallpaper images file...");
			while ((line = br.readLine()) != null)
			{
				String[] urldata = line.split(" ");
				String filepath = this.getLocalPathFor(getImageNameFromUrl(urldata[0]));
				WallpaperImage image = new WallpaperImage(urldata[0], filepath);
				image.setValidity(Boolean.parseBoolean(urldata[1]));
				this._images.put(urldata[0], image);
				if (image.getValidity())
				{
					this._images_valid.add(image);
				}
			}
		}
		catch (Exception e)
		{
			Logger.get().log(Logger.Level.ERROR, "Couldnt read images file!", e.getLocalizedMessage());
		}
	}

	private static String getImageNameFromUrl(String url)
	{
		int begin = url.lastIndexOf("/") + 1;
		if (begin < url.length())
		{
			return (url.substring(begin, url.length()));
		}
		return (null);
	}

	/**
	 * stop the downloader (save the blacklist)
	 */
	public void stop()
	{
		File file = new File(this.getLocalPathFor(IMAGES_FILE));

		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			Set<Entry<String, WallpaperImage>> entries = this._images.entrySet();
			for (Entry<String, WallpaperImage> entry : entries)
			{
				WallpaperImage img = entry.getValue();
				writer.print(img.getUrl());
				writer.print(" ");
				writer.println(img.getValidity());
			}
			writer.close();
		}
		catch (IOException e)
		{
			Logger.get().log(Logger.Level.ERROR, "Error while saving WallpaperDownloader blacklist", e.getLocalizedMessage());
		}
	}

	/**
	 * return local path for the given file name
	 */
	private String getLocalPathFor(String name)
	{
		return (this._dst + (this._dst.endsWith("/") ? "" : "/") + name);
	}

	public void bindUrl(String url, int deepness, int imgcount)
	{
		this._urls_to_search.clear();
		Logger.get().log(Logger.Level.DEBUG, "Bound url", url, deepness, imgcount);
		this.searchWallpapers(url, deepness, imgcount);
	}

	/**
	 * download every images on the given website url, and do it recursively on pages links
	 */
	private int searchWallpapers(String url, int deepness, int imgcount)
	{
		Logger.get().log(Logger.Level.DEBUG, url, deepness, imgcount);

		if (imgcount <= 0)
		{
			return (imgcount);
		}

		Document doc = this.getHtml(url);
		if (doc == null)
		{
			return (imgcount);
		}

		imgcount = this.searchWallpapers(doc, imgcount);

		if (deepness == 0)
		{
			return (imgcount);
		}

		Elements as = doc.select("a");
		for (Element a : as)
		{
			if (imgcount <= 0)
			{
				return (imgcount);
			}
			String href = a.attr("href");
			if (href == null)
			{
				continue;
			}
			imgcount = this.searchWallpapers(href, deepness - 1, imgcount);
		}
		return (imgcount);
	}

	/**
	 * download every images on the given website url
	 */
	private int searchWallpapers(Document doc, int imgcount)
	{
		Elements images = doc.select("img");
		for (Element image : images)
		{
			String url = image.attr("src");
			url = this.fixUrl(url);
			this._urls_to_search.add(url);
			--imgcount;
			if (imgcount <= 0)
			{
				imgcount = 0;
				break;
			}
		}
		return (imgcount);
	}

	public boolean done()
	{
		return (this._urls_to_search.size() == 0);
	}

	/** download an image and return true if the image as been downloaded successfully */
	public boolean processImage()
	{
		boolean r = false;

		String url = this._urls_to_search.pop();
		WallpaperImage img = this._images.get(url);

		Logger.get().log(Logger.Level.FINE, "Processing image: " + url);
		Logger.get().indent(1);

		if (img != null && img.getValidity() == false)
		{
			Logger.get().log(Logger.Level.FINE, "Blacklisted file!");
		}
		else if (img == null)
		{
			String filepath = this.getLocalPathFor(getImageNameFromUrl(url));
			img = new WallpaperImage(url, filepath);
			img.setValidity(img.isExtensionValid());
			if (img.getValidity() == true && img.download())
			{
				img.setValidity(img.isFormatValid());
				if (img.getValidity() == false)
				{
					img.delete();
					Logger.get().log(Logger.Level.FINE, "Image wasnt valid, removing: " + img.getFilepath());
				}
				else
				{
					Logger.get().log(Logger.Level.FINE, "Image is valid! " + img.getFilepath());
					this._images_valid.add(img);
					r = true;
				}
			}
			this._images.put(url, img);
		}
		Logger.get().indent(-1);
		return (r);
	}

	public WallpaperImage getRandomImage()
	{
		if (this._images_valid.size() == 0)
		{
			return (null);
		}
		int index = (int)(System.currentTimeMillis() % this._images_valid.size());
		return (this._images_valid.get(index));
	}

	private String fixUrl(String url)
	{
		if (url.startsWith("//"))
		{
			url = "http:" + url;
		}

		int index = url.lastIndexOf("?");
		if (index > 0)
		{
			url = url.substring(0, index - 1);
		}
		return (url);
	}

	public Document getHtml(String url)
	{
		try
		{
			Connection con = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(10000);
			Connection.Response resp = con.execute();
			if (resp.statusCode() == 200)
			{
				return (con.get());
			}
			return (null);
		}
		catch (Exception e)
		{
			Logger.get().log(Logger.Level.FINE, "Exception while downloading images from url: " + url + " : " + e.getLocalizedMessage());
			return (null);
		}
	}

	/**
	 * return output directory
	 */
	public String getDestination()
	{
		return (this._dst);
	}

	/**
	 * test if image should be blacklisted
	 */
	interface WallpaperBlacklistCondition
	{
		/**
		 * return true if the image should be blacklisted
		 */
		public boolean testImage(File file);

		public String getName();
	}

	/**
	 * remove the blacklist and every images
	 */
	public void reset()
	{
		this._images.clear();
		this._images_valid.clear();

		File dir = new File(this._dst);
		if (dir.exists() == false)
		{
			return;
		}

		for (File file : dir.listFiles())
		{
			file.delete();
		}
	}
}

class WallpaperImage
{
	private static final String[] EXTENSIONS_ALLOWED = {"png", "jpg", "jpeg", "PNG", "JPG", "JPEG"};

	private static final int MIN_WIDTH = 250;
	private static final int MIN_HEIGHT = 250;

	private String _url;
	private String _filepath;
	private File _file;
	private boolean _is_valid;

	public WallpaperImage(String url, String filepath)
	{
		this._url = url;
		this._filepath = filepath;
		this._file = new File(filepath);
	}

	public String getFilepath()
	{
		return (this._filepath);
	}

	/** return true if the image has been downloaded, false elseway */
	public boolean download()
	{
		if (this.exists())
		{
			Logger.get().log(Logger.Level.FINE, "File already exists, not redownloading!", this._filepath);
			return (false);
		}
		else
		{
			Logger.get().log(Logger.Level.FINE, "Downloading image", this._url, "to", this._filepath);
		}

		try
		{
			URL url = new URL(this._url);
			InputStream in = url.openStream();

			OutputStream out = new BufferedOutputStream(new FileOutputStream(this._filepath));

			byte[] buffer = new byte[4096];
			int r = 0;
			while ((r = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, r);
			}
			out.close();
			in.close();
		}
		catch (IOException e)
		{
			return (false);
		}
		return (true);
	}

	public boolean getValidity()
	{
		return (this._is_valid);
	}

	public void setValidity(boolean validity)
	{
		this._is_valid = validity;
	}

	public String getUrl()
	{
		return (this._url);
	}

	public boolean exists()
	{
		return (this._file.exists());
	}

	public boolean isFormatValid()
	{
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(this._filepath, options);
			int width = options.outWidth;
			int height = options.outHeight;

			if (width < MIN_WIDTH || height < MIN_HEIGHT)
			{
				return (false);
			}
		}
		catch (Exception e)
		{
			return (false);
		}
		return (true);
	}

	public String getExtension()
	{
		int idx = this._url.lastIndexOf(".");
		if (idx > 0)
		{
			return (this._url.substring(idx + 1, this._url.length()));
		}
		return (this._url);
	}

	public boolean isExtensionValid()
	{
		String extension = this.getExtension();

		for (String allowed : EXTENSIONS_ALLOWED)
		{
			if (extension.equals(allowed))
			{
				return (true);
			}
		}
		return (false);
	}

	public void delete()
	{
		this._file.delete();
	}
}
