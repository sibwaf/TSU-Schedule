package ru.dyatel.tsuschedule.events

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

private fun <K, V> getOrInitList(map: MutableMap<K, MutableList<V>>, key: K): MutableList<V> {
    var list = map[key]
    if (list == null) {
        list = ArrayList<V>()
        map[key] = list
    }
    return list
}
