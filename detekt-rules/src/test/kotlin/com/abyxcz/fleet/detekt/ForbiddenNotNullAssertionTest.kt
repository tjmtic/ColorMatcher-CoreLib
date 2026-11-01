package com.abyxcz.fleet.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ForbiddenNotNullAssertionTest {

    private val rule = ForbiddenNotNullAssertion(Config.empty)

    @Test
    fun `reports a not-null assertion`() {
        val findings = rule.lint("fun boom(value: String?) = value!!.length")
        assertEquals(1, findings.size)
        assertEquals("ForbiddenNotNullAssertion", findings.single().id)
        assertTrue(findings.single().message.contains("review blocker"))
    }

    @Test
    fun `reports every assertion in a chain`() {
        val findings = rule.lint("fun boom(a: String?, b: String?) = a!!.length + b!!.length")
        assertEquals(2, findings.size)
    }

    @Test
    fun `accepts safe alternatives`() {
        val findings =
            rule.lint(
                """
                fun safe(value: String?): Int = value?.length ?: 0
                fun negate(flag: Boolean) = !flag
                """
                    .trimIndent()
            )
        assertEquals(0, findings.size)
    }
}
