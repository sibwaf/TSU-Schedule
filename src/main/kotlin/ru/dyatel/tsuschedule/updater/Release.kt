package ru.dyatel.tsuschedule.updater

import ru.dyatel.tsuschedule.BuildConfig

data class Release(val version: String, val url: String) {

    private companion object {
        val VERSION_PATTERN = Regex("^((?:\\d+)(?:\\.\\d+)*)(.*)$")
    }

    fun isNewerThanInstalled(): Boolean {
        val match = VERSION_PATTERN.matchEntire(BuildConfig.VERSION_NAME)
                ?: throw RuntimeException("Current version name is malformed")

        val number = match.groupValues[1]
        val isPrerelease = match.groupValues[2].isNotEmpty()

        val currentVersionComponents = number.split(".")
        val releaseVersionComponents = version.split(".")

        val componentCount = minOf(currentVersionComponents.size, releaseVersionComponents.size)

        for (i in 0 until componentCount) {
            if (releaseVersionComponents[i] > currentVersionComponents[i]) return true
            if (releaseVersionComponents[i] < currentVersionComponents[i]) return false
        }

        if (currentVersionComponents.size > componentCount) {
            val sum = currentVersionComponents
                    .filterIndexed { index, _ -> index >= componentCount }
                    .sumBy { it.toInt() }
            if (sum > 0) return false
        } else if (releaseVersionComponents.size > componentCount) {
            val sum = releaseVersionComponents
                    .filterIndexed { index, _ -> index >= componentCount }
                    .sumBy { it.toInt() }
            if (sum > 0) return true
        }

        return isPrerelease
    }

}
