package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFERENCES_FILE = "prefs";
    private static final String DRAWER_LEARNED_KEY = "drawer_learned";

    private ActionBarDrawerToggle drawerToggle;

    private DataFragment dataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();

        // Get the data fragment
        dataFragment = (DataFragment) fragmentManager.findFragmentByTag(DataFragment.TAG);
        if (dataFragment == null) {
            dataFragment = new DataFragment();
            fragmentManager.beginTransaction().add(dataFragment, DataFragment.TAG).commit();
        }

        // Replace ActionBar with Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        View drawerContent = findViewById(R.id.drawer_content);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.setDrawerListener(drawerToggle);

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new SectionsPagerAdapter(fragmentManager));

        // Set up the TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // Open drawer if user had never seen it
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        boolean seenDrawer = preferences.getBoolean(DRAWER_LEARNED_KEY, false);
        if (!seenDrawer) {
            drawerLayout.openDrawer(drawerContent);
            preferences.edit()
                    .putBoolean(DRAWER_LEARNED_KEY, true)
                    .apply();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataFragment.clearListeners();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SparseBooleanArray initializedFragments = new SparseBooleanArray(2);

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            for (int i = 0; i < 2; i++) {
                initializedFragments.put(i, false);
            }
        }

        @Override
        public Fragment getItem(int position) {
            Parity p = null;
            switch (position) {
                case 0:
                    p = Parity.ODD;
                    break;
                case 1:
                    p = Parity.EVEN;
                    break;
            }

            // Create a fragment and subscribe it to data updates
            WeekFragment fragment = WeekFragment.newInstance(p);
            dataFragment.addListener(fragment);
            initializedFragments.put(position, true);

            // Check if we are ready to load data into fragments
            boolean fragmentsAreReady = true;
            for (int i = 0; i < initializedFragments.size(); i++) {
                if (!initializedFragments.valueAt(i)) {
                    fragmentsAreReady = false;
                    break;
                }
            }
            if (fragmentsAreReady) dataFragment.loadSavedData();

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.odd_week).toUpperCase(l);
                case 1:
                    return getString(R.string.even_week).toUpperCase(l);
            }
            return null;
        }

    }

}
