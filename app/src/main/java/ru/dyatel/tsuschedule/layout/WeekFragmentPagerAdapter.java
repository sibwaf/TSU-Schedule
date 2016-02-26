package ru.dyatel.tsuschedule.layout;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import ru.dyatel.tsuschedule.ParityReference;
import ru.dyatel.tsuschedule.data.DataFragment;
import ru.dyatel.tsuschedule.fragments.WeekFragment;

import java.util.Locale;

public class WeekFragmentPagerAdapter extends FragmentPagerAdapter {

    public WeekFragmentPagerAdapter(FragmentManager fm, DataFragment dataFragment) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return WeekFragment.newInstance(
                ParityReference.getParityFromIndex(position)
        );
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
