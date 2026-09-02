package org.awaremate.shared

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun testPlatformNameIsNotEmpty() {
        val platform = getPlatform()
        assertNotNull(platform)
        assertTrue(platform.name.isNotBlank(), "Platform name should not be blank")
    }

    @Test
    fun testPlatformNameContainsExpectedPrefix() {
        val platform = getPlatform()
        val name = platform.name
        val isRecognized = name.startsWith("Android") || name.startsWith("iOS")
        assertTrue(isRecognized, "Platform name should identify platform: $name")
    }
}
