package ru.dyatel.tsuschedule.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hirondelle.date4j.DateTime
import org.jetbrains.anko.find
import ru.dyatel.tsuschedule.ParityReference
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.layout.WeekFragmentPagerAdapter
import ru.dyatel.tsuschedule.parsing.DateUtil
import java.util.TimeZone

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.main_screen, container, false)

        val pager = root.find<ViewPager>(R.id.pager)
        pager.adapter = WeekFragmentPagerAdapter(childFragmentManager)
        pager.currentItem = ParityReference.getIndexFromParity(
                DateUtil.getWeekParity(DateTime.now(TimeZone.getDefault()))
        )

        val tabLayout = root.find<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(pager)

        return root
    }

}
