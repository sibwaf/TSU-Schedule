package ru.dyatel.tsuschedule.utilities

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.launch as launchCoroutine

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
