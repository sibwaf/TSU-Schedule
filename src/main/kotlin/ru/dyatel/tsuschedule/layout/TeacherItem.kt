package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import org.jetbrains.anko.Bold
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.buildSpanned
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import ru.dyatel.tsuschedule.ADAPTER_TEACHER_ITEM_ID
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.model.Teacher
import org.jetbrains.anko.append as ankoAppend

class TeacherItem(val teacher: Teacher) : AbstractItem<TeacherItem, TeacherItem.ViewHolder>() {

    private companion object {
        val nameViewId = View.generateViewId()
        val NAME_PATTERN = Regex("^(.+?) (.+)$")
    }

    init {
        withIdentifier(teacher.id.hashCode().toLong())
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<TeacherItem>(view) {
        private val nameView = view.find<TextView>(nameViewId)

        override fun bindView(item: TeacherItem, payloads: List<Any>) {
            val parts = NAME_PATTERN.matchEntire(item.teacher.name)!!.groups
            val text = buildSpanned {
                ankoAppend(parts[1]!!.value, Bold, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                ankoAppend(" " + parts[2]!!.value)
            }
            nameView.text = text
        }

        override fun unbindView(item: TeacherItem) {
            nameView.text = null
        }
    }

    override fun createView(ctx: Context, parent: ViewGroup?): View {
        return ctx.frameLayout {
            lparams(width = matchParent) {
                leftPadding = DIM_TEXT_ITEM_HORIZONTAL_PADDING
                rightPadding = DIM_TEXT_ITEM_HORIZONTAL_PADDING
                topPadding = DIM_TEXT_ITEM_VERTICAL_PADDING
                bottomPadding = DIM_TEXT_ITEM_VERTICAL_PADDING
            }

            textView {
                id = nameViewId
                textSize = SP_TEXT_MEDIUM
                textColor = ContextCompat.getColor(ctx, R.color.text_color)
            }
        }
    }

    override fun getType() = ADAPTER_TEACHER_ITEM_ID

    override fun getViewHolder(view: View) = ViewHolder(view)

    override fun getLayoutRes() = throw UnsupportedOperationException()

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other as TeacherItem).teacher == teacher
    }

}
