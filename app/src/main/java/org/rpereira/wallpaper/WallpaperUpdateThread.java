package org.rpereira.wallpaper;

/**
 * Created by rpereira on 06/12/15.
 */
public class WallpaperUpdateThread extends Thread implements Runnable {
    private static long DEFAULT_SLEEP_TIME = 5000;
    public static long SLEEP_TIME = DEFAULT_SLEEP_TIME;

    private boolean _run = false;

    @Override
    public void run() {
        while (this._run) {
            long sleep = SLEEP_TIME;

            if (WallpaperManager.update() == false) {
                sleep = DEFAULT_SLEEP_TIME;
            }

            try {
                Logger.get().log(Logger.Level.FINE, "Wallpaper update sleep for: " + sleep + " sec");
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                this.stopRequest();
            }
        }
    }

    public void startRequest() {
        if (this.isAlive() || this._run) {
            return;
        }
        this.start();
        this._run = true;
    }

    public void stopRequest() {
        this._run = false;
    }

}
