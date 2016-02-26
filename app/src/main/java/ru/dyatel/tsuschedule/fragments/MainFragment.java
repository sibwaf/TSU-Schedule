package ru.dyatel.tsuschedule.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import hirondelle.date4j.DateTime;
import org.solovyev.android.views.llm.LinearLayoutManager;
import ru.dyatel.tsuschedule.MainActivity;
import ru.dyatel.tsuschedule.ParityReference;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.layout.MenuButtonAdapter;
import ru.dyatel.tsuschedule.layout.WeekFragmentPagerAdapter;
import ru.dyatel.tsuschedule.parsing.DateUtil;

import java.util.TimeZone;

public class MainFragment extends Fragment {

    private static final String DRAWER_LEARNED_KEY = "drawer_learned";

    private DataFragment dataFragment;

    private ActionBarDrawerToggle toggle;

    private EditText groupIndexText;
    private Spinner subgroupSpinner;

    private void initDrawer(DrawerLayout layout, Toolbar toolbar, final DataFragment data) {
        groupIndexText.setText(data.getGroup());
        subgroupSpinner.setSelection(data.getSubgroup() - 1);
        // subgroup 1 has (1 - 1) position in string array, etc.

        Activity activity = getActivity();

        toggle = new ActionBarDrawerToggle(
                activity,
                layout,
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
                data.setGroup(group);
                if (data.getSubgroup() != subgroup) {
                    data.setSubgroup(subgroup);
                    data.broadcastDataUpdate();
                }
            }
        };
        layout.addDrawerListener(toggle);

        // Open drawer if user had never seen it
        SharedPreferences preferences =
                activity.getSharedPreferences(MainActivity.PREFERENCES_FILE, Context.MODE_PRIVATE);
        boolean seenDrawer = preferences.getBoolean(DRAWER_LEARNED_KEY, false);
        if (!seenDrawer) {
            layout.openDrawer(Gravity.LEFT);
            preferences.edit()
                    .putBoolean(DRAWER_LEARNED_KEY, true)
                    .apply();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataFragment = (DataFragment) getFragmentManager().findFragmentByTag(DataFragment.TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_layout, container, false);

        FragmentManager fm = getFragmentManager();

        // -----------------------
        // Navigation drawer start

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

        // Manage the app menu
        RecyclerView appMenu = (RecyclerView) root.findViewById(R.id.menu_list);
        appMenu.setLayoutManager(new LinearLayoutManager(root.getContext()));
        MenuButtonAdapter menuAdapter = new MenuButtonAdapter();
        menuAdapter.addMenuEntry(new MenuButtonAdapter.MenuEntry(
                R.drawable.ic_settings, R.string.action_settings,
                new Runnable() {
                    @Override
                    public void run() {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_fragment, new SettingsFragment())
                                .addToBackStack("settings_opened")
                                .commit();
                    }
                })
        );
        appMenu.setAdapter(menuAdapter);

        initDrawer(
                (DrawerLayout) root,
                (Toolbar) container.findViewById(R.id.toolbar),
                dataFragment
        );

        // Navigation drawer end
        // ---------------------

        // Set up the ViewPager with the sections adapter and select current parity tab
        ViewPager viewPager = (ViewPager) root.findViewById(R.id.pager);
        viewPager.setAdapter(new WeekFragmentPagerAdapter(fm));
        viewPager.setCurrentItem(
                ParityReference.getIndexFromParity(
                        DateUtil.getWeekParity(DateTime.now(TimeZone.getDefault()))
                )
        );

        // Set up the TabLayout
        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

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
