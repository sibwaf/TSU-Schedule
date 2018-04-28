package ru.dyatel.tsuschedule.model

data class VersionChangelog(
        val version: String,
        val prerelease: Boolean,
        val changelog: String
)