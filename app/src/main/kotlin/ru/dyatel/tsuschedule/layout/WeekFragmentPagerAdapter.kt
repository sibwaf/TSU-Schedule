package ru.dyatel.tsuschedule.layout

import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.support.v13.app.FragmentPagerAdapter
import ru.dyatel.tsuschedule.fragments.WeekFragment
import ru.dyatel.tsuschedule.data.indexToParity

class WeekFragmentPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {

    override fun getCount() = 2

    override fun getItem(position: Int): Fragment = WeekFragment.getInstance(indexToParity(position))

    override fun getPageTitle(position: Int) = indexToParity(position).toText(context)

}