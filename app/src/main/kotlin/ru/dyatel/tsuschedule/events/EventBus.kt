package ru.dyatel.tsuschedule.events

import android.app.Activity
import ru.dyatel.tsuschedule.MainActivity
import java.util.ArrayList
import java.util.EnumMap

class EventBus {

    private val listeners = EnumMap<Event, MutableList<EventListener>>(Event::class.java)

    fun subscribe(listener: EventListener, vararg types: Event) =
            types.forEach { getOrInitList(listeners, it) += listener }

    fun unsubscribe(listener: EventListener) =
            listeners.values.forEach { it.remove(listener) }

    fun broadcast(event: Event) =
            listeners.values.flatten().forEach { it.handleEvent(event) }

}

fun Activity.getEventBus(): EventBus =
        if (this is MainActivity) eventBus
        else throw IllegalArgumentException("Provided activity is not a MainActivity!")

private fun <K, V> getOrInitList(map: MutableMap<K, MutableList<V>>, key: K): MutableList<V> {
    var list = map[key]
    if (list == null) {
        list = ArrayList<V>()
        map[key] = list
    }
    return list
}
