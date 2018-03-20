package ru.dyatel.tsuschedule.model

data class RawSchedule(val timestamp: Long, val data: String)

data class ScheduleSnapshot(val schedule: RawSchedule, val pinned: Boolean, val selected: Boolean)
