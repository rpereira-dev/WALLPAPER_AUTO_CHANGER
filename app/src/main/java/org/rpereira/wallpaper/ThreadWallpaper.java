package org.rpereira.wallpaper;

import java.util.ArrayList;

/**
 * Created by rpereira on 05/12/15.
 */
public class ThreadWallpaper extends Thread implements Runnable {
    private WallpaperDownloader download;
    private boolean run;

    public ThreadWallpaper(WallpaperDownloader downloader) {
        this.download = downloader;
        this.run = false;
    }

    @Override
    public void run() {
        Logger.get().log(Logger.Level.FINE, "Downloader thread started!");
        MainActivity.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.toast("Downloader thread started.", false);
            }
        });

        ArrayList<WallpaperType> types = WallpaperManager.getTypes();
        while (this.run) {
            for (WallpaperType type : types) {
                if (type.using()) {
                    for (String url : type.getUrls()) {
                        download.bindUrl(url, 0, Integer.MAX_VALUE); //grep every image possible in the URL without redirection
                        while (!download.done()) {
                            download.processImage();
                            if (!this.run) {
                                break;
                            }
                        }

                        if (!this.run) {
                            break;
                        }

                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException exception) {
                            break;
                        }
                    }
                }
            }
        }

        Logger.get().log(Logger.Level.FINE, "Downloader thread stopped!");
        MainActivity.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.toast("Downloader thread stopped.", false);
            }
        });
    }

    public void startRequest() {
        if (this.isAlive() || this.run) {
            return;
        }
        this.start();
        this.run = true;
    }

    public void stopRequest() {
        this.run = false;
    }
}
