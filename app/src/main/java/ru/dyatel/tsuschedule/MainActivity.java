package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DataFragment.Listener {

    private static final String FRAGMENT_DATA = "data";

    private static final String DRAWER_LEARNED_KEY = "drawer_learned";

    private ActionBarDrawerToggle drawerToggle;

    private SparseArray<WeekFragment> weekFragments = new SparseArray<WeekFragment>();
    private DataFragment dataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        FragmentManager fragmentManager = getFragmentManager();

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new SectionsPagerAdapter(fragmentManager));

        // Get the data fragment
        if (fragmentManager.findFragmentByTag(FRAGMENT_DATA) == null) {
            dataFragment = new DataFragment();
            fragmentManager.beginTransaction().add(dataFragment, FRAGMENT_DATA).commit();
            dataFragment.setListener(this);
        }

        // Open drawer if user had never seen it
        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean seenDrawer = preferences.getBoolean(DRAWER_LEARNED_KEY, false);
        if (!seenDrawer) {
            drawerLayout.openDrawer(drawerContent);
            preferences.edit()
                    .putBoolean(DRAWER_LEARNED_KEY, true)
                    .apply();
        }
    }

    @Override
    public void onDataUpdate(Set<Lesson> lessons) {
        for (int i = 0; i < weekFragments.size(); i++) {
            weekFragments.get(weekFragments.keyAt(i)).updateData(lessons);
        }
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
            dataFragment.loadLessons("221251"); // TODO: replace with normal data refreshing
            return true;
        }

        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
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

            // Create fragment and remember it for future data updating
            WeekFragment fragment = WeekFragment.newInstance(p);
            weekFragments.put(position, fragment);
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
