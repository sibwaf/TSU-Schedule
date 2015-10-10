package ru.dyatel.tsuschedule;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.SparseBooleanArray;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.Locale;

public class WeekFragmentPagerAdapter extends FragmentPagerAdapter {

    private SparseBooleanArray initializedFragments = new SparseBooleanArray(2);

    private Context context;
    private DataFragment dataFragment;

    public WeekFragmentPagerAdapter(FragmentManager fm, DataFragment dataFragment, Context context) {
        super(fm);
        this.dataFragment = dataFragment;
        this.context = context;

        for (int i = 0; i < 2; i++) {
            initializedFragments.put(i, false);
        }
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
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.odd_week).toUpperCase(l);
            case 1:
                return context.getString(R.string.even_week).toUpperCase(l);
        }
        return null;
    }

}
