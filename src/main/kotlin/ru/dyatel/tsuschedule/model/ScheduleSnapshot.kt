package ru.dyatel.tsuschedule.model

data class ScheduleSnapshot(
        val id: Long,
        val group: String,
        val timestamp: Long,
        val pinned: Boolean,
        val selected: Boolean)
