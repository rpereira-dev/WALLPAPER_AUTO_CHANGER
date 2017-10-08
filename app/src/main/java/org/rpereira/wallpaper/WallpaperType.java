package org.rpereira.wallpaper;

import java.util.ArrayList;

/**
 * Created by rpereira on 06/12/15.
 */
public class WallpaperType {
    private final boolean defaultValue;
    private String name;
    private ArrayList<String> urls;
    private boolean use;

    public WallpaperType(String name, boolean default_value, String... urls) {
        this.name = name;
        this.defaultValue = default_value;
        this.urls = new ArrayList<>();
        for (String url : urls) {
            this.urls.add(url);
        }
    }

    public void addUrl(String url) {
        this.urls.add(url);
    }

    public ArrayList<String> getUrls() {
        return (this.urls);
    }

    public String getName() {
        return (this.name);
    }

    public void load() {
        this.use(ResourceManager.getPreferences(this.name + ":use:", this.defaultValue));
    }

    public void save() {
        ResourceManager.putPreferences(this.name + ":use:", this.using());
        ResourceManager.commitPreferences();
    }

    public void use(boolean use) {
        this.use = use;
    }

    public boolean using() {
        return (this.use);
    }
}
