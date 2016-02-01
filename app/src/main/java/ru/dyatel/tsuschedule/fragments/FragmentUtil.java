package ru.dyatel.tsuschedule.fragments;

import android.app.Fragment;
import android.app.FragmentManager;

/**
 * TODO: documentation
 */
public class FragmentUtil {

    public static <T extends Fragment> T getFragment(FragmentManager fm, String tag, Class<T> type) {
        // Try to get the existing fragment
        Fragment f = fm.findFragmentByTag(tag);
        if (f == null) {
            try {
                // Try to instantiate new instance
                f = type.newInstance();
                fm.beginTransaction().add(f, tag).commit();
            } catch (InstantiationException e1) {
                throw new RuntimeException("Failed to instantiate the fragment!");
            } catch (IllegalAccessException e2) {
                throw new RuntimeException("Failed to instantiate the fragment: default constructor is not visible");
            }
        }
        return type.cast(f);
    }

}
