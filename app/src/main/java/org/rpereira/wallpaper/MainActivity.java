package org.rpereira.wallpaper;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /**
     * app instance
     */
    private static MainActivity instance;
    private static WallpaperUpdateThread thrd;
    private static Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thrd.stopRequest();

        WallpaperManager.destroy();
        ResourceManager.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");

        return (true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.dialog.show();
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * initialize the application
     */
    private void initialize() {
        if (instance == this) {
            return;
        }

        instance = this;
        thrd = new WallpaperUpdateThread();

        ResourceManager.start(this);

        final ToggleButton updatebutton = (ToggleButton) findViewById(R.id.toggleButtonWallpaperUpdate);
        updatebutton.setChecked(false);
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperManager.setUpdate(updatebutton.isChecked());
            }
        });


        final ToggleButton downloadbutton = (ToggleButton) findViewById(R.id.toggleButtonWallpaperDownload);
        downloadbutton.setChecked(false);
        downloadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = false;
                for (WallpaperType type : WallpaperManager.getTypes()) {
                    if (type.using()) {
                        b = true;
                        break;
                    }
                }
                if (b) {
                    WallpaperManager.setDownload(downloadbutton.isChecked());
                } else {
                    MainActivity.toast("No Wallpaper types are choosen, nothing to be downloaded.", false);
                    downloadbutton.setChecked(false);
                }
            }
        });

        thrd.startRequest();
        this.initializeDialog();
    }

    private void initializeDialog() {
        this.dialog = new Dialog(this);
        this.dialog.setTitle("Wallpaper settings");
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(new FrameLayout.LayoutParams(params));
        layout.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        layout.setBackgroundColor(Color.TRANSPARENT);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Spinner spinner = new Spinner(this);
        ArrayList<SpinnerTimeValue> choices = new ArrayList<>();
        choices.add(new SpinnerTimeValue(5, "5 sec"));
        choices.add(new SpinnerTimeValue(10, "10 sec"));
        choices.add(new SpinnerTimeValue(15, "15 sec"));
        choices.add(new SpinnerTimeValue(30, "30 sec"));
        choices.add(new SpinnerTimeValue(60, "1 min"));
        choices.add(new SpinnerTimeValue(60 * 2, "2 min"));
        choices.add(new SpinnerTimeValue(60 * 5, "5 min"));
        choices.add(new SpinnerTimeValue(60 * 15, "15 min"));
        choices.add(new SpinnerTimeValue(60 * 30, "30 min"));
        choices.add(new SpinnerTimeValue(60 * 60, "1 h"));
        choices.add(new SpinnerTimeValue(60 * 60 * 2, "2 h"));
        choices.add(new SpinnerTimeValue(60 * 60 * 4, "4 h"));
        choices.add(new SpinnerTimeValue(60 * 60 * 4, "4 h"));
        choices.add(new SpinnerTimeValue(60 * 60 * 12, "12 h"));
        choices.add(new SpinnerTimeValue(60 * 60 * 24, "24 h"));
        ArrayAdapter<SpinnerTimeValue> spinnerArrayAdapter = new ArrayAdapter<SpinnerTimeValue>(this, android.R.layout.simple_spinner_dropdown_item, choices);
        spinner.setAdapter(spinnerArrayAdapter);
        layout.addView(spinner);
        int index = ResourceManager.getPreferences("timer", 0);
        WallpaperUpdateThread.SLEEP_TIME = (long) (choices.get(index).getMillis());
        spinner.setSelection(index);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerTimeValue timeset = (SpinnerTimeValue) spinner.getSelectedItem();
                ResourceManager.putPreferences("timer", position);
                Logger.get().log(Logger.Level.DEBUG, position);
                ResourceManager.commitPreferences();
                WallpaperUpdateThread.SLEEP_TIME = timeset.getMillis();
                toast("Time between 2 wallpapers was set to: " + timeset.toString(), false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

        for (final WallpaperType type : WallpaperManager.getTypes()) {
            SpannableStringBuilder sb = new SpannableStringBuilder(type.getName());
            sb.setSpan(bss, 0, type.getName().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            final CheckBox checkbox = new CheckBox(this);
            checkbox.setText(sb);
            checkbox.setChecked(type.using());
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    type.use(checkbox.isChecked());
                    type.save();
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
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperManager.reset();
            }
        });
        layout.addView(reset);

        this.dialog.addContentView(layout, params);
    }

    /**
     * show a toast message
     *
     * @param text     : string to toast
     * @param duration : true if long length should be used, false if short length want to be use
     */
    public static void toast(final String text, final boolean duration) {
        Toast.makeText(instance.getApplicationContext(), text,
                duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void runOnUIThread(Runnable runnable) {
        instance.runOnUiThread(runnable);
    }

    public static MainActivity instance() {
        return (instance);
    }
}

class SpinnerTimeValue {
    private int _sec;
    private String _str;

    public SpinnerTimeValue(int sec, String str) {
        this._sec = sec;
        this._str = str;
    }

    @Override
    public String toString() {
        return (this._str);
    }

    public int getSeconds() {
        return (this._sec);
    }

    public int getMillis() {
        return (this._sec * 1000);
    }
}
