package ru.dyatel.tsuschedule.layout

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import android.view.ViewGroup
import ru.dyatel.tsuschedule.ParityReference
import ru.dyatel.tsuschedule.fragments.WeekFragment

class WeekFragmentPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val registry = SparseArray<WeekFragment>(2)

    override fun getCount() = 2

    override fun getItem(position: Int): Fragment {
        val fragment = WeekFragment(ParityReference.getParityFromIndex(position))
        registry.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        registry.remove(position)
        super.destroyItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int) = ParityReference.getStringFromIndex(position).toUpperCase()

    fun getFragment(position: Int): WeekFragment = registry[position]

}