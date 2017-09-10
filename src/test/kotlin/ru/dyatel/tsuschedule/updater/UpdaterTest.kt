package ru.dyatel.tsuschedule.updater

import org.junit.Test

class UpdaterTest {

    @Test fun testBuildVersionName() {
        Release("1.0.0", "").isNewerThanInstalled()
    }

}
