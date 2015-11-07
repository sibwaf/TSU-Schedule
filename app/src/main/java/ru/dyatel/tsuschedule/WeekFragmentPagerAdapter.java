package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.Locale;

public class WeekFragmentPagerAdapter extends FragmentPagerAdapter {

    private int readyFragments = 0;

    private DataFragment dataFragment;

    public WeekFragmentPagerAdapter(FragmentManager fm, DataFragment dataFragment) {
        super(fm);

        this.dataFragment = dataFragment;
    }

    @Override
    public Fragment getItem(int position) {
        Parity p = ParityReference.getParityFromIndex(position);

        // Create a fragment and subscribe it to data updates
        WeekFragment fragment = WeekFragment.newInstance(p);
        dataFragment.addListener(fragment);
        fragment.setCallback(this);

        return fragment;
    }

    void fragmentReady() {
        readyFragments++;
        if (readyFragments == 2) dataFragment.loadSavedData();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ParityReference.getStringFromIndex(position).toUpperCase(Locale.getDefault());
    }

}
