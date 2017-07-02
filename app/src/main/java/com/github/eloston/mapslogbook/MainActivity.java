package com.github.eloston.mapslogbook;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerViewEmptySupport recyclerView;
    private ArrayList<Entry> entriesArrayList;
    private CoordinatorLayout coordLayout;
    public static final String ENTRY_EXTRA = "com.github.eloston.mapslogbook.MainActivity.entry";
    public static final String ENTRY_NEW_EXTRA = "com.github.eloston.mapslogbook.MainActivity.entry_new";
    private BasicListAdapter adapter;
    public static final int REQUEST_ID_ENTRY = 100;
    public static final String FILENAME = "entries.json";
    private StoreRetrieveData storeRetrieveData;
    private CustomRecyclerScrollViewListener customRecyclerScrollViewListener;
    public static final String SHARED_PREF_DATA_SET_CHANGED = "com.github.eloston.datasetchanged";
    public static final String CHANGE_OCCURED = "com.github.eloston.changeoccured";
    public static final String THEME_PREFERENCES = "com.github.eloston.themepref";
    public static final String RECREATE_ACTIVITY = "com.github.eloston.recreateactivity";
    public static final String THEME_SAVED = "com.github.eloston.savedtheme";
    public static final String DARKTHEME = "com.github.eloston.darktheme";
    public static final String LIGHTTHEME = "com.github.eloston.lighttheme";

    public static ArrayList<Entry> getLocallyStoredData(StoreRetrieveData storeRetrieveData){
        ArrayList<Entry> entries = null;

        try {
            entries  = storeRetrieveData.loadFromFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (entries == null) {
            entries = new ArrayList<>();
        }
        return entries;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = this.getSharedPreferences(this.SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        /*
        We need to do this, as this activity's onCreate won't be called when coming back from SettingsActivity,
        thus our changes to dark/light mode won't take place, as the setContentView() is not called again.
        So, inside our SettingsFragment, whenever the checkbox's value is changed, in our shared preferences,
        we mark our recreate_activity key as true.

        Note: the recreate_key's value is changed to false before calling recreate(), or we woudl have ended up in an infinite loop,
        as onResume() will be called on recreation, which will again call recreate() and so on....
        and get an ANR

         */
        if (this.getSharedPreferences(this.THEME_PREFERENCES, MODE_PRIVATE).getBoolean(this.RECREATE_ACTIVITY, false)) {
            SharedPreferences.Editor editor = this.getSharedPreferences(this.THEME_PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(this.RECREATE_ACTIVITY, false);
            editor.apply();
            this.recreate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(this.CHANGE_OCCURED, false)) {
            this.entriesArrayList = getLocallyStoredData(this.storeRetrieveData);
            this.adapter = new BasicListAdapter(this, this.entriesArrayList, this.coordLayout);
            this.recyclerView.setAdapter(this.adapter);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(this.CHANGE_OCCURED, false);
            editor.apply();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        //We recover the theme we've set and setTheme accordingly
        String theme = this.getSharedPreferences(this.THEME_PREFERENCES, MODE_PRIVATE).getString(this.THEME_SAVED, this.LIGHTTHEME);

        if (theme.equals(this.LIGHTTHEME)) {
            this.setTheme(R.style.CustomStyle_LightTheme);
        }
        else {
            this.setTheme(R.style.CustomStyle_DarkTheme);
        }

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getSharedPreferences(this.SHARED_PREF_DATA_SET_CHANGED, this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(this.CHANGE_OCCURED, false);
        editor.apply();

        this.storeRetrieveData = new StoreRetrieveData(this, this.FILENAME);
        this.entriesArrayList =  getLocallyStoredData(this.storeRetrieveData);

        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        this.coordLayout = (CoordinatorLayout)findViewById(R.id.myCoordinatorLayout);
        final FloatingActionButton addEntryFAB = (FloatingActionButton)findViewById(R.id.addEntryFAB);

        addEntryFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newEntry = new Intent(MainActivity.this, AddEntryActivity.class);
                Entry entry = new Entry();
                int color = ColorGenerator.MATERIAL.getRandomColor();
                entry.setColor(color);
                newEntry.putExtra(MainActivity.this.ENTRY_EXTRA, entry);
                newEntry.putExtra(MainActivity.this.ENTRY_NEW_EXTRA, true);
                MainActivity.this.startActivityForResult(newEntry, MainActivity.this.REQUEST_ID_ENTRY);
            }
        });

        this.recyclerView = (RecyclerViewEmptySupport)findViewById(R.id.entryRecyclerView);
        if (theme.equals(LIGHTTHEME)) {
            this.recyclerView.setBackgroundColor(this.getResources().getColor(R.color.primary_lightest));
        }
        this.recyclerView.setEmptyView(findViewById(R.id.toDoEmptyView));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.customRecyclerScrollViewListener = new CustomRecyclerScrollViewListener() {
            @Override
            public void show() {

                addEntryFAB.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)addEntryFAB.getLayoutParams();
                int fabMargin = lp.bottomMargin;
                addEntryFAB.animate().translationY(addEntryFAB.getHeight()+fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }
        };
        this.recyclerView.addOnScrollListener(this.customRecyclerScrollViewListener);

        this.adapter = new BasicListAdapter(this, this.entriesArrayList, this.coordLayout);

        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(this.adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(this.recyclerView);

        this.recyclerView.setAdapter(this.adapter);



    }

    public void addThemeToSharedPreferences(String theme) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(this.THEME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(this.THEME_SAVED, theme);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.aboutMeMenuItem:
                intent = new Intent(this, AboutActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.preferences:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED && requestCode == this.REQUEST_ID_ENTRY) {
            Entry entry = (Entry)data.getSerializableExtra(this.ENTRY_EXTRA);
            if (entry.getName().length() <= 0) {
                return;
            }

            boolean existed = false;
            for (int i = 0; i < this.entriesArrayList.size(); i++) {
                if (entry.getIdentifier().equals(this.entriesArrayList.get(i).getIdentifier())) {
                    this.entriesArrayList.set(i, entry);
                    existed = true;
                    this.adapter.notifyDataSetChanged();
                    break;
                }
            }
            if (!existed) {
                addToDataStore(entry);
            }
        }
    }

    private boolean doesPendingIntentExist(Intent i, int requestCode) {
        PendingIntent pi = PendingIntent.getService(this, requestCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private void addToDataStore(Entry entry) {
        this.entriesArrayList.add(entry);
        this.adapter.notifyItemInserted(this.entriesArrayList.size() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            this.storeRetrieveData.saveToFile(this.entriesArrayList);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.recyclerView.removeOnScrollListener(this.customRecyclerScrollViewListener);
    }
}


