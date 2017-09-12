package ru.dyatel.tsuschedule.updater

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReleaseTest {

    private fun release(version: String) = Release(version, "")

    @Test fun testBuildVersionName() {
        Release.CURRENT
    }

    @Test fun testEqualRelease() {
        assertEquals(0, release("0.1.0").compareTo(release("0.1.0.0.0")))
        assertEquals(0, release("1.0.0").compareTo(release("1.0.0.0.0")))
    }

    @Test fun testLesserRelease() {
        assertTrue(release("0.1.0.0.0") < release("0.2.0"))
        assertTrue(release("0.1.0") < release("0.1.0.0.0.1"))
    }

    @Test fun testGreaterRelease() {
        assertTrue(release("0.2.0") > release("0.1.0.0.0"))
        assertTrue(release("0.1.0.0.0.1") > release("0.1.0"))
    }

}
