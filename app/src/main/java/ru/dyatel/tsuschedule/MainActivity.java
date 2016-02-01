package ru.dyatel.tsuschedule;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.fragments.FragmentUtil;
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

        FragmentManager fragmentManager = getFragmentManager();

        // Get the data fragment
        dataFragment = FragmentUtil.getFragment(fragmentManager, DataFragment.TAG, DataFragment.class);
        dataFragment.setGroup(groupIndex);
        dataFragment.setSubgroup(subgroup);

        // Replace ActionBar with Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager.beginTransaction()
                .replace(R.id.content_fragment, new MainFragment())
                .commit();
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
