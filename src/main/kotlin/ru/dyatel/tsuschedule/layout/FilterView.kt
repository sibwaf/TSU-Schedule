package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.cardview.v7._CardView
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.find
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.spinner
import org.jetbrains.anko.switch
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.model.Filter
import ru.dyatel.tsuschedule.model.SubgroupFilter

open class FilterView(context: Context) : _CardView(context) {

    private companion object {
        val headerViewId = View.generateViewId()
        val bodyViewId = View.generateViewId()
        val switchViewId = View.generateViewId()
    }

    private val headerView: TextView
    private val switchView: Switch
    private val bodyContainer: ViewGroup

    init {
        lparams(width = matchParent) {
            leftMargin = DIM_CARD_HORIZONTAL_MARGIN
            rightMargin = DIM_CARD_HORIZONTAL_MARGIN
            topMargin = DIM_CARD_VERTICAL_MARGIN
            bottomMargin = DIM_CARD_VERTICAL_MARGIN
        }

        cardElevation = DIM_ELEVATION_F
        radius = DIM_CARD_RADIUS_F

        context.verticalLayout {
            lparams(width = matchParent) {
                padding = DIM_CARD_PADDING
            }

            relativeLayout {
                lparams(width = matchParent)

                textView {
                    id = headerViewId
                    textSize = SP_TEXT_MEDIUM
                    gravity = Gravity.CENTER
                }.lparams {
                    centerVertically()
                    alignParentLeft()
                    leftOf(switchViewId)
                }

                switch {
                    id = switchViewId
                }.lparams {
                    centerVertically()
                    alignParentRight()
                }
            }

            frameLayout {
                id = bodyViewId

                lparams(width = matchParent) {
                    topMargin = DIM_CARD_PADDING
                }

                visibility = View.GONE
            }
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
