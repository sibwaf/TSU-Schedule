package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.SparseBooleanArray;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.Locale;

public class WeekFragmentPagerAdapter extends FragmentPagerAdapter {

    private SparseBooleanArray initializedFragments = new SparseBooleanArray(2);

    private DataFragment dataFragment;

    public WeekFragmentPagerAdapter(FragmentManager fm, DataFragment dataFragment) {
        super(fm);

        this.dataFragment = dataFragment;

        for (int i = 0; i < initializedFragments.size(); i++) {
            initializedFragments.put(i, false);
        }
    }

    @Override
    public Fragment getItem(int position) {
        Parity p = ParityReference.getParityFromIndex(position);

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
        return initializedFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ParityReference.getStringFromIndex(position).toUpperCase(Locale.getDefault());
    }

}
