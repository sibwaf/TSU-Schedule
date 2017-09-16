package ru.dyatel.tsuschedule.updater

import ru.dyatel.tsuschedule.BuildConfig

data class Release(val version: String, val url: String) : Comparable<Release> {

    companion object {
        private val VERSION_PATTERN = Regex("^v?((?:\\d+)(?:\\.\\d+)*)$")
        val CURRENT = Release(BuildConfig.VERSION_NAME, "")
    }

    private val components: List<Int>

    init {
        val match = VERSION_PATTERN.matchEntire(version)
                ?: throw IllegalArgumentException("Version name is malformed: $version")

        components = match.groupValues[1].split(".")
                .map { it.toInt() }
                .dropLastWhile { it == 0 }
                .toList()
    }

    override fun compareTo(other: Release): Int {
        val minComponents = minOf(components.size, other.components.size)

        for (i in 0 until minComponents) {
            if (components[i] > other.components[i]) return 1
            if (components[i] < other.components[i]) return -1
        }

        if (components.size > other.components.size) return 1
        if (components.size < other.components.size) return -1
        return 0
    }

}