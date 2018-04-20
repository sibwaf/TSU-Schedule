package ru.dyatel.tsuschedule.screens

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.themedTextInputLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.themedButton
import org.jetbrains.anko.topOf
import org.jetbrains.anko.verticalLayout
import ru.dyatel.tsuschedule.BlankGroupIndexException
import ru.dyatel.tsuschedule.R
import ru.dyatel.tsuschedule.ShortGroupIndexException
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.utilities.Validator
import ru.dyatel.tsuschedule.utilities.hideKeyboard

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

                val input = themedTextInputLayout(R.style.HomeFormInputLayoutTheme) {
                    hint = context.getString(R.string.form_hint_group_index)
                    setErrorTextAppearance(R.style.HomeFormErrorTheme)

                    textInputEditText {
                        imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI

                        gravity = Gravity.CENTER
                        singleLine = true
                        inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    }
                }.lparams(width = dip(300))

                themedButton(R.string.button_add_group, R.style.HomeFormSubmitTheme) {
                    setOnClickListener {
                        try {
                            val group = Validator.validateGroup(input.editText!!.text.toString())
                            EventBus.broadcast(Event.ADD_GROUP, group)
                        } catch (e: BlankGroupIndexException) {
                            input.error = context.getString(R.string.form_error_blank_group)
                        } catch (e: ShortGroupIndexException) {
                            input.error = context.getString(R.string.form_error_short_group)
                        }
                    }
                }.lparams(width = dip(300))
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
        activity.hideKeyboard()
        EventBus.broadcast(Event.SET_TOOLBAR_SHADOW_ENABLED, true)
        super.onHide(context)
    }

    override fun getTitle(context: Context) = context.getString(R.string.screen_home)!!

}
