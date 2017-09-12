package ru.dyatel.tsuschedule.updater

import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.VERSION_PATTERN

data class Release(val version: String, val url: String) : Comparable<Release> {

    companion object {
        val CURRENT = Release(BuildConfig.VERSION_NAME, "")
    }

    private val components: List<Int>
    private val prerelease: Int?

    val isPrerelease: Boolean

    init {
        val match = VERSION_PATTERN.matchEntire(version)
                ?: throw IllegalArgumentException("Version name is malformed: $version")

        components = match.groupValues[1].split(".")
                .map { it.toInt() }
                .dropLastWhile { it == 0 }
                .toList()

        prerelease = match.groupValues[2].takeIf { it.isNotEmpty() }?.toInt()
        isPrerelease = prerelease != null
    }

    override fun compareTo(other: Release): Int {
        val minComponents = minOf(components.size, other.components.size)

        for (i in 0 until minComponents) {
            if (components[i] > other.components[i]) return 1
            if (components[i] < other.components[i]) return -1
        }

        if (components.size > other.components.size) return 1
        if (components.size < other.components.size) return -1

        if (prerelease == null && other.prerelease == null) return 0
        if (prerelease == null && other.prerelease != null) return 1
        if (prerelease != null && other.prerelease == null) return -1

        prerelease!!
        other.prerelease!!

        if (prerelease > other.prerelease) return 1
        if (prerelease < other.prerelease) return -1
        return 0
    }

}
