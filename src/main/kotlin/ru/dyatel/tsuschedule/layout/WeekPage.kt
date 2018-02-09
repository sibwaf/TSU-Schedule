package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import ru.dyatel.tsuschedule.data.Lesson
import ru.dyatel.tsuschedule.data.Parity

class WeekDataContainer {

    val oddWeek = WeekPage(Parity.ODD)
    val evenWeek = WeekPage(Parity.EVEN)

    fun updateData(odd: List<Lesson>, even: List<Lesson>) {
        oddWeek.updateData(odd)
        evenWeek.updateData(even)
    }

}

class WeekPage(val parity: Parity) {

    private val adapter = WeekdayListAdapter()

    fun createView(context: Context): View {
        val instanceAdapter = adapter
        return context.recyclerView {
            lparams { width = matchParent }
            layoutManager = LinearLayoutManager(context)
            adapter = instanceAdapter
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    }

    fun updateData(data: List<Lesson>) = adapter.updateData(data)

}

class WeekPagerAdapter(private val context: Context, data: WeekDataContainer) : PagerAdapter() {

    private val pages = listOf(data.oddWeek, data.evenWeek)
    private val views = mutableMapOf<Parity, View>()

    override fun isViewFromObject(view: View, obj: Any) = views[obj] == view

    override fun getCount() = pages.size

    override fun instantiateItem(container: ViewGroup, position: Int): Parity {
        val page = pages[position]
        page.createView(context).let {
            views[page.parity] = it
            container.addView(it)
        }
        return page.parity
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        if (obj !is Parity)
            throw IllegalArgumentException("Key must be a Parity instance")
        container.removeView(views.remove(obj))
    }

    override fun getPageTitle(position: Int) = pages[position].parity.toText(context)

    fun getPosition(parity: Parity) =
            pages.indexOfFirst { it.parity == parity }.takeUnless { it == -1 }
                    ?: throw IllegalArgumentException("No pages with parity <$parity> exist")

}
