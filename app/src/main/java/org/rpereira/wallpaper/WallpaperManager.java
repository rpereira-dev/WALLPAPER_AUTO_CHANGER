package org.rpereira.wallpaper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by rpereira on 05/12/15.
 */
public class WallpaperManager {
    private static android.app.WallpaperManager manager;
    private static WallpaperDownloader downloader;
    private static ThreadWallpaper thrd_downloader;
    private static boolean _update;

    private static ArrayList<WallpaperType> types = null;


    public static void initialize(Context context, String dirpath) {
        manager = android.app.WallpaperManager.getInstance(context);
        downloader = new WallpaperDownloader(dirpath);
        downloader.start();
        MainActivity.toast(downloader.getImages().size() + " images were loaded.", false);
        types = new ArrayList<>();
        loadTypes();
    }

    private static void loadTypes() {
        types.add(new WallpaperType("Nature", true, "http://idesigniphone.com/category/nature"));
        types.add(new WallpaperType("Space", true, "http://idesigniphone.com/category/space"));
        types.add(new WallpaperType("Abstract", true, "http://idesigniphone.com/category/abstract"));
        types.add(new WallpaperType("Animals", true, "http://idesigniphone.com/category/animals"));
        types.add(new WallpaperType("Comics", true, "http://idesigniphone.com/category/comics"));
        types.add(new WallpaperType("Food", true, "http://idesigniphone.com/category/food-drinks"));
        types.add(new WallpaperType("Games", true, "http://idesigniphone.com/category/games"));
        types.add(new WallpaperType("Paintings", true, "http://idesigniphone.com/category/paintings"));
        types.add(new WallpaperType("City", true, "http://idesigniphone.com/category/city"));
        types.add(new WallpaperType("Cultures", true, "http://idesigniphone.com/category/cultures"));
        types.add(new WallpaperType("Plains", true, "http://idesigniphone.com/category/plains"));

        Collections.sort(types, new Comparator<WallpaperType>() {
            @Override
            public int compare(WallpaperType lhs, WallpaperType rhs) {
                return (lhs.getName().compareTo(rhs.getName()));
            }
        });

        for (WallpaperType type : types) {
            String url = type.getUrls().get(0) + "/page/";
            for (int i = 0; i < 40; i++) {
                type.addUrl(url + i);
            }
        }

        for (WallpaperType type : types) {
            type.load();
        }
    }

    /**
     * start the wallpaper download thread
     */
    public static void setDownload(boolean download) {
        if (download) {
            if (thrd_downloader != null) {
                thrd_downloader.stopRequest();
            }
            thrd_downloader = new ThreadWallpaper(downloader);
            thrd_downloader.startRequest();
        } else {
            if (thrd_downloader != null) {
                thrd_downloader.stopRequest();
            }
        }
    }

    /**
     * remove every downloaded photos and the .images file
     */
    public static void reset() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance());
        alert.setTitle("Warning");
        alert.setMessage("Are you sure to delete every downloaded images?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloader.reset();
                MainActivity.toast("Every images has been removed!", false);
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    /**
     * start the wallpaper update
     */
    public static void setUpdate(boolean update) {
        _update = update;
    }

    /**
     * called when the application is stopped
     */
    public static void destroy() {
        setDownload(false);
        setUpdate(false);
        downloader.stop();

        for (WallpaperType type : types) {
            type.save();
        }
    }

    /**
     * update the wallpaper manager
     */
    public static boolean update() {
        if (!_update) {
            return (false);
        }

        MainActivity.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                WallpaperImage img = downloader.getRandomImage();
                if (img != null) {
                    WallpaperManager.setWallpaper(img.getFilepath());
                } else {
                    Logger.get().log(Logger.Level.ERROR, "Couldnt get any random images!");
                    MainActivity.toast("Not wallpaper where found!", false);
                }
            }
        });

        return (true);
    }

    /**
     * Set the current wallpaper
     *
     * @param filepath : wallpaper to set
     */
    public static void setWallpaper(final String filepath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            if (bitmap == null) {
                MainActivity.toast("Error while setting wallpaper! " + filepath, false);
            } else {
                manager.setBitmap(bitmap);
                RelativeLayout layout = (RelativeLayout) MainActivity.instance().findViewById(R.id.content_layout);
                if (MainActivity.instance().hasWindowFocus()
                        && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    layout.setBackground(Drawable.createFromPath(filepath));
                }
                Logger.get().log(Logger.Level.FINE, "Wallpaper set: " + filepath);
            }
        } catch (IOException e) {
            MainActivity.toast("An error occurred while setting wallpaper: " + e.getLocalizedMessage(), true);
        }
    }

    public static ArrayList<WallpaperType> getTypes() {
        return (types);
    }


}
