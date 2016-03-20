package ru.dyatel.tsuschedule.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import ru.dyatel.tsuschedule.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

}
