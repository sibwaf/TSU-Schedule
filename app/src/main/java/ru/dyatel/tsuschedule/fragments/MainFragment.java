package ru.dyatel.tsuschedule.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import hirondelle.date4j.DateTime;
import ru.dyatel.tsuschedule.ParityReference;
import ru.dyatel.tsuschedule.R;
import ru.dyatel.tsuschedule.layout.WeekFragmentPagerAdapter;
import ru.dyatel.tsuschedule.parsing.DateUtil;

import java.util.TimeZone;

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_layout, container, false);

        // Set up the ViewPager with the sections adapter and select current parity tab
        ViewPager viewPager = (ViewPager) root.findViewById(R.id.pager);
        viewPager.setAdapter(new WeekFragmentPagerAdapter(getChildFragmentManager()));
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

}
