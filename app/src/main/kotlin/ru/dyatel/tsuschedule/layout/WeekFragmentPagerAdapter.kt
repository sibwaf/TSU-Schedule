package ru.dyatel.tsuschedule.layout

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import ru.dyatel.tsuschedule.ParityReference
import ru.dyatel.tsuschedule.fragments.WeekFragment

class WeekFragmentPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount() = 2

    override fun getItem(position: Int) = WeekFragment(ParityReference.getParityFromIndex(position))

    override fun getPageTitle(position: Int) = ParityReference.getStringFromIndex(position).toUpperCase()

}