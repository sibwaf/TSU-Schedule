package ru.dyatel.tsuschedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.fragments.MainFragment;

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

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, new MainFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) super.onBackPressed();
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

}
