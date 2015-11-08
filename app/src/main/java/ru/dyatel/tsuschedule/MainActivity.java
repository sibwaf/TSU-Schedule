package ru.dyatel.tsuschedule;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import hirondelle.date4j.DateTime;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.parsing.DateUtil;

import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public static final String PREFERENCES_FILE = "prefs";
    private static final String GROUP_INDEX_KEY = "group_index";
    private static final String SUBGROUP_KEY = "subgroup";

    private DataFragment dataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load saved preferences
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        String groupIndex = preferences.getString(GROUP_INDEX_KEY, "");
        int subgroup = preferences.getInt(SUBGROUP_KEY, 1);

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

        // Set up the navigation drawer
        NavigationDrawerFragment navigationDrawer =
                (NavigationDrawerFragment) fragmentManager.findFragmentById(R.id.navigation_drawer);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawer.initialize(drawerLayout, toolbar, dataFragment, groupIndex, subgroup);

        // Set up the ViewPager with the sections adapter and select current parity tab
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new WeekFragmentPagerAdapter(fragmentManager, dataFragment));
        viewPager.setCurrentItem(
                ParityReference.getIndexFromParity(
                        DateUtil.getWeekParity(DateTime.now(TimeZone.getDefault()))
                )
        );

        // Set up the TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        preferences.edit()
                .putString(GROUP_INDEX_KEY, dataFragment.getGroup())
                .putInt(SUBGROUP_KEY, dataFragment.getSubgroup())
                .apply();

        dataFragment.saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataFragment.clearListeners();
    }

}
