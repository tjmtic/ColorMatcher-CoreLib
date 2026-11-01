package com.abyxcz.template.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class GreeterTest {
    private val greeter = Greeter()

    @Test
    fun usesThePlatformName() {
        assertEquals("Android 36", greeter.nameFor("  Android 36 "))
    }

    @Test
    fun fallsBackWhenThereIsNone() {
        assertEquals(Greeter.FALLBACK, greeter.nameFor(" "))
    }
}
