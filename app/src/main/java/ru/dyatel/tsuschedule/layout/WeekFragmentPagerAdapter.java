package ru.dyatel.tsuschedule.layout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import ru.dyatel.tsuschedule.ParityReference;
import ru.dyatel.tsuschedule.fragments.WeekFragment;

import java.util.Locale;

public class WeekFragmentPagerAdapter extends FragmentPagerAdapter {

	public WeekFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return WeekFragment.newInstance(ParityReference.getParityFromIndex(position));
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
