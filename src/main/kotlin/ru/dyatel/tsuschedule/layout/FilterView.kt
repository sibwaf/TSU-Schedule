package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sp
import org.jetbrains.anko.spinner
import org.jetbrains.anko.switch
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.data.Filter
import ru.dyatel.tsuschedule.data.SubgroupFilter

open class FilterView(context: Context) : CardView(context) {

    private companion object {
        val headerViewId = View.generateViewId()
        val bodyViewId = View.generateViewId()
        val switchViewId = View.generateViewId()
    }

    private val headerView: TextView
    private val switchView: Switch
    private val bodyContainer: ViewGroup

    init {
        layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { margin = dip(6) }
        radius = dip(2).toFloat()

        context.verticalLayout {
            lparams {
                width = matchParent
                margin = dip(4)
            }

            relativeLayout {
                lparams(width = matchParent)

                textView {
                    id = headerViewId
                    textSize = sp(6).toFloat()
                }.lparams {
                    alignParentLeft()
                    leftOf(switchViewId)
                    leftMargin = dip(4)
                }
                switch { id = switchViewId }.lparams { alignParentRight() }
            }
            frameLayout {
                id = bodyViewId

                lparams {
                    width = matchParent
                    margin = dip(4)
                }
            }.apply { visibility = View.GONE }
        }.let { super.addView(it) }

        headerView = find(headerViewId)
        switchView = find(switchViewId)
        bodyContainer = find(bodyViewId)
    }

    fun attachFilter(filter: Filter) {
        switchView.isChecked = filter.enabled
        switchView.setOnCheckedChangeListener { _, checked -> filter.enabled = checked }
    }

    fun setHeader(resource: Int) {
        headerView.setText(resource)
    }

    override fun addView(child: View) {
        bodyContainer.addView(child)
        bodyContainer.visibility = View.VISIBLE
    }

}

class SubgroupFilterView(context: Context) : FilterView(context) {

    private val spinner: Spinner

    init {
        setHeader(R.string.filter_subgroup)

        spinner = spinner {
            val items = context.resources.getStringArray(R.array.subgroups)
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
    }

    fun attachFilter(filter: SubgroupFilter) {
        super.attachFilter(filter)

        spinner.setSelection(filter.subgroup - 1)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) = Unit

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                filter.subgroup = position + 1
            }
        }
    }

}
