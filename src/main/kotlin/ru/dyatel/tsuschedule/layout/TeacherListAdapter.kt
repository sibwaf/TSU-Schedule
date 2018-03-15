package ru.dyatel.tsuschedule.layout

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.wealthfront.magellan.Navigator
import org.jetbrains.anko.Bold
import org.jetbrains.anko.buildSpanned
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sp
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.Teacher
import org.jetbrains.anko.append as ankoAppend

class TeacherListAdapter : RecyclerView.Adapter<TeacherListAdapter.Holder>() {

    private companion object {
        val nameViewId = View.generateViewId()

        val NAME_PATTERN = Regex("^(.+?) (.+)$")
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView = view.find<TextView>(nameViewId)
    }

    private val teachers = ArrayList<Teacher>()

    private var navigator: Navigator? = null

    fun bindNavigator(navigator: Navigator) {
        this.navigator = navigator
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val ctx = parent.context
        val view = ctx.frameLayout {
            lparams(width = matchParent) {
                margin = dip(4)
                padding = dip(4)
                leftPadding = dip(8)
                rightPadding = dip(8)
            }

            textView {
                id = nameViewId
                textSize = sp(7).toFloat()
                textColor = ContextCompat.getColor(ctx, R.color.text_color)
            }
        }
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.setOnClickListener {
            navigator?.goTo(TODO())
        }

        val parts = NAME_PATTERN.matchEntire(teachers[position].name)!!.groups
        val text = buildSpanned {
            ankoAppend(parts[1]!!.value, Bold, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            ankoAppend(" " + parts[2]!!.value)
        }
        holder.nameView.text = text
    }

    override fun getItemCount() = teachers.size

    fun updateData(teachers: List<Teacher>) {
        with(this.teachers) {
            clear()
            addAll(teachers)
        }
        notifyDataSetChanged()
    }

    fun getData(): List<Teacher> = teachers

}
