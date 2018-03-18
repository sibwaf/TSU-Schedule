package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import ru.dyatel.tsuschedule.NORMAL_WEEKDAY_ORDER
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.model.Parity

class WeekPage<T : Lesson>(val parity: Parity) {

    val adapter = ItemAdapter<WeekdayItem<T>>()
    private val fastAdapter: FastAdapter<WeekdayItem<T>> = FastAdapter.with(adapter)

    fun createView(context: Context): View {
        return context.recyclerView {
            lparams { width = matchParent }
            layoutManager = LinearLayoutManager(context)
            adapter = fastAdapter
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    }

}

class WeekDataContainer<T : Lesson>(private val viewProvider: (Context) -> LessonView<T>) {

    val oddWeek = WeekPage<T>(Parity.ODD)
    val evenWeek = WeekPage<T>(Parity.EVEN)

    fun updateData(lessons: List<T>) {
        val (odd, even) = lessons.partition { it.parity == Parity.ODD }
        oddWeek.adapter.set(generateWeekdays(odd))
        evenWeek.adapter.set(generateWeekdays(even))
    }

    private fun generateWeekdays(lessons: List<T>): List<WeekdayItem<T>> {
        return lessons
                .groupBy { it.weekday.toLowerCase() }
                .toList()
                .sortedBy { NORMAL_WEEKDAY_ORDER.indexOf(it.first) }
                .map { (weekday, lessons) -> WeekdayItem(weekday, lessons, viewProvider) }
    }

}

class WeekPagerAdapter<T : Lesson>(private val context: Context, data: WeekDataContainer<T>) : PagerAdapter() {

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
        container.removeView(views.remove(obj as Parity))
    }

    override fun getPageTitle(position: Int) = pages[position].parity.toText(context)

    fun getPosition(parity: Parity) =
            pages.indexOfFirst { it.parity == parity }.takeUnless { it == -1 }
                    ?: throw IllegalArgumentException("No pages with parity <$parity> exist")

}
