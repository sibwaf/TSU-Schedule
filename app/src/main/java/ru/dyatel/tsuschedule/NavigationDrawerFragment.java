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
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import hirondelle.date4j.DateTime;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.parsing.DateUtil;

import java.util.TimeZone;

public class NavigationDrawerFragment extends Fragment {

    private static final String DRAWER_LEARNED_KEY = "drawer_learned";

    private ActionBarDrawerToggle toggle;

    private View drawerContent;
    private EditText groupIndexText;
    private Spinner subgroupSpinner;

    public NavigationDrawerFragment() {
    }

    public void initialize(
            DrawerLayout drawerLayout, Toolbar toolbar, final DataFragment dataFragment
    ) {
        groupIndexText.setText(dataFragment.getGroup());
        subgroupSpinner.setSelection(dataFragment.getSubgroup() - 1);
        // subgroup 1 has (1 - 1) position in string array, etc.

        Activity activity = getActivity();

        toggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //Activity activity = getActivity();

                // Hide the keyboard
                /*InputMethodManager imm =
                        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(groupIndex.getWindowToken(), 0);*/
                groupIndexText.onEditorAction(EditorInfo.IME_ACTION_DONE);

                String group = groupIndexText.getText().toString();
                int subgroup = subgroupSpinner.getSelectedItemPosition() + 1;

                // Save new group and subgroup in DataFragment
                dataFragment.setGroup(group);
                if (dataFragment.getSubgroup() != subgroup) {
                    dataFragment.setSubgroup(subgroup);
                    dataFragment.broadcastDataUpdate();
                }
            }
        };
        drawerLayout.setDrawerListener(toggle);

        // Open drawer if user had never seen it
        SharedPreferences preferences =
                activity.getSharedPreferences(MainActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
        boolean seenDrawer = preferences.getBoolean(DRAWER_LEARNED_KEY, false);
        if (!seenDrawer) {
            drawerLayout.openDrawer(drawerContent);
            preferences.edit()
                    .putBoolean(DRAWER_LEARNED_KEY, true)
                    .apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_drawer_layout, container, false);

        drawerContent = root;

        // Show current parity string in the navigation drawer
        ((TextView) root.findViewById(R.id.current_parity)).setText(ParityReference.getStringFromParity(
                DateUtil.getWeekParity(DateTime.now(TimeZone.getDefault()))
        ));

        groupIndexText = (EditText) root.findViewById(R.id.group_index);

        // Manage subgroup spinner
        subgroupSpinner = (Spinner) root.findViewById(R.id.subgroup);
        ArrayAdapter<CharSequence> subgroupAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.subgroups,
                android.R.layout.simple_spinner_item
        );
        subgroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subgroupSpinner.setAdapter(subgroupAdapter);

        return root;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        toggle.syncState();
    }

}
