package ru.dyatel.tsuschedule.utilities

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NullableLateinit<T> : ReadWriteProperty<Any, T?> {

    private var initialized = false
    private var value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        if (initialized) return value
        throw IllegalStateException("Property ${property.name} must be initialized before usage")
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        this.value = value
        initialized = true
    }

}
