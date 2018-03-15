package ru.dyatel.tsuschedule.utilities

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.experimental.launch as launchCoroutine

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

class ReplacingJobLauncher(private val dispatcher: CoroutineDispatcher) {

    private var task: Job? = null

    fun launch(block: suspend () -> Unit) {
        val oldTask = task
        task = launchCoroutine(dispatcher) {
            oldTask?.cancelAndJoin()
            block()
        }
    }

    suspend fun join() = task?.join()

    fun cancel() = task?.cancel()

    suspend fun cancelAndJoin() = task?.cancelAndJoin()

}
