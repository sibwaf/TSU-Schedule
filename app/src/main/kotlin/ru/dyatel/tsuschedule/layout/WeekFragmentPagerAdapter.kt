package ru.dyatel.tsuschedule.layout

import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.support.v13.app.FragmentPagerAdapter
import android.util.SparseArray
import android.view.ViewGroup
import ru.dyatel.tsuschedule.fragments.WeekFragment
import ru.dyatel.tsuschedule.parsing.indexToParity

class WeekFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {

    private val registry = SparseArray<WeekFragment>(2)

    override fun getCount() = 2

    override fun getItem(position: Int): Fragment {
        val fragment = WeekFragment.getInstance(indexToParity(position))
        registry.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        registry.remove(position)
        super.destroyItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int) = indexToParity(position).toText(context)

    fun getFragment(position: Int): WeekFragment = registry[position]

}