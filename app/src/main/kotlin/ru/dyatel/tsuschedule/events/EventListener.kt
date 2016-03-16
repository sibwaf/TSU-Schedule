package ru.dyatel.tsuschedule.events

interface EventListener {

    fun handleEvent(type: Event)

}
