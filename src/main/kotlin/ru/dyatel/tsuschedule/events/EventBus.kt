package ru.dyatel.tsuschedule.events

import java.util.ArrayList
import java.util.EnumMap

private typealias EventListenerList = MutableList<EventListener>

object EventBus {

    private val listeners = EnumMap<Event, EventListenerList>(Event::class.java)

    fun subscribe(listener: EventListener, vararg types: Event) {
        val typeArray = types.takeIf { it.isNotEmpty() } ?: Event.values()
        typeArray.forEach {
            val list = listeners.getOrPut(it, { ArrayList() })
            list += listener
        }
    }

    fun unsubscribe(listener: EventListener) = listeners.values.forEach { it.remove(listener) }

    fun broadcast(event: Event, payload: Any? = null) = listeners[event]?.forEach { it.handleEvent(event, payload) }

}
