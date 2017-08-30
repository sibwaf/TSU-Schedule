package ru.dyatel.tsuschedule

import org.junit.Test

class UpdaterTest {

    @Test fun testBuildVersionName() {
        Release("1.0.0", "").isNewerThanInstalled()
    }

}
