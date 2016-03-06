package ru.dyatel.tsuschedule;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.fragments.MainFragment;
import ru.dyatel.tsuschedule.layout.NavigationDrawerHandler;

public class MainActivity extends AppCompatActivity {

    public static final String PREFERENCES_FILE = "prefs";
    private static final String GROUP_INDEX_KEY = "group_index";
    private static final String SUBGROUP_KEY = "subgroup";

    private DataFragment dataFragment;

    private NavigationHandler navigationHandler;
    private NavigationDrawerHandler drawerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load saved preferences
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        String groupIndex = preferences.getString(GROUP_INDEX_KEY, "");
        int subgroup = preferences.getInt(SUBGROUP_KEY, 1);

        FragmentManager fragmentManager = getSupportFragmentManager();

        // Get the data fragment
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag(DataFragment.TAG);
        if (dataFragment == null) {
            dataFragment = DataFragment.newInstance(groupIndex, subgroup);
            fragmentManager.beginTransaction()
                    .add(dataFragment, DataFragment.TAG)
                    .commit();
        }

        // Replace ActionBar with Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drawerHandler = new NavigationDrawerHandler(
                this, fragmentManager, dataFragment,
                (DrawerLayout) findViewById(R.id.drawer_layout)
        );

        navigationHandler = new NavigationHandler(fragmentManager, drawerHandler);
        fragmentManager.addOnBackStackChangedListener(navigationHandler);

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, new MainFragment())
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerHandler.syncState();
    }

    @Override
    public void onBackPressed() {
        if (!navigationHandler.onBackPressed()) super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return navigationHandler.onOptionsItemSelected(item)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        preferences.edit()
                .putString(GROUP_INDEX_KEY, dataFragment.getGroup())
                .putInt(SUBGROUP_KEY, dataFragment.getSubgroup())
                .apply();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerHandler.onConfigurationChanged(newConfig);
    }

}
