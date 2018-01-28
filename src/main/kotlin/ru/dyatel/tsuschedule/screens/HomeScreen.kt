package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.InputType
import android.view.Gravity
import android.view.View
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.button
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.design.themedTextInputEditText
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.topOf
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus

class HomeView(context: Context) : BaseScreenView<HomeScreen>(context) {

    private companion object {
        val formContainerId = View.generateViewId()
    }

    init {
        backgroundColorResource = R.color.primary_color

        relativeLayout {
            padding = dip(10)

            val icon = ContextCompat.getDrawable(context, R.drawable.logo)!!
            val color = ContextCompat.getColor(context, R.color.text_title_color)
            DrawableCompat.setTint(icon, color)

            imageView(icon).lparams {
                topOf(formContainerId)
                centerHorizontally()

                bottomMargin = dip(10)
            }

            verticalLayout {
                id = formContainerId

                textInputLayout {
                    setHintTextAppearance(R.style.HomeFormHintTheme)
                    setErrorTextAppearance(R.style.HomeFormErrorTheme)

                    hint = context.getString(R.string.form_hint_group_index)

                    themedTextInputEditText(R.style.HomeFormEditTextTheme) {
                        gravity = Gravity.CENTER
                        singleLine = true
                        inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    }
                }.lparams(width = dip(300))

                button(R.string.button_add_group).lparams(width = dip(300))
            }.lparams {
                centerInParent()
            }
        }
    }

}

class HomeScreen : Screen<HomeView>() {

    override fun createView(context: Context) = HomeView(context)

    override fun onShow(context: Context?) {
        super.onShow(context)
        EventBus.broadcast(Event.SET_TOOLBAR_SHADOW_ENABLED, false)
    }

    override fun onHide(context: Context?) {
        EventBus.broadcast(Event.SET_TOOLBAR_SHADOW_ENABLED, true)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_home)!!

}
