package ru.dyatel.tsuschedule;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NavigationDrawerFragment extends Fragment {

    private static final String DRAWER_LEARNED_KEY = "drawer_learned";

    private ActionBarDrawerToggle toggle;

    public NavigationDrawerFragment() {
    }

    public void initialize(DrawerLayout drawerLayout, Toolbar toolbar) {
        Activity activity = getActivity();

        toggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.setDrawerListener(toggle);

        // Open drawer if user had never seen it
        SharedPreferences preferences =
                activity.getSharedPreferences(MainActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
        boolean seenDrawer = preferences.getBoolean(DRAWER_LEARNED_KEY, false);
        if (!seenDrawer) {
            drawerLayout.openDrawer(drawerLayout.findViewById(R.id.drawer_content));
            preferences.edit()
                    .putBoolean(DRAWER_LEARNED_KEY, true)
                    .apply();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_drawer_layout, container, false);


        return root;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

}
